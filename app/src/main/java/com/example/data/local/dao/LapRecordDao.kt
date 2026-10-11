package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.LapRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LapRecordDao {
    @Query("SELECT * FROM lap_records WHERE sessionId = :sessionId ORDER BY lapNumber DESC")
    fun getLapsForSession(sessionId: Long): Flow<List<LapRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLap(lap: LapRecordEntity): Long

    @Query("DELETE FROM lap_records WHERE sessionId = :sessionId")
    suspend fun clearSessionLaps(sessionId: Long)
}
