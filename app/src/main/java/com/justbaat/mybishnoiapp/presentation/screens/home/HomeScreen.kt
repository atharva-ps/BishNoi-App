package com.justbaat.mybishnoiapp.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.justbaat.mybishnoiapp.domain.model.Post
import com.justbaat.mybishnoiapp.presentation.components.BottomNavBar
import com.justbaat.mybishnoiapp.presentation.components.PostCard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.justbaat.mybishnoiapp.presentation.components.DeleteConfirmationDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (Post) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ Delete dialog state
    var postToDelete by remember { mutableStateOf<Post?>(null) }

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
                    Text(
                        text = "BishNoi",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { /* TODO: notifications */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = onNavigateToSearch) {
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
                onCreatePostClick = onNavigateToCreatePost,
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
                        onDeleteClick = { post -> postToDelete = post }  // ✅ Show dialog
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFeed(
    posts: List<Post>,
    currentUserId: String?,
    onUserClick: (String) -> Unit,
    onPostClick: (Post) -> Unit,
    onToggleLike: (Post) -> Unit,  // ✅ Add this
    onDeleteClick: (Post) -> Unit
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
                onDeleteClick = { onDeleteClick(post) }  // ✅ Pass delete callback
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

