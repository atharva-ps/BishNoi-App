package com.app.bishnoi.utils

import android.util.Log
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenHelper @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: ApiService
) {

    suspend fun sendTokenToBackend() {
        try {
            // Get FCM token
            val fcmToken = tokenManager.getFcmToken() ?: run {
                // If not in storage, get fresh token
                val token = FirebaseMessaging.getInstance().token.await()
                tokenManager.saveFcmToken(token)
                token
            }

            Log.d("FCM", "📤 Sending FCM token to backend...")
            Log.d("FCM", "📱 Token: ${fcmToken.take(30)}...")

            val request = FcmTokenRequest(token = fcmToken)
            val response = apiService.updateFcmToken(request)

            if (response.isSuccessful) {
                Log.d("FCM", "✅ FCM Token sent to backend successfully")
            } else {
                Log.e("FCM", "❌ Failed to send FCM token: ${response.code()} - ${response.message()}")
                Log.e("FCM", "❌ Error body: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("FCM", "❌ Error sending FCM token: ${e.message}")
            e.printStackTrace()
        }
    }
}
