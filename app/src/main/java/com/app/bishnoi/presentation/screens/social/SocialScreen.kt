package com.app.bishnoi.presentation.screens.social

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.bishnoi.R
import com.app.bishnoi.domain.model.Post
import com.app.bishnoi.presentation.components.BottomNavBar
import com.app.bishnoi.presentation.components.DeleteConfirmationDialog
import com.app.bishnoi.presentation.components.PostCard
import com.app.bishnoi.presentation.components.ReportBottomSheet
import com.app.bishnoi.presentation.components.ShareCardGenerator
import com.app.bishnoi.presentation.components.ShareUtils
import kotlinx.coroutines.launch

private val BishnoiFont = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    onNavigateBack: () -> Unit,  // ✅ Changed from onNavigateToHome
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPostDetail: (Post) -> Unit,
    viewModel: SocialViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val playStoreUrl = "https://play.google.com/store/apps/details?id=com.app.bishnoi"

    val scope = rememberCoroutineScope()

    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var postToReport by remember { mutableStateOf<Post?>(null) }

    // ✅ Share loading state
    var isSharing by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Social",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                uiState.isLoading && uiState.socialPosts.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.socialPosts.isEmpty() -> {
                    EmptySocialFeed()
                }
                else -> {
                    SocialFeed(
                        posts = uiState.socialPosts,
                        currentUserId = uiState.currentUser?.id,
                        onUserClick = onNavigateToProfile,
                        onToggleLike = { post -> viewModel.toggleLike(post) },
                        onPostClick = onNavigateToPostDetail,
                        onDeleteClick = { post -> postToDelete = post },
                        onReportClick = { post -> postToReport = post },
                        onShareClick = { post ->
                            scope.launch {
                                isSharing = true
                                try {
                                    val profile = uiState.currentUserProfile

                                    if (profile == null) {
                                        Toast.makeText(
                                            context,
                                            "Loading profile...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isSharing = false
                                        return@launch
                                    }

                                    // Build full name
                                    val fullName = buildString {
                                        if (profile.firstName.isNotBlank()) {
                                            append(profile.firstName)
                                        }
                                        if (profile.lastName.isNotBlank()) {
                                            if (isNotEmpty()) append(" ")
                                            append(profile.lastName)
                                        }
                                    }.ifEmpty { profile.username }

                                    // Get designation
                                    val designation = profile.professionalDetails.designation
                                        .takeIf { it.isNotBlank() }

                                    // Get city
                                    val city = profile.address.current.city
                                        .takeIf { it.isNotBlank() }

                                    // Get state
                                    val state = profile.address.current.state
                                        .takeIf { it.isNotBlank() }

                                    val imageFile = ShareCardGenerator.generateShareCard(
                                        context = context,
                                        postImageUrl = post.imageUrl ?: "",
                                        userProfileUrl = profile.profilePhoto,
                                        userName = fullName,
                                        userDesignation = designation,
                                        userCity = city,
                                        postFormat = post.format,
                                        userState = state
                                    )

                                    if (imageFile != null) {
                                        ShareUtils.shareImage(
                                            context = context,
                                            imageFile = imageFile,
                                            text = "बिश्नोई समाज से जुड़ने के लिए अभी बिश्नोई ऐप डाउनलोड करें\n$playStoreUrl"
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to create share card",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    isSharing = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialFeed(
    posts: List<Post>,
    currentUserId: String?,
    onUserClick: (String) -> Unit,
    onPostClick: (Post) -> Unit,
    onToggleLike: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onReportClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                currentUserId = currentUserId,
                showShareButton = true,
                onUserClick = onUserClick,
                onLikeClick = { onToggleLike(post) },
                onCommentsClick = { onPostClick(post) },
                onDeleteClick = { onDeleteClick(post) },
                onReportClick = { onReportClick(post) },
                onShareClick = { onShareClick(post) }
            )
        }
    }
}

@Composable
private fun EmptySocialFeed() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📢 No Social Posts Yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Admin posts marked as Social will appear here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
