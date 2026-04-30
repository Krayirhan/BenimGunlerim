package com.benimgunlerim.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeInputValidatorTest {

    @Test
    fun blank_isValid() {
        assertTrue(TimeInputValidator.isValid(""))
        assertTrue(TimeInputValidator.isValid("  "))
    }

    @Test
    fun valid_hhmm() {
        assertTrue(TimeInputValidator.isValid("00:00"))
        assertTrue(TimeInputValidator.isValid("09:30"))
        assertTrue(TimeInputValidator.isValid("23:59"))
    }

    @Test
    fun invalid_examples() {
        assertFalse(TimeInputValidator.isValid("24:00"))
        assertFalse(TimeInputValidator.isValid("12:60"))
        assertFalse(TimeInputValidator.isValid("99:99"))
        assertFalse(TimeInputValidator.isValid("9:30"))
        assertFalse(TimeInputValidator.isValid("12345"))
    }

    @Test
    fun sanitize_keepsDigitsAndColon_maxFiveChars() {
        assertEquals("09:30", TimeInputValidator.sanitize("09:30abc"))
        assertEquals("12:34", TimeInputValidator.sanitize("12:345"))
    }
}
