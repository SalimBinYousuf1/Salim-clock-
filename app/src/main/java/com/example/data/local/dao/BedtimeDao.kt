package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BedtimeScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BedtimeDao {
    @Query("SELECT * FROM bedtime_schedule WHERE id = 1")
    fun getSchedule(): Flow<BedtimeScheduleEntity?>

    @Query("SELECT * FROM bedtime_schedule WHERE id = 1")
    suspend fun getScheduleSync(): BedtimeScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(schedule: BedtimeScheduleEntity)

    @Update
    suspend fun update(schedule: BedtimeScheduleEntity)
}
