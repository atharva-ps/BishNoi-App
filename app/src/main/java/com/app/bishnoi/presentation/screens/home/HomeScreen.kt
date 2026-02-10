package com.app.bishnoi.presentation.screens.home

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.bishnoi.domain.model.Post
import com.app.bishnoi.presentation.components.BottomNavBar
import com.app.bishnoi.presentation.components.PostCard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.app.bishnoi.presentation.components.DeleteConfirmationDialog
import com.app.bishnoi.presentation.components.ReportBottomSheet
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import com.app.bishnoi.presentation.components.ShareCardGenerator
import com.app.bishnoi.presentation.components.ShareUtils
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.app.bishnoi.R
import com.justbaat.ads.sdk.AdSdkManager


private val BishnoiFont = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (Post) -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToSocial: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity


    val scope = rememberCoroutineScope()

    // ✅ Delete dialog state
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    // ✅ Report sheet state
    var postToReport by remember { mutableStateOf<Post?>(null) }

    // ✅ Share loading state
    var isSharing by remember { mutableStateOf(false) }

    // Show delete confirmation dialog
    postToDelete?.let { post ->
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deletePost(post.id)
                postToDelete = null
            },
            onDismiss = {
                postToDelete = null
            }
        )
    }
    // ✅ Show report bottom sheet
    postToReport?.let { post ->
        ReportBottomSheet(
            onDismiss = { postToReport = null },
            onSubmitReport = { reportType, message ->
                viewModel.reportPost(
                    post = post,
                    reportType = reportType,
                    message = message,
                    onSuccess = {
                        postToReport = null
                        Toast.makeText(
                            context,
                            "Report submitted successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            isLoading = false
        )
    }

    // ✅ Share loading indicator
    if (isSharing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    LaunchedEffect(Unit) {
        AdSdkManager.registerSdkReadyCallback {
            activity?.let {
                AdSdkManager.loadRewarded(
                    activity = it,
                    placementId = CREATE_POST_REWARDED_PLACEMENT_ID,
                    onAdLoaded = { Log.d("HomeScreen", "Rewarded Loaded") },
                    onAdFailed = { error -> Log.e("HomeScreen", "Rewarded Failed: $error") }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_bishnoi),
                        contentDescription = "BishNoi Logo",
                        modifier = Modifier
                            .height(32.dp), // Adjust height to match text size
                        contentScale = ContentScale.Fit
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onNavigateToMembers) {
                            Icon(
                                imageVector = Icons.Default.Group,  // or Icons.Default.Public or Icons.Default.Groups
                                contentDescription = "Member",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (activity == null) {
                                    onNavigateToSearch()
                                    return@IconButton
                                }

                                AdSdkManager.showRewardedAd(
                                    activity = activity,
                                    placementId = CREATE_POST_REWARDED_PLACEMENT_ID,
                                    enableClickCounting = false,
                                    threshold = 0,
                                    onUserEarnedReward = {
                                        // Reward can be handled here if needed later.
                                    },
                                    onAdDismissed = {
                                        onNavigateToSearch()
                                        AdSdkManager.loadRewarded(
                                            activity = activity,
                                            placementId = CREATE_POST_REWARDED_PLACEMENT_ID
                                        )
                                    },
                                    onAdFailedToShow = { error ->
                                        Log.e("HomeScreen", "Rewarded failed: $error")
                                        onNavigateToSearch()
                                        AdSdkManager.loadRewarded(
                                            activity = activity,
                                            placementId = CREATE_POST_REWARDED_PLACEMENT_ID
                                        )
                                    }
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomNavBar(
                selectedRoute = "home",
                onHomeClick = { /* Already on home */ },
                onNewsClick = onNavigateToNews,
                onSocialClick = onNavigateToSocial,
                onCreatePostClick = {
                    if (activity == null) {
                        onNavigateToCreatePost()
                        return@BottomNavBar
                    }

                    AdSdkManager.showRewardedAd(
                        activity = activity,
                        placementId = CREATE_POST_REWARDED_PLACEMENT_ID,
                        enableClickCounting = false,
                        threshold = 0,
                        onUserEarnedReward = {
                            // Reward can be handled here if needed later.
                        },
                        onAdDismissed = {
                            onNavigateToCreatePost()
                            AdSdkManager.loadRewarded(
                                activity = activity,
                                placementId = CREATE_POST_REWARDED_PLACEMENT_ID
                            )
                        },
                        onAdFailedToShow = { error ->
                            Log.e("HomeScreen", "Rewarded failed: $error")
                            onNavigateToCreatePost()
                            AdSdkManager.loadRewarded(
                                activity = activity,
                                placementId = CREATE_POST_REWARDED_PLACEMENT_ID
                            )
                        }
                    )
                },
                onProfileClick = {
                    uiState.currentUser?.id?.let { onNavigateToProfile(it) }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading && uiState.feedPosts.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.feedPosts.isEmpty() -> {
                    EmptyFeedPlaceholder(uiState)
                }
                else -> {
                    HomeFeed(
                        posts = uiState.feedPosts,
                        currentUserId = uiState.currentUser?.id,
                        onUserClick = onNavigateToProfile,
                        onToggleLike = { post -> viewModel.toggleLike(post) },  // ✅ Add this
                        onPostClick = onNavigateToPostDetail,
                        onDeleteClick = { post -> postToDelete = post },  // ✅ Show dialog
                        onReportClick = { post -> postToReport = post }
//                        onShareClick = { post ->
//                            scope.launch {
//                                isSharing = true
//                                try {
//                                    val profile = uiState.currentUserProfile
//
//                                    if (profile == null) {
//                                        Toast.makeText(
//                                            context,
//                                            "Loading profile...",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                        isSharing = false
//                                        return@launch
//                                    }
//
//                                    // Build full name
//                                    val fullName = buildString {
//                                        if (profile.firstName.isNotBlank()) {
//                                            append(profile.firstName)
//                                        }
//                                        if (profile.lastName.isNotBlank()) {
//                                            if (isNotEmpty()) append(" ")
//                                            append(profile.lastName)
//                                        }
//                                    }.ifEmpty { profile.username }
//
//                                    // Get designation
//                                    val designation = profile.professionalDetails.designation
//                                        .takeIf { it.isNotBlank() }
//
//                                    // Get city
//                                    val city = profile.address.current.city
//                                        .takeIf { it.isNotBlank() }
//
//                                    // Get state
//                                    val state = profile.address.current.state
//                                        .takeIf { it.isNotBlank() }
//
//                                    val imageFile = ShareCardGenerator.generateShareCard(
//                                        context = context,
//                                        postImageUrl = post.imageUrl ?: "",
//                                        userProfileUrl = profile.profilePhoto,
//                                        userName = fullName,
//                                        userDesignation = designation,
//                                        userCity = city,
//                                        postFormat = post.format,
//                                        userState = state
//                                    )
//
//                                    if (imageFile != null) {
//                                        ShareUtils.shareImage(
//                                            context = context,
//                                            imageFile = imageFile,
//                                            text = "Check out this post on BishNoi! ${post.caption}"
//                                        )
//                                    } else {
//                                        Toast.makeText(
//                                            context,
//                                            "Failed to create share card",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                } catch (e: Exception) {
//                                    Toast.makeText(
//                                        context,
//                                        "Error: ${e.message}",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                } finally {
//                                    isSharing = false
//                                }
//                            }
//                        }
                    )
                }
            }
        }
    }
}

private const val CREATE_POST_REWARDED_PLACEMENT_ID = "rewarded_placement"

@Composable
private fun HomeFeed(
    posts: List<Post>,
    currentUserId: String?,
    onUserClick: (String) -> Unit,
    onPostClick: (Post) -> Unit,
    onToggleLike: (Post) -> Unit,  // ✅ Add this
    onDeleteClick: (Post) -> Unit,
    onReportClick: (Post) -> Unit,
//    onShareClick: (Post) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                currentUserId = currentUserId,  // ✅ Pass current user id
                onUserClick = onUserClick,
                onLikeClick = { onToggleLike(post) },  // ✅ Wire this
                onCommentsClick = { onPostClick(post) },
                onDeleteClick = { onDeleteClick(post) },  // ✅ Pass delete callback
                onReportClick = { onReportClick(post) },
//                onShareClick = { onShareClick(post) }
            )
        }
    }
}


@Composable
private fun EmptyFeedPlaceholder(uiState: HomeUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, start = 32.dp, end = 32.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🎉 Welcome to BishNoi!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your feed will appear here soon",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    uiState.currentUser?.let { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Logged in as",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

