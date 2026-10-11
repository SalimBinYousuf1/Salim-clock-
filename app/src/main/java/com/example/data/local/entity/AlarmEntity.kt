package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Morning Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: Int = 0x3E, // Mon-Fri default (bit 1..5)
    val isRecurring: Boolean = true,
    val skipNextOccurrence: Boolean = false,
    val clockFaceStyle: String = "RingLike", // Fragmented, NewTimes, RingLike, SpringWarmth, Fleeting, Continuity
    val clockShape: String = "Circle", // Circle, RoundedSquare, NotchedSquircle
    val fillMode: String = "Gradient", // Solid, Gradient
    val dismissChallenge: String = "Math", // None, Math, Shake, QrCode, Phrase
    val mathDifficulty: String = "Medium", // Easy, Medium, Hard
    val phraseText: String = "I am awake, clear minded, and ready to conquer today.",
    val qrTargetCode: String = "SALIM-BEDSIDE-ALARM-2026",
    val shakeTargetCount: Int = 20,
    val sunriseEnabled: Boolean = true,
    val sunriseRampMinutes: Int = 15,
    val sunriseStartBrightness: Float = 0.15f,
    val sunriseEndBrightness: Float = 1.0f,
    val sunriseStartColorTemp: Int = 2000,
    val sunriseEndColorTemp: Int = 5500,
    val volumeRampMinutes: Int = 2,
    val soundName: String = "Radiant Horizon",
    val vibrationPattern: String = "Escalating",
    val customVibePattern: String = "200,200,400,200,600,200",
    val maxSnoozeCount: Int = 3,
    val snoozeDurationMinutes: Int = 5,
    val escalatingSnooze: Boolean = true,
    val currentSnoozeCount: Int = 0,
    val escalationEnabled: Boolean = true,
    val escalationIntervalMinutes: Int = 3,
    val escalationSecondarySound: String = "Energetic Pulse",
    val emergencyContactName: String = "Emergency Contact",
    val emergencyContactPhone: String = "",
    val geofenceAutoSkip: Boolean = false,
    val smartWakeEnabled: Boolean = true,
    val smartWakeWindowMinutes: Int = 20
)
