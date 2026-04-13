package com.benimgunlerim.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bir bildirimin gösterilip gösterilmeyeceğine karar veren merkezi politika sınıfı.
 *
 * Kontrol sırası:
 * 1. POST_NOTIFICATIONS izni var mı?
 * 2. Notification mode "off" mu?
 * 3. Sessiz saatler aktif mi?
 *
 * BroadcastReceiver'dan güvenli şekilde çağrılabilmesi için
 * sadece synchronous işlemler kullanılır (SharedPreferences).
 */
@Singleton
class ReminderPolicy @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val PREFS_NAME = "reminder_policy_cache"
        private const val KEY_NOTIFICATION_MODE = "notification_mode"
        private const val KEY_QH_ENABLED = "quiet_hours_enabled"
        private const val KEY_QH_START = "quiet_hours_start"
        private const val KEY_QH_END = "quiet_hours_end"
        private const val DEFAULT_QH_START = "22:00"
        private const val DEFAULT_QH_END = "07:00"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** SettingsViewModel tarafından tercihler değiştiğinde çağrılır. */
    fun updateCache(
        notificationMode: String,
        quietHoursEnabled: Boolean,
        quietHoursStart: String,
        quietHoursEnd: String,
    ) {
        prefs.edit()
            .putString(KEY_NOTIFICATION_MODE, notificationMode)
            .putBoolean(KEY_QH_ENABLED, quietHoursEnabled)
            .putString(KEY_QH_START, quietHoursStart)
            .putString(KEY_QH_END, quietHoursEnd)
            .apply()
    }

    /** Bildirim gösterilebilir mi? (BroadcastReceiver'da güvenli) */
    fun canShowNotification(): Boolean {
        if (!hasPostNotificationsPermission()) return false
        if (isNotificationModeOff()) return false
        if (isInQuietHours()) return false
        return true
    }

    fun hasPostNotificationsPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun isNotificationModeOff(): Boolean =
        prefs.getString(KEY_NOTIFICATION_MODE, "normal") == "off"

    fun isInQuietHours(): Boolean {
        val enabled = prefs.getBoolean(KEY_QH_ENABLED, false)
        if (!enabled) return false
        val startStr = prefs.getString(KEY_QH_START, DEFAULT_QH_START) ?: DEFAULT_QH_START
        val endStr = prefs.getString(KEY_QH_END, DEFAULT_QH_END) ?: DEFAULT_QH_END
        return quietHoursActive(java.time.LocalTime.now(), startStr, endStr)
    }
}

/** Pure function — testable without Android Context. */
internal fun quietHoursActive(
    now: java.time.LocalTime,
    startStr: String,
    endStr: String,
): Boolean = try {
    val fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    val start = java.time.LocalTime.parse(startStr, fmt)
    val end = java.time.LocalTime.parse(endStr, fmt)
    // Geceyi geçen aralık: 22:00 – 07:00
    if (start.isBefore(end)) now >= start && now <= end else now >= start || now <= end
} catch (_: Exception) {
    false
}
