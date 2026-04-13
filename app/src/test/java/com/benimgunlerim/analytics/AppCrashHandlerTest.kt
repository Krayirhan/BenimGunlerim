package com.benimgunlerim.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AppCrashHandlerTest {
    @Test
    fun reportingHandler_recordsUncaughtExceptionAndDelegates() {
        val reports = mutableListOf<Throwable>()
        var delegatedThread: Thread? = null
        var delegatedError: Throwable? = null
        val reporter = object : ErrorReporter {
            override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
                reports.add(error)
                assertEquals("uncaught", context["type"])
            }

            override fun setUserProperty(key: String, value: String) = Unit
        }
        val previous = Thread.UncaughtExceptionHandler { thread, throwable ->
            delegatedThread = thread
            delegatedError = throwable
        }
        val handler = ReportingUncaughtExceptionHandler(reporter, previous)
        val error = IllegalStateException("boom")
        val thread = Thread.currentThread()

        handler.uncaughtException(thread, error)

        assertEquals(listOf(error), reports)
        assertSame(thread, delegatedThread)
        assertSame(error, delegatedError)
    }
}
