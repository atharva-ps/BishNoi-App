package com.justbaat.mybishnoiapp.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    selectedRoute: String,
    onHomeClick: () -> Unit,
    onCreatePostClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        // Home
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = if (selectedRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(28.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )

        // Create Post (Center button with yellow background)
        NavigationBarItem(
            selected = false,
            onClick = onCreatePostClick,
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(56.dp, 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Post",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )

        // Profile
        NavigationBarItem(
            selected = selectedRoute == "profile",
            onClick = onProfileClick,
            icon = {
                Icon(
                    imageVector = if (selectedRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(28.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
    }
}
