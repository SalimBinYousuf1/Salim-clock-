package com.example.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationManager {
    val PRESET_PATTERNS = mapOf(
        "Standard" to longArrayOf(0, 500, 500, 500),
        "SOS" to longArrayOf(0, 150, 100, 150, 100, 150, 300, 400, 100, 400, 100, 400, 300, 150, 100, 150, 100, 150),
        "Heartbeat" to longArrayOf(0, 140, 120, 140, 700),
        "Escalating" to longArrayOf(0, 100, 300, 200, 250, 300, 200, 500, 150, 700),
        "Rapid Staccato" to longArrayOf(0, 80, 80, 80, 80, 80, 80, 80, 350)
    )

    fun parseCustomPattern(patternStr: String): LongArray {
        return try {
            val parts = patternStr.split(",").map { it.trim().toLong() }
            if (parts.size >= 2) {
                // Prepend 0 delay if not present
                (listOf(0L) + parts).toLongArray()
            } else {
                longArrayOf(0, 300, 300, 300)
            }
        } catch (_: Exception) {
            longArrayOf(0, 300, 300, 300)
        }
    }

    fun vibrate(context: Context, patternName: String, customPattern: String = "", repeatIndex: Int = -1) {
        val vibrator = getVibrator(context) ?: return
        val pattern = if (patternName == "Custom") {
            parseCustomPattern(customPattern)
        } else {
            PRESET_PATTERNS[patternName] ?: longArrayOf(0, 500, 500)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeatIndex))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeatIndex)
        }
    }

    fun stop(context: Context) {
        val vibrator = getVibrator(context) ?: return
        vibrator.cancel()
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
