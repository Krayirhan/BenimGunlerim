package com.benimgunlerim.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailySummaryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationConstants.ACTION_DAILY_SUMMARY) return
        context.showDailySummaryReminder()
    }
}
