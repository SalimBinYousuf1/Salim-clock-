package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.SalimClockDatabase
import com.example.data.repository.SalimClockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SalimClockApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val database by lazy { SalimClockDatabase.getDatabase(this) }
    val repository by lazy { SalimClockRepository(database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository.initializeDefaultsIfNeeded(applicationScope)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM,
                "Alarm Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alarm and escalation notifications"
                enableVibration(true)
            }

            val timerChannel = NotificationChannel(
                CHANNEL_TIMER,
                "Active Timers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active timer countdowns and stage completions"
            }

            val smartWakeChannel = NotificationChannel(
                CHANNEL_SMART_WAKE,
                "Smart Wake Window",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Smart wake monitoring sensor status"
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(timerChannel)
            notificationManager.createNotificationChannel(smartWakeChannel)
        }
    }

    companion object {
        lateinit var instance: SalimClockApplication
            private set
        const val CHANNEL_ALARM = "salim_alarm_channel"
        const val CHANNEL_TIMER = "salim_timer_channel"
        const val CHANNEL_SMART_WAKE = "salim_smart_wake_channel"
    }
}
