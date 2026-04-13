package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeTextTest {
    @Test
    fun normalizedTimeAcceptsValidValues() {
        assertEquals("09:05", "9:5".normalizedTimeOrNull())
        assertEquals("21:30", "21:30".normalizedTimeOrNull())
    }

    @Test
    fun normalizedTimeRejectsInvalidValues() {
        assertNull("24:00".normalizedTimeOrNull())
        assertNull("10:60".normalizedTimeOrNull())
        assertNull("1030".normalizedTimeOrNull())
        assertNull("".normalizedTimeOrNull())
    }
}
