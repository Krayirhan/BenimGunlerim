package com.benimgunlerim.domain.service

import com.benimgunlerim.domain.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrantResultTest {

    @Test
    fun granted_exposesRewardFields() {
        val level = GameEngine.calculateLevel(1000)
        val result = GrantResult.Granted(xpGranted = 30, goldGranted = 10, leveledUp = level)

        assertEquals(30, result.xpGranted)
        assertEquals(10, result.goldGranted)
        assertEquals(level, result.leveledUp)
        assertFalse(result.alreadyGranted)
    }

    @Test
    fun alreadyGranted_exposesZeroValues() {
        val result = GrantResult.AlreadyGranted

        assertEquals(0, result.xpGranted)
        assertEquals(0, result.goldGranted)
        assertNull(result.leveledUp)
        assertTrue(result.alreadyGranted)
    }
}
