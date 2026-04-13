package com.benimgunlerim.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

/**
 * Tests for ErrorReporter contract behaviour using a test double.
 * No Android framework required — pure JVM.
 */
class ErrorReporterTest {

    private class RecordingReporter : ErrorReporter {
        val recordings = mutableListOf<Pair<Throwable, Map<String, String>>>()
        val properties = mutableMapOf<String, String>()

        override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
            recordings.add(error to context)
        }

        override fun setUserProperty(key: String, value: String) {
            properties[key] = value
        }
    }

    @Test
    fun recordNonFatal_storesThrowableAndContext() {
        val reporter = RecordingReporter()
        val ex = IllegalStateException("test error")

        reporter.recordNonFatal(ex, mapOf("screen" to "today"))

        assertEquals(1, reporter.recordings.size)
        assertEquals(ex, reporter.recordings[0].first)
        assertEquals("today", reporter.recordings[0].second["screen"])
    }

    @Test
    fun recordNonFatal_acceptsEmptyContext() {
        val reporter = RecordingReporter()
        reporter.recordNonFatal(RuntimeException("no context"))
        assertEquals(1, reporter.recordings.size)
        assertTrue(reporter.recordings[0].second.isEmpty())
    }

    @Test
    fun setUserProperty_storesKeyValue() {
        val reporter = RecordingReporter()
        reporter.setUserProperty("theme", "dark")
        assertEquals("dark", reporter.properties["theme"])
    }

    @Test
    fun setUserProperty_overwritesPreviousValue() {
        val reporter = RecordingReporter()
        reporter.setUserProperty("theme", "light")
        reporter.setUserProperty("theme", "dark")
        assertEquals("dark", reporter.properties["theme"])
    }

    @Ignore("LocalErrorReporter calls android.util.Log which is a stub in JVM unit tests. Covered by instrumented tests.")
    @Test
    fun localErrorReporter_recordNonFatal_doesNotThrow() {
        val reporter = LocalErrorReporter()
        // Should log to Logcat without throwing even on a severe exception
        reporter.recordNonFatal(OutOfMemoryError("simulated"), mapOf("action" to "test"))
    }

    @Ignore("LocalErrorReporter calls android.util.Log which is a stub in JVM unit tests. Covered by instrumented tests.")
    @Test
    fun localErrorReporter_setUserProperty_doesNotThrow() {
        val reporter = LocalErrorReporter()
        reporter.setUserProperty("build_type", "debug")
    }
}
