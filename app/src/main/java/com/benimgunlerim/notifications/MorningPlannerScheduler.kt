package com.benimgunlerim.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface MorningPlannerSchedule {
    fun schedule(time: LocalTime)
    fun cancel()
}

@Singleton
class MorningPlannerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : MorningPlannerSchedule {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(time: LocalTime) {
        val now = LocalDateTime.now()
        var next = LocalDate.now().atTime(time)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            morningPendingIntent(),
        )
    }

    override fun cancel() {
        alarmManager.cancel(morningPendingIntent())
    }

    private fun morningPendingIntent(): PendingIntent {
        val intent = Intent(context, MorningPlannerReceiver::class.java).apply {
            action = NotificationConstants.ACTION_MORNING_PLANNER
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationConstants.MORNING_PLANNER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
