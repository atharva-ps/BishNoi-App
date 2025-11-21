package com.justbaat.mybishnoiapp.presentation.screens.profile

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.justbaat.mybishnoiapp.presentation.components.CoverImage
import com.justbaat.mybishnoiapp.presentation.components.ProfileImage
import com.justbaat.mybishnoiapp.utils.FileUtils
import com.justbaat.mybishnoiapp.utils.rememberImagePickerLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    isOwnProfile: Boolean, // ✅ Add this parameter
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Image picker launchers (only for own profile)
    val profilePhotoLauncher = rememberImagePickerLauncher { uri ->
        if (isOwnProfile) {
            val file = FileUtils.compressImage(context, uri, maxSizeKB = 500)
            file?.let { viewModel.uploadProfilePhoto(it) }
        }
    }

    val coverPhotoLauncher = rememberImagePickerLauncher { uri ->
        if (isOwnProfile) {
            val file = FileUtils.compressImage(context, uri, maxSizeKB = 1000)
            file?.let { viewModel.uploadCoverPhoto(it) }
        }
    }

    // Load profile on first composition
    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isOwnProfile) "My Profile" else "Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // ✅ Only show Settings and Edit for own profile
                    if (isOwnProfile) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                        IconButton(onClick = onNavigateToEditProfile) {
                            Icon(Icons.Default.Edit, "Edit Profile")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.profile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Cover Photo - ✅ Only editable for own profile
                    CoverImage(
                        imageUrl = uiState.profile?.coverPhoto,
                        onClick = if (isOwnProfile) {
                            { coverPhotoLauncher.launch("image/*") }
                        } else null, // ✅ No click for other users
                        showEditIcon = isOwnProfile, // ✅ Only show edit icon for own profile
                        isLoading = uiState.isUploadingCoverPhoto && isOwnProfile
                    )

                    // Profile Info Section
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Photo - ✅ Only editable for own profile
                        ProfileImage(
                            imageUrl = uiState.profile?.profilePhoto,
                            size = 120.dp,
                            onClick = if (isOwnProfile) {
                                { profilePhotoLauncher.launch("image/*") }
                            } else null, // ✅ No click for other users
                            showEditIcon = isOwnProfile, // ✅ Only show edit icon for own profile
                            isLoading = uiState.isUploadingProfilePhoto && isOwnProfile
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name
                        Text(
                            text = "${uiState.profile?.firstName} ${uiState.profile?.lastName}".trim()
                                .ifEmpty { "No name set" },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Username
                        if (uiState.profile?.username?.isNotEmpty() == true) {
                            Text(
                                text = "@${uiState.profile?.username}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // About Me
                        if (uiState.profile?.aboutMe?.isNotEmpty() == true) {
                            Text(
                                text = uiState.profile?.aboutMe ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                count = uiState.profile?.postsCount ?: 0,
                                label = "Posts"
                            )
                            StatItem(
                                count = uiState.profile?.followersCount ?: 0,
                                label = "Followers"
                            )
                            StatItem(
                                count = uiState.profile?.followingCount ?: 0,
                                label = "Following"
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Contact Info
                        ProfileInfoSection(
                            title = "Contact Information",
                            items = listOf(
                                "Email" to (uiState.profile?.email ?: ""),
                                "Mobile" to (uiState.profile?.mobileNumber?.ifEmpty { "Not set" } ?: "Not set"),
                                "Gender" to (uiState.profile?.gender?.ifEmpty { "Not set" } ?: "Not set"),
                                "Date of Birth" to (uiState.profile?.dob?.ifEmpty { "Not set" } ?: "Not set")
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileInfoSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                items.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
