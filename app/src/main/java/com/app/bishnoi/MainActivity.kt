package com.app.bishnoi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.presentation.components.UpdateDialog
import com.app.bishnoi.presentation.navigation.NavGraph
import com.app.bishnoi.presentation.navigation.Screen
import com.app.bishnoi.ui.theme.BishNoiTheme
import com.app.bishnoi.utils.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var apiService: ApiService

    // ✅ ADD THIS: Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("✅ Notification permission granted")
        } else {
            println("⚠️ Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Get news link from notification intent
        val newsLink = intent.getStringExtra("news_link")
        val newsTitle = intent.getStringExtra("news_title")
        val notificationType = intent.getStringExtra("notification_type")

        Log.d(TAG, "========================================")
        Log.d(TAG, "MainActivity onCreate")
        Log.d(TAG, "========================================")
        Log.d(TAG, "notification_type: $notificationType")
        Log.d(TAG, "news_link: $newsLink")
        Log.d(TAG, "news_title: $newsTitle")
        Log.d(TAG, "========================================")

        // ✅ Request notification permission for Android 13+
        requestNotificationPermission()
        setContent {
            BishNoiTheme {
                var showUpdateDialog by remember { mutableStateOf(false) }
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

                // Check for updates on app start
                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        checkForUpdates { info ->
                            if (info.needsUpdate) {
                                updateInfo = info
                                showUpdateDialog = true
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val isLoggedIn = remember { tokenManager.isLoggedIn() }

                    val startDestination = if (isLoggedIn) {
                        Screen.MainGraph.route
                    } else {
                        Screen.AuthGraph.route
                    }

                    // ✅ Navigate to WebView if notification was clicked
                    LaunchedEffect(newsLink) {
                        if (newsLink != null && notificationType == "news") {
                            Log.d(TAG, "📰 Navigating to WebView: $newsLink")
                            // Wait for navigation to be ready
                            kotlinx.coroutines.delay(500)

                            val encodedUrl = Uri.encode(newsLink)
                            val encodedTitle = Uri.encode(newsTitle ?: "News")
                            navController.navigate("webview/$encodedUrl/$encodedTitle")
                        }
                    }

                    NavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        tokenManager = tokenManager
                    )

                    // Show update dialog
                    if (showUpdateDialog && updateInfo != null) {
                        UpdateDialog(
                            latestVersion = updateInfo!!.latestVersion,
                            updateMessage = updateInfo!!.updateMessage,
                            playStoreUrl = updateInfo!!.playStoreUrl,
                            isForceUpdate = updateInfo!!.forceUpdate,
                            onDismiss = { showUpdateDialog = false }
                        )
                    }
                }
            }
        }
    }

    // ✅ Handle notification when app is already running
    override fun onNewIntent(intent: Intent) {  // ✅ Remove the ? (non-nullable)
        super.onNewIntent(intent)
        setIntent(intent)

        val newsLink = intent.getStringExtra("news_link")
        val newsTitle = intent.getStringExtra("news_title")
        val notificationType = intent.getStringExtra("notification_type")

        Log.d(TAG, "========================================")
        Log.d(TAG, "MainActivity onNewIntent")
        Log.d(TAG, "========================================")
        Log.d(TAG, "notification_type: $notificationType")
        Log.d(TAG, "news_link: $newsLink")
        Log.d(TAG, "news_title: $newsTitle")
        Log.d(TAG, "========================================")

        // TODO: Handle navigation when app is already open
        // You can use a shared event or ViewModel to trigger navigation
    }


    // ✅ ADD THIS FUNCTION
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    println("✅ Notification permission already granted")
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private suspend fun checkForUpdates(onResult: (UpdateInfo) -> Unit) {
        try {
            // Get current app version (handle null safely)
            val currentVersion = packageManager
                .getPackageInfo(packageName, 0)
                .versionName ?: "1.0.0"  // ✅ fallback value

            // Call API
            val response = apiService.checkVersion(currentVersion)  // ✅ now String, not String?

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!
                onResult(
                    UpdateInfo(
                        needsUpdate = data.needsUpdate,
                        forceUpdate = data.forceUpdate,
                        latestVersion = data.latestVersion,
                        updateMessage = data.updateMessage,
                        playStoreUrl = data.playStoreUrl
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    data class UpdateInfo(
        val needsUpdate: Boolean,
        val forceUpdate: Boolean,
        val latestVersion: String,
        val updateMessage: String,
        val playStoreUrl: String
    )
}
