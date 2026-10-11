package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lap_records")
data class LapRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long,
    val isFastest: Boolean = false,
    val isSlowest: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
