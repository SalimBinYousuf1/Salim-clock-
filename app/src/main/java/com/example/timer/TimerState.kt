package com.example.timer

data class ActiveTimer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Timer",
    val totalSeconds: Long,
    var remainingSeconds: Long = totalSeconds,
    var isRunning: Boolean = false,
    val clockFaceStyle: String = "RingLike",
    val colorHex: Long = 0xFF00E5FF
) {
    val progress: Float
        get() = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedRemaining: String
        get() {
            val hrs = remainingSeconds / 3600
            val mins = (remainingSeconds % 3600) / 60
            val secs = remainingSeconds % 60
            return if (hrs > 0) {
                String.format("%02d:%02d:%02d", hrs, mins, secs)
            } else {
                String.format("%02d:%02d", mins, secs)
            }
        }
}

enum class PomodoroStage(val displayName: String, val defaultMinutes: Int) {
    WORK("Focus Work", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

data class IntervalStage(
    val name: String,
    val durationSeconds: Int,
    val colorHex: Long
)
