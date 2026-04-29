package com.benimgunlerim.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.DateTimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dateTimeProvider: DateTimeProvider,
) : RoutineReminderSchedulerContract {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(routine: RoutineEntity) {
        val time = routine.preferredTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return
        val triggerAt = nextTriggerMillis(time)
        val pendingIntent = routinePendingIntent(routine)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent,
        )
    }

    override fun cancel(routine: RoutineEntity) {
        alarmManager.cancel(routinePendingIntent(routine))
    }

    private fun nextTriggerMillis(time: LocalTime): Long {
        val zone = ZoneId.systemDefault()
        val now = dateTimeProvider.today().atTime(dateTimeProvider.currentTime())
        var next = dateTimeProvider.today().atTime(time)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return next.atZone(zone).toInstant().toEpochMilli()
    }

    private fun routinePendingIntent(routine: RoutineEntity): PendingIntent {
        val intent = Intent(context, RoutineReminderReceiver::class.java).apply {
            action = NotificationConstants.ACTION_ROUTINE_REMINDER
            putExtra(NotificationConstants.EXTRA_ROUTINE_ID, routine.id)
            putExtra(NotificationConstants.EXTRA_ROUTINE_NAME, routine.name)
            putExtra(NotificationConstants.EXTRA_TARGET_DAYS, routine.targetDays)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forRoutine(routine.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
