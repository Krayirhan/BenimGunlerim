package com.benimgunlerim.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(NotificationConstants.EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(NotificationConstants.EXTRA_TASK_TITLE) ?: "Görev"
        context.showTaskReminder(taskId, title)
    }
}
