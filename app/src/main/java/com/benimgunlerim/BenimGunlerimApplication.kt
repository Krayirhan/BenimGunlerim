package com.benimgunlerim

import android.app.Application
import com.benimgunlerim.notifications.ReminderBootstrapper
import com.benimgunlerim.notifications.ensureRoutineNotificationChannel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BenimGunlerimApplication : Application() {
    @Inject lateinit var reminderBootstrapper: ReminderBootstrapper

    override fun onCreate() {
        super.onCreate()
        ensureRoutineNotificationChannel()
        reminderBootstrapper.rescheduleRoutineReminders()
    }
}
