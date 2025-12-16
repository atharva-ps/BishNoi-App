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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
