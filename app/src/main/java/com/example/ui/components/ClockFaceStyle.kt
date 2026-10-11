package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AlarmGradient
import com.example.ui.theme.ContinuityPalette
import com.example.ui.theme.FleetingPalette
import com.example.ui.theme.SpringWarmthPalette
import com.example.ui.theme.StopwatchGradient
import com.example.ui.theme.TimerGradient
import com.example.ui.theme.WorldClockGradient

enum class ClockFaceStyle(val displayName: String, val description: String) {
    RingLike("RingLike", "Fluid, continuous gradient ring"),
    Fragmented("Fragmented", "Offset segmented arcs with modern rhythm"),
    NewTimes("NewTimes", "Geometric dial indices with sleek accent ring"),
    SpringWarmth("Spring Warmth", "Coral, peach & luminous gold gradient"),
    Fleeting("Fleeting", "Dynamic particle trail chasing the indicator"),
    Continuity("Continuity", "Smooth 24-hour day-to-night solar arc");

    fun getPaletteForArea(defaultAreaColors: List<Color>): List<Color> {
        return when (this) {
            SpringWarmth -> SpringWarmthPalette
            Fleeting -> FleetingPalette
            Continuity -> ContinuityPalette
            else -> defaultAreaColors
        }
    }
}

enum class ClockShape(val displayName: String) {
    Circle("Circle"),
    RoundedSquare("Rounded Square"),
    NotchedSquircle("Notched Squircle")
}

enum class FillMode(val displayName: String) {
    Gradient("Gradient"),
    Solid("Solid")
}
