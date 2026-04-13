package com.benimgunlerim.notifications

import java.time.LocalTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPolicyTest {

    // ── Overnight range (22:00 – 07:00) ──────────────────────────────────

    @Test
    fun quietHours_overnight_justAfterStart_returnsTrue() {
        assertTrue(quietHoursActive(LocalTime.of(22, 30), "22:00", "07:00"))
    }

    @Test
    fun quietHours_overnight_midnight_returnsTrue() {
        assertTrue(quietHoursActive(LocalTime.of(0, 0), "22:00", "07:00"))
    }

    @Test
    fun quietHours_overnight_justBeforeEnd_returnsTrue() {
        assertTrue(quietHoursActive(LocalTime.of(6, 59), "22:00", "07:00"))
    }

    @Test
    fun quietHours_overnight_atEnd_returnsTrue() {
        assertTrue(quietHoursActive(LocalTime.of(7, 0), "22:00", "07:00"))
    }

    @Test
    fun quietHours_overnight_middleOfDay_returnsFalse() {
        assertFalse(quietHoursActive(LocalTime.of(12, 0), "22:00", "07:00"))
    }

    @Test
    fun quietHours_overnight_justBeforeStart_returnsFalse() {
        assertFalse(quietHoursActive(LocalTime.of(21, 59), "22:00", "07:00"))
    }

    // ── Same-day range (08:00 – 12:00) ───────────────────────────────────

    @Test
    fun quietHours_sameDay_atStart_returnsTrue() {
        assertTrue(quietHoursActive(LocalTime.of(8, 0), "08:00", "12:00"))
    }

    @Test
    fun quietHours_sameDay_middle_returnsTrue() {
        assertTrue(quietHoursActive(LocalTime.of(10, 30), "08:00", "12:00"))
    }

    @Test
    fun quietHours_sameDay_beforeRange_returnsFalse() {
        assertFalse(quietHoursActive(LocalTime.of(7, 59), "08:00", "12:00"))
    }

    @Test
    fun quietHours_sameDay_afterRange_returnsFalse() {
        assertFalse(quietHoursActive(LocalTime.of(13, 0), "08:00", "12:00"))
    }

    // ── Invalid input ─────────────────────────────────────────────────────

    @Test
    fun quietHours_invalidStartString_returnsFalse() {
        assertFalse(quietHoursActive(LocalTime.of(23, 0), "notaTime", "07:00"))
    }

    @Test
    fun quietHours_invalidEndString_returnsFalse() {
        assertFalse(quietHoursActive(LocalTime.of(23, 0), "22:00", ""))
    }
}
