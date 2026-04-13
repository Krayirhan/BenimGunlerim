package com.benimgunlerim.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(taskId: String, taskTitle: String, date: LocalDate, time: LocalTime) {
        val triggerAt = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerAt <= System.currentTimeMillis()) return
        val pendingIntent = taskPendingIntent(taskId, taskTitle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(taskId: String) {
        alarmManager.cancel(taskPendingIntent(taskId, ""))
    }

    private fun taskPendingIntent(taskId: String, taskTitle: String): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = NotificationConstants.ACTION_TASK_REMINDER
            putExtra(NotificationConstants.EXTRA_TASK_ID, taskId)
            putExtra(NotificationConstants.EXTRA_TASK_TITLE, taskTitle)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
