package com.benimgunlerim.domain.service

import com.benimgunlerim.domain.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LevelProgressionServiceTest {

    private lateinit var service: LevelProgressionService

    @Before
    fun setUp() {
        service = LevelProgressionService()
    }

    @Test
    fun calculateLevel_delegates_to_GameEngine() {
        val expected = GameEngine.calculateLevel(0)
        val result = service.calculateLevel(0)
        assertEquals(expected, result)
    }

    @Test
    fun calculateLevel_largeXp_matchesGameEngine() {
        val xp = 1_000
        assertEquals(GameEngine.calculateLevel(xp), service.calculateLevel(xp))
    }

    @Test
    fun checkLevelUp_noLevelUp_returnsNull() {
        // Gain 0 XP — level cannot change
        val result = service.checkLevelUp(previousXp = 0, gained = 0)
        assertNull(result)
    }

    @Test
    fun checkLevelUp_massiveGain_returnsNewLevel() {
        // Gaining enough XP to guarantee a level-up
        val result = service.checkLevelUp(previousXp = 0, gained = 10_000)
        val expected = GameEngine.calculateLevel(10_000)
        assertEquals(expected, result)
    }

    @Test
    fun checkLevelUp_justBelowThreshold_returnsNull() {
        // Find an XP value that is 1 below a level boundary
        val levelInfo = GameEngine.calculateLevel(0)
        val needed = levelInfo.xpForNextLevel - levelInfo.currentXp
        // Gain needed - 1 → still same level
        val result = service.checkLevelUp(previousXp = 0, gained = (needed - 1).coerceAtLeast(0))
        assertNull(result)
    }

    @Test
    fun checkLevelUp_exactlyAtThreshold_returnsLevelUp() {
        val levelInfo = GameEngine.calculateLevel(0)
        val needed = levelInfo.xpForNextLevel - levelInfo.currentXp
        val result = service.checkLevelUp(previousXp = 0, gained = needed)
        val expectedLevel = levelInfo.level + 1
        assertEquals(expectedLevel, result?.level)
    }
}
