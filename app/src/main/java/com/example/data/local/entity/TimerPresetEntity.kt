package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_presets")
data class TimerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationSeconds: Long,
    val category: String = "Quick", // Quick, Pomodoro, Interval, Custom
    val iconName: String = "timer",
    val colorHex: Long = 0xFF00E5FF
)
