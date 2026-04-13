package com.benimgunlerim

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.notifications.BootReceiver
import com.benimgunlerim.notifications.DailySummaryReceiver
import com.benimgunlerim.notifications.DailySummaryScheduler
import com.benimgunlerim.notifications.MorningPlannerReceiver
import com.benimgunlerim.notifications.MorningPlannerScheduler
import com.benimgunlerim.notifications.NotificationConstants
import com.benimgunlerim.notifications.RoutineReminderReceiver
import com.benimgunlerim.notifications.RoutineReminderScheduler
import com.benimgunlerim.notifications.SnoozeReceiver
import com.benimgunlerim.notifications.TaskReminderReceiver
import com.benimgunlerim.notifications.TaskReminderScheduler
import com.benimgunlerim.notifications.ensureMorningNotificationChannel
import com.benimgunlerim.notifications.ensureRoutineNotificationChannel
import com.benimgunlerim.notifications.ensureTaskNotificationChannel
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationMatrixSmokeTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun notificationChannels_areCreated_onSupportedApiLevels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        context.ensureRoutineNotificationChannel()
        context.ensureTaskNotificationChannel()
        context.ensureMorningNotificationChannel()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        assertNotNull(manager.getNotificationChannel(NotificationConstants.ROUTINE_CHANNEL_ID))
        assertNotNull(manager.getNotificationChannel(NotificationConstants.TASK_CHANNEL_ID))
        assertNotNull(manager.getNotificationChannel(NotificationConstants.MORNING_CHANNEL_ID))
    }

    @Test
    fun schedulers_scheduleAndCancel_withoutCrash() {
        val now = LocalDate.now()
        val futureTime = LocalTime.now().plusMinutes(10)

        TaskReminderScheduler(context).apply {
            schedule("task_matrix_1", "Bildirim matrix test görevi", now, futureTime)
            cancel("task_matrix_1")
        }

        DailySummaryScheduler(context).apply {
            schedule(futureTime)
            cancel()
        }

        MorningPlannerScheduler(context).apply {
            schedule(futureTime)
            cancel()
        }

        val routine = RoutineEntity(
            id = "routine_matrix_1",
            name = "Matrix Routine",
            description = null,
            targetDays = "1,2,3,4,5,6,7",
            preferredTime = futureTime.withSecond(0).withNano(0).toString(),
            color = null,
            isArchived = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        RoutineReminderScheduler(context).apply {
            schedule(routine)
            cancel(routine)
        }
    }

    @Test
    fun notificationReceivers_arePresentInManifest() {
        assertReceiverRegistered(BootReceiver::class.java)
        assertReceiverRegistered(RoutineReminderReceiver::class.java)
        assertReceiverRegistered(TaskReminderReceiver::class.java)
        assertReceiverRegistered(DailySummaryReceiver::class.java)
        assertReceiverRegistered(MorningPlannerReceiver::class.java)
        assertReceiverRegistered(SnoozeReceiver::class.java)
    }

    @Test
    fun bootReceiver_ignoresUnrelatedActions_withoutCrash() {
        val receiver = BootReceiver()
        val unrelatedIntent = Intent(Intent.ACTION_VIEW)
        receiver.onReceive(context, unrelatedIntent)
    }

    private fun assertReceiverRegistered(receiverClass: Class<*>) {
        val componentName = ComponentName(context, receiverClass)
        val receiverInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getReceiverInfo(
                componentName,
                PackageManager.ComponentInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getReceiverInfo(componentName, PackageManager.GET_META_DATA)
        }
        assertNotNull(receiverInfo)
    }
}
