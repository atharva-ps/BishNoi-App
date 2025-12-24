package com.app.bishnoi.presentation.screens.createpost

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.app.bishnoi.utils.FileUtils
import com.app.bishnoi.utils.rememberImagePickerLauncher
import java.io.File
import com.yalantis.ucrop.UCrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    // 1️⃣ Crop result launcher (MUST come first)
    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri =
                UCrop.getOutput(result.data!!) ?: return@rememberLauncherForActivityResult
            val file = FileUtils.compressImage(context, croppedUri, 1200)
            viewModel.setImageFile(file)
        }
    }

    // 2️⃣ Image picker launcher
    val imagePickerLauncher = rememberImagePickerLauncher { sourceUri ->

        val destinationUri = Uri.fromFile(
            File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
        )

        val isVertical = uiState.format == PostFormat.VERTICAL

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(if (isVertical) 4f else 191f, if (isVertical) 5f else 100f)
            .withMaxResultSize(if (isVertical) 1080 else 1080, if (isVertical) 1350 else 566)

        cropLauncher.launch(uCrop.getIntent(context))
    }


    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createPost {
                                onPostCreated()
                            }
                        },
                        enabled = !uiState.isUploading
                    ) {
                        if (uiState.isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Post",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        if (uiState.format == PostFormat.VERTICAL) 4f / 5f else 1.91f / 1f
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.selectedImage != null) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.selectedImage),
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Tap to select an image",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Transparent button over whole box
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0f))
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caption
            OutlinedTextField(
                value = uiState.caption,
                onValueChange = viewModel::setCaption,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text("Write a caption...") },
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visibility
            Text(
                text = "Who can see this post?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = uiState.visibility == PostVisibility.FOLLOWERS,
                    onClick = { viewModel.setVisibility(PostVisibility.FOLLOWERS) },
                    label = { Text("Followers") }
                )
                FilterChip(
                    selected = uiState.visibility == PostVisibility.PUBLIC,
                    onClick = { viewModel.setVisibility(PostVisibility.PUBLIC) },
                    label = { Text("Public") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = uiState.format == PostFormat.VERTICAL,
                    onClick = { viewModel.setFormat(PostFormat.VERTICAL) },
                    label = { Text("Vertical") }
                )
                FilterChip(
                    selected = uiState.format == PostFormat.HORIZONTAL,
                    onClick = { viewModel.setFormat(PostFormat.HORIZONTAL) },
                    label = { Text("Horizontal") }
                )
            }
            // Inside CreatePostScreen Column, after format chips, add:

            if (uiState.isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setIsSocial(!uiState.isSocial) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.isSocial,
                        onCheckedChange = { viewModel.setIsSocial(it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Post to Social Feed",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "This post will appear in the Social tab instead of Home",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
