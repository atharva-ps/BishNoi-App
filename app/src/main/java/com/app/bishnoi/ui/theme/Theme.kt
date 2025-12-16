package com.app.bishnoi.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = YellowPrimary,
    onPrimary = OnYellow,
    primaryContainer = YellowContainer,
    onPrimaryContainer = OnYellow,

    secondary = YellowPrimary,
    onSecondary = OnYellow,

    background = BackgroundLight,
    onBackground = OnBackgroundLight,

    surface = SurfaceLight,
    onSurface = OnBackgroundLight,

    surfaceVariant = SurfaceLight,
    onSurfaceVariant = GrayText,

    outline = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = YellowPrimary,
    onPrimary = OnYellow,
    primaryContainer = YellowContainer,
    onPrimaryContainer = OnYellow,

    secondary = YellowPrimary,
    onSecondary = OnYellow,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,

    surface = SurfaceDark,
    onSurface = OnBackgroundDark,

    surfaceVariant = SurfaceDark,
    onSurfaceVariant = GrayText
)

@Composable
fun BishNoiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
