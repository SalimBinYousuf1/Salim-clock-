package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AlarmDao
import com.example.data.local.dao.BedtimeDao
import com.example.data.local.dao.LapRecordDao
import com.example.data.local.dao.SavedCityDao
import com.example.data.local.dao.TimerPresetDao
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.BedtimeScheduleEntity
import com.example.data.local.entity.LapRecordEntity
import com.example.data.local.entity.SavedCityEntity
import com.example.data.local.entity.TimerPresetEntity

@Database(
    entities = [
        AlarmEntity::class,
        TimerPresetEntity::class,
        SavedCityEntity::class,
        LapRecordEntity::class,
        BedtimeScheduleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SalimClockDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun timerPresetDao(): TimerPresetDao
    abstract fun savedCityDao(): SavedCityDao
    abstract fun lapRecordDao(): LapRecordDao
    abstract fun bedtimeDao(): BedtimeDao

    companion object {
        @Volatile
        private var INSTANCE: SalimClockDatabase? = null

        fun getDatabase(context: Context): SalimClockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalimClockDatabase::class.java,
                    "salim_clock_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
