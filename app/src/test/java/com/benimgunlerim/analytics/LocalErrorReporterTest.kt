package com.benimgunlerim.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalErrorReporterTest {

    private val reporter = LocalErrorReporter(context = null)

    // ── getStoredReports ───────────────────────────────────────────────────

    @Test
    fun getStoredReports_returnsEmptyList_whenContextIsNull() {
        assertTrue(reporter.getStoredReports().isEmpty())
    }

    // ── parseReportLine ────────────────────────────────────────────────────

    @Test
    fun parseReportLine_fullLine_parsesAllFields() {
        val result = reporter.parseReportLine(
            "1714000000000|java.lang.NullPointerException|something was null|type=uncaught,thread=main",
        )
        assertNotNull(result)
        assertEquals(1714000000000L, result!!.timestampMs)
        assertEquals("java.lang.NullPointerException", result.exceptionClass)
        assertEquals("something was null", result.message)
        assertEquals("type=uncaught,thread=main", result.contextStr)
    }

    @Test
    fun parseReportLine_missingContext_setsEmptyContextStr() {
        val result = reporter.parseReportLine("1000|com.example.Ex|oh no")
        assertNotNull(result)
        assertEquals("", result!!.contextStr)
    }

    @Test
    fun parseReportLine_tooFewParts_returnsNull() {
        assertNull(reporter.parseReportLine("1000|OnlyTwo"))
    }

    @Test
    fun parseReportLine_emptyString_returnsNull() {
        assertNull(reporter.parseReportLine(""))
    }

    @Test
    fun parseReportLine_invalidTimestamp_returnsNull() {
        assertNull(reporter.parseReportLine("notANumber|SomeEx|message"))
    }

    @Test
    fun parseReportLine_messageWithPipes_preservesMessageAndContext() {
        // limit=4 means the 4th segment absorbs any extra pipes
        val result = reporter.parseReportLine("2000|Ex|msg|ctx=a|b=c")
        assertNotNull(result)
        assertEquals("ctx=a|b=c", result!!.contextStr)
    }

    // ── StoredErrorReport equality ─────────────────────────────────────────

    @Test
    fun storedErrorReport_equalityIsValueBased() {
        val a = StoredErrorReport(100L, "Ex", "msg", "ctx")
        val b = StoredErrorReport(100L, "Ex", "msg", "ctx")
        assertEquals(a, b)
    }
}
