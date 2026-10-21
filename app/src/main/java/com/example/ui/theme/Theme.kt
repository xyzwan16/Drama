package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DramakuDarkColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = TextPrimary,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = TextPrimary,
    secondary = GoldAccent,
    onSecondary = CinemaBlack,
    secondaryContainer = CinemaCardSurfaceElevated,
    onSecondaryContainer = GoldYellow,
    background = CinemaBlack,
    onBackground = TextPrimary,
    surface = CinemaDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CinemaCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Premium Cinema aesthetic is dark-first
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DramakuDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CinemaBlack.toArgb()
                window.navigationBarColor = CinemaBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
