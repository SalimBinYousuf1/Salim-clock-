package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.entity.AlarmEntity
import java.util.Calendar

object AlarmScheduler {
    const val ACTION_TRIGGER = "com.example.salimclock.ACTION_ALARM_TRIGGER"
    const val EXTRA_ALARM_ID = "extra_alarm_id"

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = calculateNextTriggerMillis(alarm)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback for permission restrictions
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun calculateNextTriggerMillis(alarm: AlarmEntity): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!alarm.isRecurring || alarm.repeatDays == 0) {
            // One-time alarm
            if (target.before(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (alarm.skipNextOccurrence) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        // Recurring alarm
        while (true) {
            val dayOfWeek = target.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon ... 7 = Sat
            val bitIndex = dayOfWeek - 1
            val isDayEnabled = (alarm.repeatDays and (1 shl bitIndex)) != 0

            if (isDayEnabled && target.after(now)) {
                if (alarm.skipNextOccurrence) {
                    // Skip this occurrence and search for the next
                    target.add(Calendar.DAY_OF_YEAR, 1)
                    continue
                }
                return target.timeInMillis
            }
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    fun formatRemainingTime(triggerMillis: Long): String {
        val diff = triggerMillis - System.currentTimeMillis()
        if (diff <= 0) return "Right now"
        val totalMinutes = diff / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "in ${hours}h ${minutes}m"
            hours > 0 -> "in ${hours}h"
            else -> "in ${minutes}m"
        }
    }
}
