package com.app.bishnoi.presentation.screens.news

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import com.justbaat.ads.sdk.AdSdkManager
import com.justbaat.ads.sdk.AdSdkManager.showBanner
import com.justbaat.ads.sdk.AdSdkManager.showInterstitialAd


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel(),
    onNavigateToWebView: (url: String, title: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    var lastAdShownIndex by remember { mutableIntStateOf(0) }
    val rewardedPrefs = remember {
        context.getSharedPreferences(REWARDED_PREFS, Context.MODE_PRIVATE)
    }
    var lastRewardedShownAt by remember {
        mutableLongStateOf(rewardedPrefs.getLong(REWARDED_LAST_SHOWN_KEY, 0L))
    }
    var isRewardedShowing by remember { mutableStateOf(false) }

    // Load Ad Initially
    LaunchedEffect(Unit) {
        AdSdkManager.registerSdkReadyCallback {
            Log.d("NewsScreen", "SDK is ready, now loading Interstitial...")
            activity?.let {
                AdSdkManager.loadInterstitialAd(
                    activity = it,
                    placementId = "interstitial_placement",
                    onAdLoaded = { Log.d("NewsScreen", "Interstitial Loaded") },
                    onAdFailed = { Log.e("NewsScreen", "Interstitial Failed: $it") }
                )
            }
            activity?.let {
                AdSdkManager.loadRewarded(
                    activity = it,
                    placementId = REWARDED_PLACEMENT_ID,
                    onAdLoaded = { Log.d("NewsScreen", "Rewarded Loaded") },
                    onAdFailed = { error -> Log.e("NewsScreen", "Rewarded Failed: $error") }
                )
            }
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
                                externalLink?.let { link ->
                                    val title = uiState.filteredNewsList[page].title
                                    val now = System.currentTimeMillis()
                                    val isCooldownActive = now - lastRewardedShownAt < REWARDED_COOLDOWN_MILLIS
                                    val navigateToNews = {
                                        onNavigateToWebView(link, title)
                                    }

                                    if (activity == null || isCooldownActive || isRewardedShowing) {
                                        navigateToNews()
                                    } else {
                                        isRewardedShowing = true
                                        var rewardRecorded = false
                                        AdSdkManager.showRewardedAd(
                                            activity = activity,
                                            placementId = REWARDED_PLACEMENT_ID,
                                            enableClickCounting = false,
                                            threshold = 0,
                                            onUserEarnedReward = {
                                                if (!rewardRecorded) {
                                                    rewardRecorded = true
                                                    val completedAt = System.currentTimeMillis()
                                                    rewardedPrefs.edit()
                                                        .putLong(REWARDED_LAST_SHOWN_KEY, completedAt)
                                                        .apply()
                                                    lastRewardedShownAt = completedAt
                                                }
                                            },
                                            onAdDismissed = {
                                                if (!rewardRecorded) {
                                                    val completedAt = System.currentTimeMillis()
                                                    rewardedPrefs.edit()
                                                        .putLong(REWARDED_LAST_SHOWN_KEY, completedAt)
                                                        .apply()
                                                    lastRewardedShownAt = completedAt
                                                }
                                                isRewardedShowing = false
                                                navigateToNews()
                                                AdSdkManager.loadRewarded(
                                                    activity = activity,
                                                    placementId = REWARDED_PLACEMENT_ID
                                                )
                                            },
                                            onAdFailedToShow = { error ->
                                                Log.e("NewsScreen", "Rewarded failed: $error")
                                                isRewardedShowing = false
                                                navigateToNews()
                                                AdSdkManager.loadRewarded(
                                                    activity = activity,
                                                    placementId = REWARDED_PLACEMENT_ID
                                                )
                                            }
                                        )
                                    }
                                }
                            },
                            onShareClick = { newsItem ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, newsItem.title)

                                    // ✅ Enhanced share message
                                    val shareLink = newsItem.link ?: newsItem.externalLink ?: ""
                                    val shareMessage = """
                                        ${newsItem.title}
                                        ${newsItem.description.take(150)}...
                                        Read more: $shareLink 
                                        
                                        📱Get BishNoi App for latest news https://play.google.com/store/apps/details?id=com.app.bishnoi """.trimIndent()
                                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            },
                            showSwipeHint = page == 0
                        )
                    }

                    // Load more when reaching end (only for "My Feed")
                    LaunchedEffect(pagerState.currentPage) {
                        val currentIndex = pagerState.currentPage
                        if (currentIndex > 0 && currentIndex % 3 == 0 && currentIndex > lastAdShownIndex) {
                            lastAdShownIndex = currentIndex

                            // This will show the ad ONLY if it successfully loaded above
                            activity?.let {
                                showInterstitialAd(
                                    activity = it,
                                    placementId = "interstitial_placement",
                                    enableClickCounting = true,
                                    threshold = 1,
                                    onAdDismissed = {
                                        // Reload for next time
                                        AdSdkManager.loadInterstitialAd(it, "interstitial_placement")
                                    }
                                )
                            }
                        }

                        // Load More Data Logic
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
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                AsyncImage(
                    model = newsItem.imageUrl,
                    contentDescription = newsItem.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark Gradient Overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                // Category Chips
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
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
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.weight(1f))

                    // Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { /* Bookmark */ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { onShareClick(newsItem) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Title - Clickable with better typography
                Text(
                    text = newsItem.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable { onTitleClick(newsItem.externalLink) }
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Description - Strictly limited to 4 lines
                Text(
                    text = newsItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Spacer pushes footer to the bottom of this weighted column
                Spacer(Modifier.weight(0.5f))

                // Metadata Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${newsItem.publishedTime} • ${newsItem.source}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (activity != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                this.tag = "banner_container_first"
                                showBanner(
                                    activity = activity,
                                    parent = this,
                                    onAdLoaded = { Log.d("NewsCard","Ad loaded") },
                                    onAdFailed = { Log.d("NewsCard","Ad failed: $it") }
                                )
                            }
                        }
                    )
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

private const val REWARDED_PREFS = "news_rewarded_ad_prefs"
private const val REWARDED_LAST_SHOWN_KEY = "last_rewarded_shown_at"
private const val REWARDED_COOLDOWN_MILLIS = 15 * 60 * 1000L
private const val REWARDED_PLACEMENT_ID = "rewarded_placement"