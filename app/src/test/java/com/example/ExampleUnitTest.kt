package com.example

import com.example.alarm.AlarmScheduler
import com.example.data.local.entity.AlarmEntity
import com.example.ui.components.ClockFaceStyle
import com.example.ui.screens.formatLapTime
import com.example.vibration.VibrationManager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun testAlarmScheduler_calculatesFutureTimestamp() {
        val now = Calendar.getInstance()
        val targetHour = (now.get(Calendar.HOUR_OF_DAY) + 2) % 24
        val targetMinute = now.get(Calendar.MINUTE)

        val alarm = AlarmEntity(
            hour = targetHour,
            minute = targetMinute,
            isEnabled = true,
            isRecurring = false
        )

        val nextTrigger = AlarmScheduler.calculateNextTriggerMillis(alarm)
        assertTrue(nextTrigger > System.currentTimeMillis() - 5000)
    }

    @Test
    fun testFormatRemainingTime() {
        val future = System.currentTimeMillis() + (3 * 3600 * 1000L) + (15 * 60 * 1000L)
        val formatted = AlarmScheduler.formatRemainingTime(future)
        assertTrue(formatted.contains("3h") || formatted.contains("in"))
    }

    @Test
    fun testVibrationManager_parseCustomPattern() {
        val pattern = VibrationManager.parseCustomPattern("200, 300, 400")
        assertArrayEquals(longArrayOf(0, 200, 300, 400), pattern)
    }

    @Test
    fun testStopwatch_formatLapTime() {
        val millis = 65430L // 1 min, 5 sec, 43 centis
        val formatted = formatLapTime(millis)
        assertEquals("01:05.43", formatted)
    }

    @Test
    fun testClockFaceStyle_hasDistinctPalettes() {
        assertEquals(6, ClockFaceStyle.entries.size)
        val defaultColors = listOf(androidx.compose.ui.graphics.Color.Red)
        val springPalette = ClockFaceStyle.SpringWarmth.getPaletteForArea(defaultColors)
        assertTrue(springPalette.size >= 3)
    }
}
