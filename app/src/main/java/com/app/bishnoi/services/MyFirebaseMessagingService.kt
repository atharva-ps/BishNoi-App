package com.app.bishnoi.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.bishnoi.MainActivity
import com.app.bishnoi.R
import com.app.bishnoi.utils.FcmTokenHelper
import com.app.bishnoi.utils.TokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var fcmTokenHelper: FcmTokenHelper

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "bishnoi_notifications"
        private const val CHANNEL_NAME = "BishNoi Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "========================================")
        Log.d(TAG, "🔔 NEW FCM TOKEN GENERATED")
        Log.d(TAG, "Token: ${token.take(30)}...")
        Log.d(TAG, "========================================")

        tokenManager.saveFcmToken(token)
        Log.d(TAG, "✅ Token saved locally")

        if (tokenManager.isLoggedIn()) {
            Log.d(TAG, "📤 Sending token to backend...")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    fcmTokenHelper.sendTokenToBackend()
                    Log.d(TAG, "✅ Token sent to backend successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to send token to backend: ${e.message}")
                }
            }
        } else {
            Log.d(TAG, "⚠️ User not logged in, token will be sent after login")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "========================================")
        Log.d(TAG, "📩 FCM MESSAGE RECEIVED")
        Log.d(TAG, "========================================")
        Log.d(TAG, "From: ${message.from}")
        Log.d(TAG, "Message ID: ${message.messageId}")
        Log.d(TAG, "Has Notification: ${message.notification != null}")
        Log.d(TAG, "Has Data: ${message.data.isNotEmpty()}")
        Log.d(TAG, "Data: ${message.data}")
        Log.d(TAG, "========================================")

        // Get notification type from data
        val notificationType = message.data["type"] ?: ""
        Log.d(TAG, "🏷️ Notification Type: $notificationType")

        // ✅ Route to appropriate handler based on type
        when (notificationType) {
            "news" -> {
                Log.d(TAG, "➡️ Routing to NEWS handler")
                handleNewsNotification(message)
            }
            "like", "comment", "follow" -> {
                Log.d(TAG, "➡️ Routing to REGULAR notification handler")
                handleRegularNotification(message)
            }
            else -> {
                Log.d(TAG, "⚠️ Unknown notification type, using default handler")
                handleRegularNotification(message)
            }
        }
    }

    /**
     * ✅ Handle NEWS notifications
     * - Opens external news link in browser
     * - Does NOT store in database
     * - Shows system notification only
     */
    /**
     * ✅ Handle NEWS notifications - Opens in WebView
     */
    private fun handleNewsNotification(message: RemoteMessage) {
        Log.d(TAG, "📰 Processing NEWS notification...")

        val messageData = message.data
        val newsSource = messageData["newsSource"] ?: "BishNoi News"
        val title = messageData["title"] ?: "New News Update"
        val newsLink = messageData["newsLink"] ?: ""
        val imageUrl = messageData["imageUrl"] ?: ""

        Log.d(TAG, "📰 News Source: $newsSource")
        Log.d(TAG, "📰 Title: ${title.take(100)}...")
        Log.d(TAG, "🔗 Link: $newsLink")

        // ✅ Create intent to open WebView in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "news")
            putExtra("news_link", newsLink)
            putExtra("news_title", title)
            putExtra("news_source", newsSource)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        createNotificationChannel()

        // Build notification
        val notificationTitle = message.notification?.title ?: "📰 New News Update"
        val notificationBody = message.notification?.body
            ?: "$newsSource: ${title.take(100)}${if (title.length > 100) "..." else ""}"

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        Log.d(TAG, "✅ News notification displayed")
    }


    /**
     * ✅ Handle REGULAR notifications (likes, comments, follows)
     * - Opens app and navigates to relevant screen
     * - Stored in database by backend
     * - Shows in app notification screen
     */
    private fun handleRegularNotification(message: RemoteMessage) {
        Log.d(TAG, "👤 Processing REGULAR notification...")

        val messageData = message.data
        val type = messageData["type"] ?: "unknown"

        Log.d(TAG, "Type: $type")
        Log.d(TAG, "Post ID: ${messageData["postId"]}")
        Log.d(TAG, "User ID: ${messageData["userId"]}")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", messageData["type"])
            putExtra("notification_id", messageData["notificationId"])
            putExtra("post_id", messageData["postId"])
            putExtra("user_id", messageData["userId"])
            putExtra("comment_id", messageData["commentId"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        createNotificationChannel()

        val title = message.notification?.title ?: messageData["title"] ?: "BishNoi"
        val body = message.notification?.body ?: messageData["body"] ?: ""

        Log.d(TAG, "📝 Title: $title")
        Log.d(TAG, "📝 Body: ${body.take(50)}...")

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        val notificationId = System.currentTimeMillis().toInt()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "✅ Regular notification displayed (ID: $notificationId)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to display notification: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Check if channel already exists
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel != null) {
                return // Channel already created
            }

            Log.d(TAG, "📢 Creating notification channel: $CHANNEL_ID")

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for likes, comments, follows, and news updates"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "✅ Notification channel created successfully")
        }
    }
}
