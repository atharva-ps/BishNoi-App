package com.app.bishnoi.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (isFollowing) {
                onUnfollowClick()
            } else {
                onFollowClick()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = if (isFollowing) {
            ButtonDefaults.outlinedButtonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = if (isFollowing) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }
            )
        } else {
            Text(
                text = if (isFollowing) "Following" else "Follow",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
