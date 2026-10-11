package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SalimDarkColorScheme = darkColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = AmberSecondary,
    secondary = CyanPrimary,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = CyanPrimary,
    tertiary = EmeraldPrimary,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFF1F1F5),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F1F5),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA5A5B8),
    outline = DarkBorder
)

private val SalimLightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = Color(0xFFC43E42),
    secondary = VioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceCard,
    onSecondaryContainer = VioletSecondary,
    tertiary = TealSecondary,
    onTertiary = Color.Black,
    background = LightBackground,
    onBackground = Color(0xFF13131D),
    surface = LightSurface,
    onSurface = Color(0xFF13131D),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5D5D70),
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SalimDarkColorScheme else SalimLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
