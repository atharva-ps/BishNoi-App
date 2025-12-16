package com.app.bishnoi.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.bishnoi.presentation.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Navigate on successful logout
    LaunchedEffect(homeUiState.isLoggedOut) {
        if (homeUiState.isLoggedOut) {
            onLogout()
        }
    }

    // Show success message
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(settingsUiState.saveSuccess) {
        if (settingsUiState.saveSuccess) {
            settingsUiState.successMessage?.let {
                snackbarHostState.showSnackbar(it)
            }
            settingsViewModel.clearSuccess()
        }
    }

    // Show error message
    LaunchedEffect(settingsUiState.error) {
        settingsUiState.error?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (settingsUiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Privacy Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Profile Visibility
                SettingItem(
                    title = "Public Profile",
                    description = "Allow anyone to view your profile",
                    checked = settingsUiState.isProfilePublic,
                    onCheckedChange = {
                        settingsViewModel.updatePrivacySetting(isProfilePublic = it)
                    }
                )

                HorizontalDivider()

                Text(
                    text = "What Others Can See",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                SettingItem(
                    title = "Email Address",
                    description = "Show email on your profile",
                    checked = settingsUiState.showEmail,
                    onCheckedChange = {
                        settingsViewModel.updatePrivacySetting(showEmail = it)
                    }
                )

                SettingItem(
                    title = "Mobile Number",
                    description = "Show mobile number on your profile",
                    checked = settingsUiState.showMobile,
                    onCheckedChange = {
                        settingsViewModel.updatePrivacySetting(showMobile = it)
                    }
                )

                SettingItem(
                    title = "Address",
                    description = "Show address on your profile",
                    checked = settingsUiState.showAddress,
                    onCheckedChange = {
                        settingsViewModel.updatePrivacySetting(showAddress = it)
                    }
                )

                HorizontalDivider()

                Text(
                    text = "Communication",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                SettingItem(
                    title = "Messages from Anyone",
                    description = "Allow anyone to send you messages",
                    checked = settingsUiState.allowMessagesFromAnyone,
                    onCheckedChange = {
                        settingsViewModel.updateCommunicationSetting(allowMessagesFromAnyone = it)
                    }
                )

                SettingItem(
                    title = "Online Status",
                    description = "Show when you're online",
                    checked = settingsUiState.showOnlineStatus,
                    onCheckedChange = {
                        settingsViewModel.updateCommunicationSetting(showOnlineStatus = it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        settingsViewModel.saveSettings()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !settingsUiState.isSaving
                ) {
                    if (settingsUiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save Settings")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Account Section
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Logout Button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !homeUiState.isLoggingOut
                ) {
                    if (homeUiState.isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logging out...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to logout?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        homeViewModel.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
