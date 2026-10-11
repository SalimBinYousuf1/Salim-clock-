package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimClockApplication
import com.example.alarm.AlarmScheduler
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.BedtimeScheduleEntity
import com.example.data.local.entity.SavedCityEntity
import com.example.data.local.entity.TimerPresetEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalimClockViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SalimClockApplication).repository

    val alarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timerPresets: StateFlow<List<TimerPresetEntity>> = repository.allTimerPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedCities: StateFlow<List<SavedCityEntity>> = repository.allSavedCities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bedtimeSchedule: StateFlow<BedtimeScheduleEntity?> = repository.bedtimeSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = isEnabled)
            repository.updateAlarm(updated)
            if (isEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), updated)
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
            }
        }
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            if (alarm.id == 0L) {
                val newId = repository.insertAlarm(alarm)
                val created = alarm.copy(id = newId)
                if (created.isEnabled) {
                    AlarmScheduler.scheduleAlarm(getApplication(), created)
                }
            } else {
                repository.updateAlarm(alarm)
                if (alarm.isEnabled) {
                    AlarmScheduler.scheduleAlarm(getApplication(), alarm)
                } else {
                    AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
                }
            }
        }
    }

    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), alarmId)
            repository.deleteAlarmById(alarmId)
        }
    }

    fun saveTimerPreset(preset: TimerPresetEntity) {
        viewModelScope.launch {
            repository.insertTimerPreset(preset)
        }
    }

    fun deleteTimerPreset(preset: TimerPresetEntity) {
        viewModelScope.launch {
            repository.deleteTimerPreset(preset)
        }
    }

    fun addCity(city: SavedCityEntity) {
        viewModelScope.launch {
            repository.insertCity(city)
        }
    }

    fun deleteCity(city: SavedCityEntity) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }
    }

    fun updateBedtimeSchedule(schedule: BedtimeScheduleEntity) {
        viewModelScope.launch {
            repository.updateBedtimeSchedule(schedule)
        }
    }
}
