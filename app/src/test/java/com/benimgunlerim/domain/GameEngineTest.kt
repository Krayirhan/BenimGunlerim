package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    // ── xpForTask ────────────────────────────────────────────────────────────

    @Test
    fun xpForTask_priority1_returnsLowXp() {
        assertEquals(GameEngine.XP_TASK_LOW, GameEngine.xpForTask(1))
    }

    @Test
    fun xpForTask_priority2_returnsNormalXp() {
        assertEquals(GameEngine.XP_TASK_NORMAL, GameEngine.xpForTask(2))
    }

    @Test
    fun xpForTask_priority3_returnsHighXp() {
        assertEquals(GameEngine.XP_TASK_HIGH, GameEngine.xpForTask(3))
    }

    @Test
    fun xpForTask_unknownPriority_returnsNormalXp() {
        assertEquals(GameEngine.XP_TASK_NORMAL, GameEngine.xpForTask(99))
    }

    // ── xpForRoutine ─────────────────────────────────────────────────────────

    @Test
    fun xpForRoutine_checkType_returnsCheckboxXp() {
        assertEquals(GameEngine.XP_ROUTINE_CHECKBOX, GameEngine.xpForRoutine("check"))
    }

    @Test
    fun xpForRoutine_goalType_returnsGoalXp() {
        assertEquals(GameEngine.XP_ROUTINE_GOAL, GameEngine.xpForRoutine("goal"))
    }

    @Test
    fun xpForRoutine_unknownType_returnsGoalXp() {
        assertEquals(GameEngine.XP_ROUTINE_GOAL, GameEngine.xpForRoutine("other"))
    }

    // ── XP values are positive ────────────────────────────────────────────────

    @Test
    fun xpConstants_areAllPositive() {
        assertTrue(GameEngine.XP_TASK_LOW > 0)
        assertTrue(GameEngine.XP_TASK_NORMAL > 0)
        assertTrue(GameEngine.XP_TASK_HIGH > 0)
        assertTrue(GameEngine.XP_ROUTINE_CHECKBOX > 0)
        assertTrue(GameEngine.XP_ROUTINE_GOAL > 0)
        assertTrue(GameEngine.XP_DAY_CLOSE > 0)
    }

    // ── XP ordering sanity ───────────────────────────────────────────────────

    @Test
    fun xpForTask_highPriorityGivesMoreThanLow() {
        assertTrue(GameEngine.xpForTask(3) > GameEngine.xpForTask(1))
    }

    // ── calculateLevel ───────────────────────────────────────────────────

    @Test
    fun calculateLevel_zeroXp_returnsLevel1() {
        val info = GameEngine.calculateLevel(0)
        assertEquals(1, info.level)
        assertEquals(0, info.currentXp)
    }

    @Test
    fun calculateLevel_exactFirstThreshold_advancesToLevel2() {
        // First threshold is 100 XP
        val info = GameEngine.calculateLevel(100)
        assertEquals(2, info.level)
        assertEquals(0, info.currentXp)
    }

    @Test
    fun calculateLevel_midFirstLevel_staysAtLevel1() {
        val info = GameEngine.calculateLevel(50)
        assertEquals(1, info.level)
        assertEquals(50, info.currentXp)
        assertEquals(100, info.xpForNextLevel)
    }

    @Test
    fun calculateLevel_highXp_returnsPositiveLevel() {
        val info = GameEngine.calculateLevel(10000)
        assertTrue(info.level > 10)
        assertEquals(10000, info.totalXp)
    }

    @Test
    fun calculateLevel_totalXpAlwaysPreserved() {
        listOf(0, 50, 100, 500, 2000, 9999).forEach { xp ->
            assertEquals(xp, GameEngine.calculateLevel(xp).totalXp)
        }
    }

    // ── clampHappiness ───────────────────────────────────────────────────

    @Test
    fun clampHappiness_aboveMax_returnsMax() {
        assertEquals(GameEngine.HAPPINESS_MAX, GameEngine.clampHappiness(200))
    }

    @Test
    fun clampHappiness_belowMin_returnsMin() {
        assertEquals(GameEngine.HAPPINESS_MIN, GameEngine.clampHappiness(-100))
    }

    @Test
    fun clampHappiness_inRange_returnsUnchanged() {
        assertEquals(60, GameEngine.clampHappiness(60))
    }

    // ── companionMood ────────────────────────────────────────────────────

    @Test
    fun companionMood_highHappiness_returnsEcstatic() {
        assertEquals("ecstatic", GameEngine.companionMood(80))
        assertEquals("ecstatic", GameEngine.companionMood(100))
    }

    @Test
    fun companionMood_mediumHappiness_returnsHappy() {
        assertEquals("happy", GameEngine.companionMood(60))
        assertEquals("happy", GameEngine.companionMood(79))
    }

    @Test
    fun companionMood_neutralHappiness_returnsNeutral() {
        assertEquals("neutral", GameEngine.companionMood(40))
        assertEquals("neutral", GameEngine.companionMood(59))
    }

    @Test
    fun companionMood_lowHappiness_returnsSad() {
        assertEquals("sad", GameEngine.companionMood(20))
        assertEquals("sad", GameEngine.companionMood(39))
    }

    // ── Day close XP cannot be 0 ─────────────────────────────────────────

    @Test
    fun dayCloseXp_isPositive() {
        assertTrue(GameEngine.XP_DAY_CLOSE > 0)
    }

    // ── companionMessage — deterministic via FixedIndexRandomProvider ─────

    @Test
    fun companionMessage_fullProgress_returnsCelebrationMessage() {
        val fixed = FixedIndexRandomProvider(0)
        val msg = GameEngine.companionMessage("neutral", streak = 0, progress = 1f, random = fixed)
        assertEquals("Bugün efsaneydin! 🎉", msg)
    }

    @Test
    fun companionMessage_ecstaticMood_returnsEcstaticMessage() {
        val fixed = FixedIndexRandomProvider(0)
        val msg = GameEngine.companionMessage("ecstatic", streak = 0, progress = 0f, random = fixed)
        assertEquals("Harika gidiyorsun! 🥳", msg)
    }

    @Test
    fun companionMessage_happyMood_returnsHappyMessage() {
        val fixed = FixedIndexRandomProvider(0)
        val msg = GameEngine.companionMessage("happy", streak = 0, progress = 0f, random = fixed)
        assertEquals("Güzel gidiyor! 💪", msg)
    }

    @Test
    fun companionMessage_longStreak_returnsStreakMessage() {
        val msg = GameEngine.companionMessage("neutral", streak = 7, progress = 0f)
        assertNotNull(msg)
        assertTrue(msg.contains("7"))
    }

    @Test
    fun companionMessage_halfProgress_returnsHalfwayMessage() {
        val fixed = FixedIndexRandomProvider(0)
        val msg = GameEngine.companionMessage("neutral", streak = 0, progress = 0.5f, random = fixed)
        assertEquals("Yarıyı geçtin! 💫", msg)
    }

    @Test
    fun companionMessage_smallProgress_returnsStarterMessage() {
        val fixed = FixedIndexRandomProvider(0)
        val msg = GameEngine.companionMessage("neutral", streak = 0, progress = 0.1f, random = fixed)
        assertEquals("Güzel başlangıç! 🌱", msg)
    }

    @Test
    fun companionMessage_zeroProgress_returnsMotivationMessage() {
        val fixed = FixedIndexRandomProvider(0)
        val msg = GameEngine.companionMessage("neutral", streak = 0, progress = 0f, random = fixed)
        assertEquals("Hadi maceraya başlayalım! ✨", msg)
    }

    @Test
    fun companionMessage_fixedIndex1_returnsDifferentMessage() {
        val fixed0 = FixedIndexRandomProvider(0)
        val fixed1 = FixedIndexRandomProvider(1)
        val msg0 = GameEngine.companionMessage("ecstatic", streak = 0, progress = 0f, random = fixed0)
        val msg1 = GameEngine.companionMessage("ecstatic", streak = 0, progress = 0f, random = fixed1)
        assertFalse(msg0 == msg1)
    }

    @Test
    fun companionMessage_fullProgressHasPriority_overMood() {
        // progress >= 1f branch should fire before mood check
        val fixed = FixedIndexRandomProvider(0)
        val msg = GameEngine.companionMessage("sad", streak = 0, progress = 1f, random = fixed)
        assertEquals("Bugün efsaneydin! 🎉", msg)
    }

    // ── clampHappiness boundaries ─────────────────────────────────────────

    @Test
    fun clampHappiness_exactMin_returnsMin() {
        assertEquals(GameEngine.HAPPINESS_MIN, GameEngine.clampHappiness(GameEngine.HAPPINESS_MIN))
    }

    @Test
    fun clampHappiness_exactMax_returnsMax() {
        assertEquals(GameEngine.HAPPINESS_MAX, GameEngine.clampHappiness(GameEngine.HAPPINESS_MAX))
    }

    @Test
    fun clampHappiness_oneAboveMax_returnsMax() {
        assertEquals(GameEngine.HAPPINESS_MAX, GameEngine.clampHappiness(GameEngine.HAPPINESS_MAX + 1))
    }

    @Test
    fun clampHappiness_oneBelowMin_returnsMin() {
        assertEquals(GameEngine.HAPPINESS_MIN, GameEngine.clampHappiness(GameEngine.HAPPINESS_MIN - 1))
    }

    // ── calculateLevel — threshold boundaries ────────────────────────────

    @Test
    fun calculateLevel_justBeforeLevel2_staysAtLevel1() {
        val info = GameEngine.calculateLevel(99)
        assertEquals(1, info.level)
        assertEquals(99, info.currentXp)
    }

    @Test
    fun calculateLevel_atLevel3Threshold_isLevel3() {
        // Level 1 requires 100 XP (0-99), Level 2 requires 100 XP (100-199), so 200 total XP = level 3
        val info = GameEngine.calculateLevel(200)
        assertEquals(3, info.level)
        assertEquals(0, info.currentXp)
    }

    @Test
    fun calculateLevel_xpForNextLevelIsPositive() {
        val info = GameEngine.calculateLevel(0)
        assertTrue(info.xpForNextLevel > 0)
    }
}
