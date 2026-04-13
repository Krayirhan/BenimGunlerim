package com.benimgunlerim.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate

class RoutineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationConstants.ACTION_ROUTINE_REMINDER) return
        val routineId = intent.getStringExtra(NotificationConstants.EXTRA_ROUTINE_ID) ?: return
        val routineName = intent.getStringExtra(NotificationConstants.EXTRA_ROUTINE_NAME) ?: "Rutinin"
        val targetDays = intent.getStringExtra(NotificationConstants.EXTRA_TARGET_DAYS).orEmpty()
        val today = LocalDate.now().dayOfWeek.name
        if (targetDays.isNotBlank() && today !in targetDays.split(",")) return
        context.showRoutineReminder(routineId, routineName)
    }
}
