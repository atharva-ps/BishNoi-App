package com.app.bishnoi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException
import java.security.GeneralSecurityException

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAuth = FirebaseAuth.getInstance()

    // ✅ Lazy initialization with error handling
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            createEncryptedPreferences()
        } catch (e: GeneralSecurityException) {
            // KeyStore corrupted - delete and recreate
            println("⚠️ KeyStore corrupted, clearing preferences")
            clearCorruptedPreferences()
            createEncryptedPreferences()
        } catch (e: IOException) {
            println("⚠️ IO error with encrypted preferences")
            clearCorruptedPreferences()
            createEncryptedPreferences()
        }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_ADMIN = "is_admin"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val PREFS_NAME = "auth_prefs"
    }

    // ✅ Create encrypted preferences
    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ✅ Clear corrupted preferences
    private fun clearCorruptedPreferences() {
        try {
            // Delete the corrupted file
            val prefsFile = context.getFileStreamPath("${PREFS_NAME}.xml")
            val masterKeyFile = context.getFileStreamPath("${PREFS_NAME}_master_key")

            prefsFile?.delete()
            masterKeyFile?.delete()

            // Also try to delete via shared preferences
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        } catch (e: Exception) {
            println("⚠️ Error clearing preferences: ${e.message}")
        }
    }

    suspend fun getToken(): String? {
        return try {
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            println("🔑 Firebase Token: $token")
            token
        } catch (e: Exception) {
            null
        }
    }

    suspend fun refreshToken(): String? {
        return try {
            firebaseAuth.currentUser?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    fun saveUserData(userId: String, email: String, name: String, isAdmin: Boolean = false) {
        try {
            sharedPreferences.edit().apply {
                putString(KEY_USER_ID, userId)
                putString(KEY_USER_EMAIL, email)
                putString(KEY_USER_NAME, name)
                putBoolean(KEY_IS_ADMIN, isAdmin)
                apply()
            }
        } catch (e: Exception) {
            println("⚠️ Error saving user data: ${e.message}")
        }
    }

    // ✅ NEW: FCM Token methods
    fun saveFcmToken(token: String) {
        try {
            sharedPreferences.edit().apply {
                putString(KEY_FCM_TOKEN, token)
                apply()
            }
            println("✅ FCM Token saved locally")
        } catch (e: Exception) {
            println("⚠️ Error saving FCM token: ${e.message}")
        }
    }

    fun getFcmToken(): String? = try {
        sharedPreferences.getString(KEY_FCM_TOKEN, null)
    } catch (e: Exception) {
        null
    }

    fun getUserId(): String? = try {
        sharedPreferences.getString(KEY_USER_ID, null)
    } catch (e: Exception) {
        null
    }

    fun getUserEmail(): String? = try {
        sharedPreferences.getString(KEY_USER_EMAIL, null)
    } catch (e: Exception) {
        null
    }

    fun getUserName(): String? = try {
        sharedPreferences.getString(KEY_USER_NAME, null)
    } catch (e: Exception) {
        null
    }

    fun isAdmin(): Boolean = try {
        sharedPreferences.getBoolean(KEY_IS_ADMIN, false)
    } catch (e: Exception) {
        false
    }

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun clearUserData() {
        try {
            sharedPreferences.edit().clear().apply()
        } catch (e: Exception) {
            println("⚠️ Error clearing user data: ${e.message}")
        }
    }

    fun getCurrentUser() = firebaseAuth.currentUser
}
