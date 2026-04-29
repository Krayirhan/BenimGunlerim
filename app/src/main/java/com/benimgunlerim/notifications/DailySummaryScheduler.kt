package com.benimgunlerim.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.benimgunlerim.domain.DateTimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface DailySummarySchedule {
    fun schedule(time: LocalTime)
    fun cancel()
}

@Singleton
class DailySummaryScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dateTimeProvider: DateTimeProvider,
) : DailySummarySchedule {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(time: LocalTime) {
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(time),
            AlarmManager.INTERVAL_DAY,
            pendingIntent(),
        )
    }

    override fun cancel() {
        alarmManager.cancel(pendingIntent())
    }

    private fun nextTriggerMillis(time: LocalTime): Long {
        val now = dateTimeProvider.today().atTime(dateTimeProvider.currentTime())
        var next = dateTimeProvider.today().atTime(time)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, DailySummaryReceiver::class.java).apply {
            action = NotificationConstants.ACTION_DAILY_SUMMARY
        }
        return PendingIntent.getBroadcast(
            context,
            DAILY_SUMMARY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val DAILY_SUMMARY_REQUEST_CODE = 21_000
    }
}
