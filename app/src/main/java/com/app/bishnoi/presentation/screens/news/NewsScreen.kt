package com.app.bishnoi.presentation.screens.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.bishnoi.domain.model.News
import androidx.compose.animation.core.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel(),
    onNavigateToWebView: (url: String, title: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            // ✅ Category tabs
            if (uiState.allCategories.isNotEmpty()) {
                CategoryTabRow(
                    categories = uiState.allCategories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.selectCategory(category)
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // ✅ Use filteredNewsList instead of newsList
                uiState.isLoading && uiState.filteredNewsList.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.filteredNewsList.isNotEmpty() -> {
                    val pagerState = rememberPagerState(pageCount = { uiState.filteredNewsList.size })

                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        NewsCard(
                            newsItem = uiState.filteredNewsList[page],
                            onTitleClick = { externalLink ->
                                externalLink?.let {
                                    onNavigateToWebView(it, uiState.filteredNewsList[page].title)
                                }
                            },
                            onShareClick = { newsItem ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, newsItem.title)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${newsItem.title}\n\n${newsItem.externalLink}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            },
                            showSwipeHint = page == 0
                        )
                    }

                    // Load more when reaching end (only for "My Feed")
                    LaunchedEffect(pagerState.currentPage) {
                        if (uiState.selectedCategory == "My Feed" &&
                            pagerState.currentPage >= uiState.filteredNewsList.size - 3) {
                            viewModel.loadNextPage()
                        }
                    }
                }
                uiState.filteredNewsList.isEmpty() && !uiState.isLoading -> {
                    // ✅ Empty state for category
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📰",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No news in ${uiState.selectedCategory}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try selecting a different category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadNews(refresh = true) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

// ✅ Category Tab Row
@Composable
fun CategoryTabRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                CategoryTab(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

// ✅ Individual Category Tab
@Composable
fun CategoryTab(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun NewsCard(
    newsItem: News,
    onTitleClick: (String?) -> Unit,
    onShareClick: (News) -> Unit,
    showSwipeHint: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Featured Image with overlay - FIXED HEIGHT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp) // Slightly reduced to give more space to content
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                // Image with proper content scale
                AsyncImage(
                    model = newsItem.imageUrl,
                    contentDescription = newsItem.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Enhanced gradient overlay for better text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.5f)
                                ),
                                startY = 0f,
                                endY = 1000f
                            )
                        )
                )

                // Top bar with more options
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .align(Alignment.TopCenter),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
                    // Source badge
//                    Surface(
//                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
//                        shape = RoundedCornerShape(20.dp),
//                        shadowElevation = 4.dp
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
//                        ) {
//                            Surface(
//                                color = MaterialTheme.colorScheme.primary,
//                                shape = CircleShape,
//                                modifier = Modifier.size(6.dp)
//                            ) {}
//
//                            Spacer(Modifier.width(6.dp))
//
//                            Text(
//                                text = "LIVE",
//                                style = MaterialTheme.typography.labelSmall,
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 10.sp,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }

                    // More options button with better visibility
//                    Surface(
//                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
//                        shape = CircleShape
//                    ) {
//                        IconButton(onClick = { /* Show options menu */ }) {
//                            Icon(
//                                Icons.Default.MoreVert,
//                                contentDescription = "More options",
//                                tint = MaterialTheme.colorScheme.onSurface
//                            )
//                        }
//                    }
//                }

                // Category chips at bottom of image
                if (newsItem.categories.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        newsItem.categories.take(3).forEach { category ->
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(20.dp),
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = category.uppercase(),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Content Section with scrollable content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes remaining space
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Source header with actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    // Brand logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "in",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "stream",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Bookmark
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        ) {
                            IconButton(
                                onClick = { /* Bookmark logic */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Share
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        ) {
                            IconButton(
                                onClick = { onShareClick(newsItem) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Title - Clickable with better typography
                Text(
                    text = newsItem.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable { onTitleClick(newsItem.externalLink) }
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Description with better spacing
                Text(
                    text = newsItem.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(Modifier.weight(1f))

                // Metadata footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time and source
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${newsItem.publishedTime} • ${newsItem.source}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Animated swipe hint that disappears after 3 seconds
        if (showSwipeHint) {
            AnimatedSwipeHint()
        }
    }
}

@Composable
fun AnimatedSwipeHint() {
    var isVisible by remember { mutableStateOf(true) }

    // Animation for upward movement
    val infiniteTransition = rememberInfiniteTransition(label = "swipe_animation")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_animation"
    )

    // Fade out animation
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha_animation"
    )

    // Hide after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        isVisible = false
    }

    if (alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .alpha(alpha),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "↑",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Swipe up",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}