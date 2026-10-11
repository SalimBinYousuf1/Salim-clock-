package com.example.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.SalimClockApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val app = context.applicationContext as SalimClockApplication

        // Launch AlarmAlertActivity directly
        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        context.startActivity(alertIntent)

        // Also post heads-up notification in case activity was backgrounded
        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            alertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SalimClockApplication.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Salim Alarm")
            .setContentText("Wake up! Dismissal challenge required.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarmId.toInt(), notification)

        // Reschedule recurring alarms for next occurrence
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = app.repository.getAlarmById(alarmId)
            if (alarm != null) {
                if (alarm.isRecurring) {
                    AlarmScheduler.scheduleAlarm(context, alarm)
                } else {
                    app.repository.updateAlarm(alarm.copy(isEnabled = false))
                }
            }
        }
    }
}
