package com.benimgunlerim

import android.app.Application
import android.os.StrictMode
import com.benimgunlerim.analytics.AppCrashHandler
import com.benimgunlerim.notifications.ensureRoutineNotificationChannel
import com.benimgunlerim.notifications.ReminderBootstrapper
import com.benimgunlerim.domain.service.AppEventCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BenimGunlerimApplication : Application() {
    @Inject lateinit var appCrashHandler: AppCrashHandler
    @Inject lateinit var reminderBootstrapper: ReminderBootstrapper
    @Inject lateinit var appEventCoordinator: AppEventCoordinator

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build(),
            )
        }
        appCrashHandler.install()
        ensureRoutineNotificationChannel()
        reminderBootstrapper.rescheduleReminders()
        appEventCoordinator.start()
    }
}
