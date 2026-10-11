package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.TimerPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerPresetDao {
    @Query("SELECT * FROM timer_presets ORDER BY durationSeconds ASC")
    fun getAllPresets(): Flow<List<TimerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TimerPresetEntity): Long

    @Delete
    suspend fun deletePreset(preset: TimerPresetEntity)

    @Query("DELETE FROM timer_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
}
