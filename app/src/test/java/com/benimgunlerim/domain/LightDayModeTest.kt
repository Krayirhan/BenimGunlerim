package com.benimgunlerim.domain

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LightDayModeTest {

    private val today = LocalDate.of(2026, 8, 16)

    @Test
    fun lightDayModeDate_equalsToday_isActive() {
        assertTrue(LightDayMode.isActiveOn(today.toString(), today))
    }

    @Test
    fun lightDayModeDate_yesterday_isNotActive() {
        assertFalse(LightDayMode.isActiveOn(today.minusDays(1).toString(), today))
    }

    @Test
    fun lightDayModeDate_tomorrow_isNotActive() {
        assertFalse(LightDayMode.isActiveOn(today.plusDays(1).toString(), today))
    }

    @Test
    fun lightDayModeDate_blank_isNotActive() {
        assertFalse(LightDayMode.isActiveOn("", today))
    }
}
