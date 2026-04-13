package com.benimgunlerim.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MorningPlannerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.showMorningPlannerReminder()
    }
}
