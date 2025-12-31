package com.app.bishnoi

import android.app.Application
import android.util.Log
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.utils.PlacesHelper
import com.app.bishnoi.utils.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BishNoiApplication : Application() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate() {
        super.onCreate()

        PlacesHelper.initialize(this)

        // ✅ Just get and save token locally, don't send to backend yet
        initializeFCM()
    }

    private fun initializeFCM() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FCM", "❌ Fetching FCM token failed", task.exception)
                        return@addOnCompleteListener
                    }

                    val token = task.result
                    Log.d("FCM", "🔔 FCM Token generated: ${token.take(30)}...")

                    // ✅ Only save locally for now
                    tokenManager.saveFcmToken(token)
                    Log.d("FCM", "✅ FCM Token saved locally")
                }
            } catch (e: Exception) {
                Log.e("FCM", "❌ Error initializing FCM: ${e.message}")
            }
        }
    }
}
