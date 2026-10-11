package com.example.data.repository

import android.content.Context
import com.example.data.local.SalimClockDatabase
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.BedtimeScheduleEntity
import com.example.data.local.entity.LapRecordEntity
import com.example.data.local.entity.SavedCityEntity
import com.example.data.local.entity.TimerPresetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SalimClockRepository(private val database: SalimClockDatabase) {
    private val alarmDao = database.alarmDao()
    private val timerPresetDao = database.timerPresetDao()
    private val savedCityDao = database.savedCityDao()
    private val lapRecordDao = database.lapRecordDao()
    private val bedtimeDao = database.bedtimeDao()

    // Alarms
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    suspend fun getAlarmById(id: Long) = alarmDao.getAlarmById(id)
    suspend fun insertAlarm(alarm: AlarmEntity) = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)
    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)

    // Timer Presets
    val allTimerPresets: Flow<List<TimerPresetEntity>> = timerPresetDao.getAllPresets()
    suspend fun insertTimerPreset(preset: TimerPresetEntity) = timerPresetDao.insertPreset(preset)
    suspend fun deleteTimerPreset(preset: TimerPresetEntity) = timerPresetDao.deletePreset(preset)

    // Saved Cities
    val allSavedCities: Flow<List<SavedCityEntity>> = savedCityDao.getAllSavedCities()
    suspend fun insertCity(city: SavedCityEntity) = savedCityDao.insertCity(city)
    suspend fun deleteCity(city: SavedCityEntity) = savedCityDao.deleteCity(city)

    // Laps
    fun getLapsForSession(sessionId: Long): Flow<List<LapRecordEntity>> = lapRecordDao.getLapsForSession(sessionId)
    suspend fun insertLap(lap: LapRecordEntity) = lapRecordDao.insertLap(lap)
    suspend fun clearSessionLaps(sessionId: Long) = lapRecordDao.clearSessionLaps(sessionId)

    // Bedtime
    val bedtimeSchedule: Flow<BedtimeScheduleEntity?> = bedtimeDao.getSchedule()
    suspend fun updateBedtimeSchedule(schedule: BedtimeScheduleEntity) = bedtimeDao.insertOrUpdate(schedule)

    fun initializeDefaultsIfNeeded(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            // Check alarms
            val existingAlarms = alarmDao.getAllAlarms().firstOrNull()
            if (existingAlarms.isNullOrEmpty()) {
                alarmDao.insertAlarm(
                    AlarmEntity(
                        hour = 6,
                        minute = 45,
                        label = "Sunrise Awakening",
                        isEnabled = true,
                        repeatDays = 0x3E, // Mon-Fri
                        clockFaceStyle = "SpringWarmth",
                        dismissChallenge = "Math",
                        mathDifficulty = "Medium",
                        sunriseEnabled = true,
                        sunriseRampMinutes = 15,
                        volumeRampMinutes = 2,
                        soundName = "Radiant Horizon",
                        vibrationPattern = "Escalating"
                    )
                )
                alarmDao.insertAlarm(
                    AlarmEntity(
                        hour = 8,
                        minute = 15,
                        label = "Gym & Morning Run",
                        isEnabled = false,
                        repeatDays = 0x54, // Mon, Wed, Fri
                        clockFaceStyle = "Fleeting",
                        dismissChallenge = "Shake",
                        shakeTargetCount = 25,
                        sunriseEnabled = false,
                        soundName = "Energetic Pulse",
                        vibrationPattern = "Rapid Staccato"
                    )
                )
                alarmDao.insertAlarm(
                    AlarmEntity(
                        hour = 22,
                        minute = 30,
                        label = "Wind-down Reminder",
                        isEnabled = true,
                        repeatDays = 0x7F, // Everyday
                        clockFaceStyle = "Continuity",
                        dismissChallenge = "Phrase",
                        phraseText = "Tomorrow is a fresh canvas.",
                        sunriseEnabled = false,
                        soundName = "Zen Bell"
                    )
                )
            }

            // Check timer presets
            val existingPresets = timerPresetDao.getAllPresets().firstOrNull()
            if (existingPresets.isNullOrEmpty()) {
                val presets = listOf(
                    TimerPresetEntity(name = "Espresso Shot", durationSeconds = 25, category = "Cooking", colorHex = 0xFFFF9966),
                    TimerPresetEntity(name = "Green Tea", durationSeconds = 180, category = "Cooking", colorHex = 0xFF00F5A0),
                    TimerPresetEntity(name = "Mindful Pause", durationSeconds = 300, category = "Quick", colorHex = 0xFF00E5FF),
                    TimerPresetEntity(name = "Power Nap", durationSeconds = 1200, category = "Quick", colorHex = 0xFF7C4DFF),
                    TimerPresetEntity(name = "Pomodoro Focus", durationSeconds = 1500, category = "Pomodoro", colorHex = 0xFFFF5E62),
                    TimerPresetEntity(name = "HIIT Interval", durationSeconds = 240, category = "Interval", colorHex = 0xFFFFD166)
                )
                for (p in presets) {
                    timerPresetDao.insertPreset(p)
                }
            }

            // Check saved cities
            val existingCities = savedCityDao.getAllSavedCities().firstOrNull()
            if (existingCities.isNullOrEmpty()) {
                val cities = listOf(
                    SavedCityEntity(cityName = "Tokyo", country = "Japan", timeZoneId = "Asia/Tokyo", isHomeCity = false, displayOrder = 1),
                    SavedCityEntity(cityName = "London", country = "United Kingdom", timeZoneId = "Europe/London", isHomeCity = false, displayOrder = 2),
                    SavedCityEntity(cityName = "New York", country = "United States", timeZoneId = "America/New_York", isHomeCity = false, displayOrder = 3),
                    SavedCityEntity(cityName = "Dubai", country = "United Arab Emirates", timeZoneId = "Asia/Dubai", isHomeCity = false, displayOrder = 4),
                    SavedCityEntity(cityName = "Sydney", country = "Australia", timeZoneId = "Australia/Sydney", isHomeCity = false, displayOrder = 5),
                    SavedCityEntity(cityName = "Paris", country = "France", timeZoneId = "Europe/Paris", isHomeCity = false, displayOrder = 6)
                )
                savedCityDao.insertCities(cities)
            }

            // Check bedtime
            val existingBedtime = bedtimeDao.getScheduleSync()
            if (existingBedtime == null) {
                bedtimeDao.insertOrUpdate(
                    BedtimeScheduleEntity(
                        id = 1,
                        bedHour = 23,
                        bedMinute = 0,
                        wakeHour = 7,
                        wakeMinute = 0,
                        isEnabled = true,
                        windDownMinutes = 30,
                        hasCalendarSuggestion = true,
                        calendarEventTitle = "Product Strategy Sync at 08:30 AM",
                        lastSuggestedWakeHour = 6,
                        lastSuggestedWakeMinute = 45,
                        sleepSoundName = "Rain on Window",
                        sleepSoundTimerMinutes = 30
                    )
                )
            }
        }
    }
}
