package com.justbaat.mybishnoiapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val firebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    // Get Firebase Bearer Token
    suspend fun getToken(): String? {
        return try {
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            println("🔑 Firebase Token: $token") // Add this line
            token
        } catch (e: Exception) {
            null
        }
    }


    // Force refresh token
    suspend fun refreshToken(): String? {
        return try {
            firebaseAuth.currentUser?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    // Save user data
    fun saveUserData(userId: String, email: String, name: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            apply()
        }
    }

    // Get user data
    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)
    fun getUserEmail(): String? = sharedPreferences.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = sharedPreferences.getString(KEY_USER_NAME, null)

    // Check if user is logged in
    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    // Clear all data on logout
    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }

    // Get current Firebase user
    fun getCurrentUser() = firebaseAuth.currentUser
}
