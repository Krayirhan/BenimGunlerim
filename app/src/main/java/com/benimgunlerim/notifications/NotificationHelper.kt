package com.benimgunlerim.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.benimgunlerim.MainActivity
import com.benimgunlerim.R
import com.benimgunlerim.data.userPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun Context.ensureRoutineNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        NotificationConstants.ROUTINE_CHANNEL_ID,
        getString(R.string.notif_channel_routine_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = getString(R.string.notif_channel_routine_desc)
    }
    manager.createNotificationChannel(channel)
}

fun Context.showRoutineReminder(routineId: String, routineName: String) {
    if (!canPostNotifications()) return
    if (isInQuietHours()) return
    ensureRoutineNotificationChannel()
    val notification = NotificationCompat.Builder(this, NotificationConstants.ROUTINE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(getString(R.string.notif_routine_title))
        .setContentText(getString(R.string.notif_routine_body, routineName))
        .setContentIntent(openAppPendingIntent("routines", routineId.hashCode()))
        .addAction(buildSnoozeAction(NotificationConstants.SNOOZE_TYPE_ROUTINE, routineId, routineName, routineId.hashCode()))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    safeNotify(routineId.hashCode(), notification)
}

fun Context.showDailySummaryReminder() {
    if (!canPostNotifications()) return
    if (isInQuietHours()) return
    ensureRoutineNotificationChannel()
    val notification = NotificationCompat.Builder(this, NotificationConstants.ROUTINE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(getString(R.string.notif_daily_title))
        .setContentText(getString(R.string.notif_daily_body))
        .setContentIntent(openAppPendingIntent("today", 21_000))
        .addAction(buildSnoozeAction(NotificationConstants.SNOOZE_TYPE_DAILY, "daily", "Günlük özet", 21_000))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    safeNotify(21_000, notification)
}

private fun Context.canPostNotifications(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

/**
 * Permission kontrolü ve SecurityException koruması olan merkezi bildirim gönderici.
 * Lint [MissingPermission] uyarısını bu tek noktada bastırarak tüm çağrı yerlerini temiz tutar.
 */
@SuppressLint("MissingPermission")
internal fun Context.safeNotify(notificationId: Int, notification: Notification): Boolean {
    if (!canPostNotifications()) return false
    return try {
        NotificationManagerCompat.from(this).notify(notificationId, notification)
        true
    } catch (_: SecurityException) {
        false
    }
}

private fun Context.openAppPendingIntent(route: String, requestCode: Int): PendingIntent {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(NotificationConstants.EXTRA_START_ROUTE, route)
    }
    return PendingIntent.getActivity(
        this,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

fun Context.ensureTaskNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        NotificationConstants.TASK_CHANNEL_ID,
        getString(R.string.notif_channel_task_name),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = getString(R.string.notif_channel_task_desc)
    }
    manager.createNotificationChannel(channel)
}

fun Context.showTaskReminder(taskId: String, taskTitle: String) {
    if (!canPostNotifications()) return
    if (isInQuietHours()) return
    ensureTaskNotificationChannel()
    val notification = NotificationCompat.Builder(this, NotificationConstants.TASK_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(getString(R.string.notif_task_title))
        .setContentText(taskTitle)
        .setContentIntent(openAppPendingIntent("today", taskId.hashCode()))
        .addAction(buildSnoozeAction(NotificationConstants.SNOOZE_TYPE_TASK, taskId, taskTitle, taskId.hashCode()))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()
    safeNotify(taskId.hashCode(), notification)
}

fun Context.ensureMorningNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        NotificationConstants.MORNING_CHANNEL_ID,
        getString(R.string.notif_channel_morning_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = getString(R.string.notif_channel_morning_desc)
    }
    manager.createNotificationChannel(channel)
}

fun Context.showMorningPlannerReminder() {
    if (!canPostNotifications()) return
    if (isInQuietHours()) return
    ensureMorningNotificationChannel()
    val notification = NotificationCompat.Builder(this, NotificationConstants.MORNING_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(getString(R.string.notif_morning_title))
        .setContentText(getString(R.string.notif_morning_body))
        .setContentIntent(openAppPendingIntent("today", NotificationConstants.MORNING_PLANNER_REQUEST_CODE))
        .addAction(buildSnoozeAction(NotificationConstants.SNOOZE_TYPE_MORNING, "morning", "Sabah planlayıcısı", NotificationConstants.MORNING_PLANNER_REQUEST_CODE))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    safeNotify(NotificationConstants.MORNING_PLANNER_REQUEST_CODE, notification)
}

// ─── Quiet Hours ──────────────────────────────────────────────────────────────

internal fun Context.isInQuietHours(): Boolean {
    return try {
        val prefs = runBlocking { applicationContext.userPreferencesDataStore.data.first() }
        val enabled = prefs[booleanPreferencesKey("quiet_hours_enabled")] ?: false
        if (!enabled) return false
        val startStr = prefs[stringPreferencesKey("quiet_hours_start")] ?: "22:00"
        val endStr = prefs[stringPreferencesKey("quiet_hours_end")] ?: "07:00"
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalTime.now()
        val start = LocalTime.parse(startStr, fmt)
        val end = LocalTime.parse(endStr, fmt)
        // Handles overnight ranges (e.g. 22:00 – 07:00)
        if (start.isBefore(end)) now in start..end else now >= start || now <= end
    } catch (_: Exception) {
        false
    }
}

// ─── Snooze ───────────────────────────────────────────────────────────────────

private fun Context.buildSnoozeAction(type: String, id: String, title: String, requestCode: Int): NotificationCompat.Action {
    val intent = Intent(this, SnoozeReceiver::class.java).apply {
        action = NotificationConstants.ACTION_SNOOZE
        putExtra(NotificationConstants.EXTRA_SNOOZE_TYPE, type)
        putExtra(NotificationConstants.EXTRA_SNOOZE_ID, id)
        putExtra(NotificationConstants.EXTRA_SNOOZE_TITLE, title)
    }
    val pi = PendingIntent.getBroadcast(
        this,
        requestCode + 90_000,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    return NotificationCompat.Action.Builder(0, getString(R.string.notif_action_snooze), pi).build()
}

internal fun Context.scheduleSnoozeShow(type: String, id: String, title: String) {
    val intent = Intent(this, SnoozeReceiver::class.java).apply {
        action = NotificationConstants.ACTION_SNOOZE
        putExtra(NotificationConstants.EXTRA_SNOOZE_TYPE, type)
        putExtra(NotificationConstants.EXTRA_SNOOZE_ID, id)
        putExtra(NotificationConstants.EXTRA_SNOOZE_TITLE, title)
        putExtra("snooze_show", true)
    }
    val requestCode = "$type$id".hashCode() + 91_000
    val pi = PendingIntent.getBroadcast(
        this,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerAt = SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(NotificationConstants.SNOOZE_DELAY_MINUTES)
    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi)
}
