package com.benimgunlerim.analytics

import android.content.Context
import android.util.Log
import com.benimgunlerim.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BenimGunlerimError"
private const val PREFS_NAME = "local_error_reports"
private const val KEY_REPORTS = "reports"
private const val MAX_REPORTS = 20

/**
 * Local [ErrorReporter] implementation — the only one wired in the app.
 *
 * Debug builds log to Logcat. All builds keep a bounded on-device history of
 * non-fatal reports so diagnostics exist without sending user data off-device.
 */
@Singleton
class LocalErrorReporter @Inject constructor(
    @ApplicationContext private val context: Context?,
) : ErrorReporter {

    override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
        val safeContext = ErrorReporterContextPolicy.sanitize(context)
        val contextStr = if (safeContext.isEmpty()) "" else " | ctx=${safeContext}"
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "non-fatal: ${error.javaClass.simpleName}: ${error.message}$contextStr", error)
        }
        persistReport(error, safeContext)
    }

    override fun setUserProperty(key: String, value: String) {
        if (!ErrorReporterContextPolicy.isAllowedKey(key)) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "property[$key]=$value")
        }
    }

    private fun persistReport(error: Throwable, context: Map<String, String>) {
        val prefs = this.context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val current = prefs.getStringSet(KEY_REPORTS, emptySet()).orEmpty()
        val report = buildString {
            append(System.currentTimeMillis())
            append('|')
            append(error.javaClass.name)
            append('|')
            append(error.message.orEmpty().take(160))
            if (context.isNotEmpty()) {
                append('|')
                append(context.entries.joinToString(",") { "${it.key}=${it.value.take(80)}" })
            }
        }
        val next = (current + report)
            .sortedByDescending { it.substringBefore('|').toLongOrNull() ?: 0L }
            .take(MAX_REPORTS)
            .toSet()
        prefs.edit().putStringSet(KEY_REPORTS, next).apply()
    }

    /** Returns on-device stored reports, newest first. Safe to call from any thread. */
    fun getStoredReports(): List<StoredErrorReport> {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?: return emptyList()
        return prefs.getStringSet(KEY_REPORTS, emptySet()).orEmpty()
            .mapNotNull { parseReportLine(it) }
            .sortedByDescending { it.timestampMs }
    }

    /**
     * Parses a single stored report line.
     * Format: `timestamp|exceptionClass|message[|contextStr]`
     */
    internal fun parseReportLine(raw: String): StoredErrorReport? {
        val parts = raw.split('|', limit = 4)
        if (parts.size < 3) return null
        return StoredErrorReport(
            timestampMs = parts[0].toLongOrNull() ?: return null,
            exceptionClass = parts[1],
            message = parts[2],
            contextStr = parts.getOrElse(3) { "" },
        )
    }
}

data class StoredErrorReport(
    val timestampMs: Long,
    val exceptionClass: String,
    val message: String,
    val contextStr: String,
)
