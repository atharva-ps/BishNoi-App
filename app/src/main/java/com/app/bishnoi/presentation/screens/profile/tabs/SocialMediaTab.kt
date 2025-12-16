package com.app.bishnoi.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.bishnoi.presentation.components.ProfileTextField
import com.app.bishnoi.utils.ValidationHelper

@Composable
fun SocialMediaTab(
    viewModel: EditProfileViewModel,
    uiState: EditProfileUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Social Media Links",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Connect your social media profiles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ProfileTextField(
            value = uiState.socialMedia.instagram,
            onValueChange = {
                viewModel.updateSocialMedia(
                    uiState.socialMedia.copy(instagram = it)
                )
            },
            label = "Instagram",
            placeholder = "https://instagram.com/username",
            errorMessage = ValidationHelper.validateUrl(uiState.socialMedia.instagram)
        )

        ProfileTextField(
            value = uiState.socialMedia.facebook,
            onValueChange = {
                viewModel.updateSocialMedia(
                    uiState.socialMedia.copy(facebook = it)
                )
            },
            label = "Facebook",
            placeholder = "https://facebook.com/username",
            errorMessage = ValidationHelper.validateUrl(uiState.socialMedia.facebook)
        )

        ProfileTextField(
            value = uiState.socialMedia.twitter,
            onValueChange = {
                viewModel.updateSocialMedia(
                    uiState.socialMedia.copy(twitter = it)
                )
            },
            label = "Twitter/X",
            placeholder = "https://twitter.com/username",
            errorMessage = ValidationHelper.validateUrl(uiState.socialMedia.twitter)
        )

        ProfileTextField(
            value = uiState.socialMedia.linkedin,
            onValueChange = {
                viewModel.updateSocialMedia(
                    uiState.socialMedia.copy(linkedin = it)
                )
            },
            label = "LinkedIn",
            placeholder = "https://linkedin.com/in/username",
            errorMessage = ValidationHelper.validateUrl(uiState.socialMedia.linkedin)
        )

        ProfileTextField(
            value = uiState.socialMedia.youtube,
            onValueChange = {
                viewModel.updateSocialMedia(
                    uiState.socialMedia.copy(youtube = it)
                )
            },
            label = "YouTube",
            placeholder = "https://youtube.com/@username",
            errorMessage = ValidationHelper.validateUrl(uiState.socialMedia.youtube)
        )

        Button(
            onClick = { viewModel.saveSocialMedia() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Social Media")
            }
        }
    }
}
