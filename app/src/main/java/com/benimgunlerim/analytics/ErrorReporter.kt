package com.benimgunlerim.analytics

/**
 * Centralises non-fatal error recording and user-property tagging.
 *
 * Production implementation can delegate to Firebase Crashlytics or any other
 * crash provider. The local implementation logs to Logcat only so no data
 * leaves the device in development.
 */
interface ErrorReporter {
    /**
     * Record a non-fatal exception. [context] holds key/value pairs that will
     * appear alongside the report (e.g. screen name, action that failed).
     * Never include PII (task titles, user names, etc.) in [context].
     */
    fun recordNonFatal(error: Throwable, context: Map<String, String> = emptyMap())

    /**
     * Attaches a diagnostic property to every subsequent event/crash report
     * for the current session. Values must be non-PII (e.g. theme="dark").
     */
    fun setUserProperty(key: String, value: String)
}
