package com.justbaat.mybishnoiapp.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.justbaat.mybishnoiapp.presentation.components.BottomNavBar
import com.justbaat.mybishnoiapp.presentation.components.StoryCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            // Custom Top Bar
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
                    // Logo/Brand Name
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
                        // Notifications
                        IconButton(onClick = { /* TODO: Navigate to notifications */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Search
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stories Section
            item {
                StoriesSection(
                    currentUserId = uiState.currentUser?.id,
                    currentUserName = uiState.currentUser?.name ?: "You",
                    currentUserPhoto = null,
                    onStoryClick = { userId ->
                        // TODO: Navigate to story view
                    },
                    onCreateStory = {
                        // TODO: Navigate to create story
                    }
                )
            }

            // Divider
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }

            // Feed Placeholder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🎉 Feed Coming Soon!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Posts from people you follow will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoriesSection(
    currentUserId: String?,
    currentUserName: String,
    currentUserPhoto: String?,
    onStoryClick: (String) -> Unit,
    onCreateStory: () -> Unit
) {
    Column {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Your Story (Create Story)
            item {
                StoryCircle(
                    imageUrl = currentUserPhoto,
                    name = currentUserName,
                    isViewed = false,
                    onClick = onCreateStory
                )
            }

            // Mock stories (will be replaced with real data later)
            items(getDummyStories()) { story ->
                StoryCircle(
                    imageUrl = story.imageUrl,
                    name = story.name,
                    isViewed = story.isViewed,
                    onClick = { onStoryClick(story.userId) }
                )
            }
        }
    }
}

// Dummy data for stories (temporary)
data class StoryItem(
    val userId: String,
    val name: String,
    val imageUrl: String?,
    val isViewed: Boolean
)

fun getDummyStories(): List<StoryItem> {
    return listOf(
        StoryItem("1", "Nacho", null, false),
        StoryItem("2", "Taylor", null, false),
        StoryItem("3", "Javier", null, true),
        StoryItem("4", "Rich", null, false),
        StoryItem("5", "Galen", null, true)
    )
}
