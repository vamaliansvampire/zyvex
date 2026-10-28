package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ZyvexGoldColorScheme = darkColorScheme(
    primary = MetallicGold300,
    onPrimary = RoyalBlack900,
    primaryContainer = MetallicGold700,
    onPrimaryContainer = MetallicGold100,
    secondary = MetallicGold400,
    onSecondary = RoyalBlack900,
    secondaryContainer = RoyalBlack600,
    onSecondaryContainer = MetallicGold200,
    tertiary = HudCyan,
    onTertiary = RoyalBlack900,
    background = RoyalBlack900,
    onBackground = MetallicGold100,
    surface = RoyalBlack800,
    onSurface = MetallicGold50,
    surfaceVariant = RoyalBlack700,
    onSurfaceVariant = MetallicGold200,
    outline = MetallicGold600,
    outlineVariant = RoyalBlack500,
    error = HudRedAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = RoyalBlack900.toArgb()
                window.navigationBarColor = RoyalBlack900.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ZyvexGoldColorScheme,
        typography = Typography,
        content = content
    )
}
