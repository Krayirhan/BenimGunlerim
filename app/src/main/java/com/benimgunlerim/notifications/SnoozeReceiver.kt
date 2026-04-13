package com.benimgunlerim.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles snooze actions from notifications.
 * When ACTION_SNOOZE is received and snooze_show=false (user tapped the action button):
 *   → schedules a re-show alarm 10 minutes from now.
 * When ACTION_SNOOZE is received and snooze_show=true (alarm fired):
 *   → shows the appropriate notification.
 */
class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationConstants.ACTION_SNOOZE) return

        val type = intent.getStringExtra(NotificationConstants.EXTRA_SNOOZE_TYPE) ?: return
        if (type !in setOf(
                NotificationConstants.SNOOZE_TYPE_ROUTINE,
                NotificationConstants.SNOOZE_TYPE_TASK,
                NotificationConstants.SNOOZE_TYPE_DAILY,
                NotificationConstants.SNOOZE_TYPE_MORNING,
            )
        ) return
        val id = intent.getStringExtra(NotificationConstants.EXTRA_SNOOZE_ID) ?: return
        val title = intent.getStringExtra(NotificationConstants.EXTRA_SNOOZE_TITLE) ?: return
        val show = intent.getBooleanExtra("snooze_show", false)

        if (!show) {
            // User tapped "ertele" — schedule a re-show 10 min from now
            context.scheduleSnoozeShow(type, id, title)
        } else {
            // Alarm fired — show the notification now
            when (type) {
                NotificationConstants.SNOOZE_TYPE_ROUTINE -> context.showRoutineReminder(id, title)
                NotificationConstants.SNOOZE_TYPE_TASK -> context.showTaskReminder(id, title)
                NotificationConstants.SNOOZE_TYPE_DAILY -> context.showDailySummaryReminder()
                NotificationConstants.SNOOZE_TYPE_MORNING -> context.showMorningPlannerReminder()
            }
        }
    }
}
