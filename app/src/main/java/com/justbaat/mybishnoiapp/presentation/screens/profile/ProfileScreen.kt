package com.justbaat.mybishnoiapp.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.justbaat.mybishnoiapp.presentation.components.BottomNavBar
import com.justbaat.mybishnoiapp.utils.FileUtils
import com.justbaat.mybishnoiapp.utils.rememberImagePickerLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    isOwnProfile: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
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

    // Load profile
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
                title = {
                    Text(
                        text = "@${uiState.profile?.username ?: "username"}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                        IconButton(onClick = onNavigateToEditProfile) {
                            Icon(Icons.Default.Edit, "Edit Profile")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedRoute = "profile",
                onHomeClick = onNavigateToHome,
                onCreatePostClick = { /* TODO */ },
                onProfileClick = { /* Already on profile */ }
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
                    // Cover Photo
                    CoverPhotoSection(
                        coverPhotoUrl = uiState.profile?.coverPhoto,
                        isOwnProfile = isOwnProfile,
                        isUploading = uiState.isUploadingCoverPhoto,
                        onClick = if (isOwnProfile) {
                            { coverPhotoLauncher.launch("image/*") }
                        } else null
                    )

                    // Profile Photo (overlapping cover)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-60).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfilePhotoSection(
                            profilePhotoUrl = uiState.profile?.profilePhoto,
                            isOwnProfile = isOwnProfile,
                            isUploading = uiState.isUploadingProfilePhoto,
                            onClick = if (isOwnProfile) {
                                { profilePhotoLauncher.launch("image/*") }
                            } else null
                        )
                    }

                    // User Info Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-40).dp)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Name
                        Text(
                            text = buildString {
                                val firstName = uiState.profile?.firstName?.trim() ?: ""
                                val lastName = uiState.profile?.lastName?.trim() ?: ""
                                append(firstName)
                                if (firstName.isNotEmpty() && lastName.isNotEmpty()) append(" ")
                                append(lastName)
                            }.ifEmpty { "No name set" },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        // About Me / Bio
                        if (!uiState.profile?.aboutMe.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.profile?.aboutMe ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats Row (Posts, Followers, Following - NO LIKES)
                        StatsRow(
                            postsCount = uiState.profile?.postsCount ?: 0,
                            followersCount = uiState.profile?.followersCount ?: 0,
                            followingCount = uiState.profile?.followingCount ?: 0
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Posts Section Header
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Posts Grid (Placeholder)
                    PostsGridPlaceholder()
                }
            }
        }
    }
}

@Composable
fun CoverPhotoSection(
    coverPhotoUrl: String?,
    isOwnProfile: Boolean,
    isUploading: Boolean,
    onClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (coverPhotoUrl != null) {
            AsyncImage(
                model = coverPhotoUrl,
                contentDescription = "Cover Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Edit FAB (only for own profile)
        if (isOwnProfile && onClick != null) {
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Cover",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProfilePhotoSection(
    profilePhotoUrl: String?,
    isOwnProfile: Boolean,
    isUploading: Boolean,
    onClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier.size(120.dp)
    ) {
        // Profile photo with golden border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 4.dp,
                    color = Color(0xFFFFD700), // Golden color
                    shape = CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            } else if (profilePhotoUrl != null) {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Settings, // Placeholder icon
                    contentDescription = "No Profile Photo",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Edit button (only for own profile)
        if (isOwnProfile && onClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsRow(
    postsCount: Int,
    followersCount: Int,
    followingCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(count = postsCount, label = "Posts")

        VerticalDivider(
            modifier = Modifier.height(40.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        StatItem(count = followersCount, label = "Followers")

        VerticalDivider(
            modifier = Modifier.height(40.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        StatItem(count = followingCount, label = "Following")
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PostsGridPlaceholder() {
    // Placeholder for posts grid (will be implemented later with real posts)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📸",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "No posts yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Posts will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
