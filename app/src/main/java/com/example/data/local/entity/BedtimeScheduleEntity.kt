package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bedtime_schedule")
data class BedtimeScheduleEntity(
    @PrimaryKey val id: Int = 1,
    val bedHour: Int = 23,
    val bedMinute: Int = 0,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val isEnabled: Boolean = true,
    val windDownMinutes: Int = 30,
    val activeDays: Int = 0x7F,
    val calendarIntegrationEnabled: Boolean = true,
    val lastSuggestedWakeHour: Int = 6,
    val lastSuggestedWakeMinute: Int = 45,
    val hasCalendarSuggestion: Boolean = true,
    val calendarEventTitle: String = "Team Standup at 08:30 AM",
    val sleepSoundName: String = "Rain on Window",
    val sleepSoundTimerMinutes: Int = 30
)
