package com.benimgunlerim.analytics

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsErrorReporter @Inject constructor(
    @ApplicationContext private val context: Context,
) : ErrorReporter {

    override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
        val crashlytics = crashlyticsOrNull() ?: return
        ErrorReporterContextPolicy.sanitize(context).forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(error)
    }

    override fun setUserProperty(key: String, value: String) {
        if (!ErrorReporterContextPolicy.isAllowedKey(key)) return
        crashlyticsOrNull()?.setCustomKey(key, value.take(ErrorReporterContextPolicy.MAX_VALUE_LENGTH))
    }

    private fun crashlyticsOrNull(): FirebaseCrashlytics? {
        if (FirebaseApp.getApps(context).isEmpty()) return null
        return FirebaseCrashlytics.getInstance()
    }
}
