package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Surface Foundations
val DarkBackground = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF13131E)
val DarkSurfaceVariant = Color(0xFF1C1C2B)
val DarkSurfaceCard = Color(0xFF222236)
val DarkBorder = Color(0x33FFFFFF)

val LightBackground = Color(0xFFF4F4F8)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE9E9F0)
val LightSurfaceCard = Color(0xFFFBFBFD)
val LightBorder = Color(0x1A000000)

// Accent Section Gradients
val CoralPrimary = Color(0xFFFF5E62)
val AmberSecondary = Color(0xFFFF9966)
val AlarmGradient = listOf(CoralPrimary, AmberSecondary)

val CyanPrimary = Color(0xFF00E5FF)
val VioletSecondary = Color(0xFF7C4DFF)
val TimerGradient = listOf(CyanPrimary, VioletSecondary)

val EmeraldPrimary = Color(0xFF00F5A0)
val TealSecondary = Color(0xFF00D9F5)
val StopwatchGradient = listOf(EmeraldPrimary, TealSecondary)

val IndigoPrimary = Color(0xFF6366F1)
val MagentaSecondary = Color(0xFFEC4899)
val WorldClockGradient = listOf(IndigoPrimary, MagentaSecondary)

val BedtimeDeepNavy = Color(0xFF2E1C59)
val BedtimeRoseGold = Color(0xFFE07A5F)
val BedtimeGradient = listOf(BedtimeDeepNavy, BedtimeRoseGold)

// Clock Face Specific Palettes
val SpringWarmthPalette = listOf(Color(0xFFFF6B6B), Color(0xFFFFA07A), Color(0xFFFFD166))
val FleetingPalette = listOf(Color(0xFF00FFFF), Color(0xFF00B0FF), Color(0xFF7C4DFF))
val ContinuityPalette = listOf(Color(0xFF1A237E), Color(0xFF673AB7), Color(0xFFFF9800), Color(0xFF4FC3F7))

fun getBrushForArea(gradient: List<Color>): Brush {
    return Brush.linearGradient(gradient)
}
