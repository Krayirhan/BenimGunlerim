package com.benimgunlerim.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCrashHandler @Inject constructor(
    private val errorReporter: ErrorReporter,
) {
    fun install() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        if (previous is ReportingUncaughtExceptionHandler) return
        Thread.setDefaultUncaughtExceptionHandler(
            ReportingUncaughtExceptionHandler(errorReporter, previous),
        )
    }
}

internal class ReportingUncaughtExceptionHandler(
    private val errorReporter: ErrorReporter,
    private val previous: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        errorReporter.recordNonFatal(
            throwable,
            mapOf(
                "type" to "uncaught",
                "thread" to thread.name.take(80),
            ),
        )
        previous?.uncaughtException(thread, throwable)
    }
}
