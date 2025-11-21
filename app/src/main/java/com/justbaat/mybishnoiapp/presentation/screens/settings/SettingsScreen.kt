package com.justbaat.mybishnoiapp.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var isProfilePublic by remember { mutableStateOf(true) }
    var showEmail by remember { mutableStateOf(true) }
    var showMobile by remember { mutableStateOf(false) }
    var showAddress by remember { mutableStateOf(false) }
    var allowMessagesFromAnyone by remember { mutableStateOf(true) }
    var showOnlineStatus by remember { mutableStateOf(true) }

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
        }
    ) { padding ->
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
                checked = isProfilePublic,
                onCheckedChange = { isProfilePublic = it }
            )

            Divider()

            Text(
                text = "What Others Can See",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            SettingItem(
                title = "Email Address",
                description = "Show email on your profile",
                checked = showEmail,
                onCheckedChange = { showEmail = it }
            )

            SettingItem(
                title = "Mobile Number",
                description = "Show mobile number on your profile",
                checked = showMobile,
                onCheckedChange = { showMobile = it }
            )

            SettingItem(
                title = "Address",
                description = "Show address on your profile",
                checked = showAddress,
                onCheckedChange = { showAddress = it }
            )

            Divider()

            Text(
                text = "Communication",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            SettingItem(
                title = "Messages from Anyone",
                description = "Allow anyone to send you messages",
                checked = allowMessagesFromAnyone,
                onCheckedChange = { allowMessagesFromAnyone = it }
            )

            SettingItem(
                title = "Online Status",
                description = "Show when you're online",
                checked = showOnlineStatus,
                onCheckedChange = { showOnlineStatus = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    // TODO: Save settings to backend
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
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
                style = MaterialTheme.typography.bodyLarge
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
