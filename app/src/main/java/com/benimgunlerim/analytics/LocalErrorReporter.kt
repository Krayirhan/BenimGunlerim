package com.benimgunlerim.analytics

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BenimGunlerimError"

/**
 * Development-only [ErrorReporter] that logs to Logcat.
 * Replace or supplement with a crash-reporting SDK before production launch.
 */
@Singleton
class LocalErrorReporter @Inject constructor() : ErrorReporter {

    override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
        val contextStr = if (context.isEmpty()) "" else " | ctx=${context}"
        Log.e(TAG, "non-fatal: ${error.javaClass.simpleName}: ${error.message}$contextStr", error)
    }

    override fun setUserProperty(key: String, value: String) {
        Log.d(TAG, "property[$key]=$value")
    }
}
