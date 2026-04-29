package com.benimgunlerim.domain.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEvaluationServiceTest {

    // ── achievementsForStreak ─────────────────────────────────────────────────

    @Test
    fun streak_belowFirstThreshold_returnsEmpty() {
        assertTrue(AchievementEvaluationService.achievementsForStreak(2).isEmpty())
    }

    @Test
    fun streak_exactly3_includesStreak3() {
        val ids = AchievementEvaluationService.achievementsForStreak(3)
        assertTrue("streak_3" in ids)
        assertEquals(1, ids.size)
    }

    @Test
    fun streak_30_includesAllMilestones() {
        val ids = AchievementEvaluationService.achievementsForStreak(30)
        assertEquals(listOf("streak_3", "streak_7", "streak_14", "streak_30"), ids)
    }

    // ── achievementsForTasks ──────────────────────────────────────────────────

    @Test
    fun tasks_below10_returnsEmpty() {
        assertTrue(AchievementEvaluationService.achievementsForTasks(9).isEmpty())
    }

    @Test
    fun tasks_exactly10_includesTasks10() {
        val ids = AchievementEvaluationService.achievementsForTasks(10)
        assertTrue("tasks_10" in ids)
        assertEquals(1, ids.size)
    }

    @Test
    fun tasks_500_includesAllMilestones() {
        val ids = AchievementEvaluationService.achievementsForTasks(500)
        assertEquals(listOf("tasks_10", "tasks_50", "tasks_100", "tasks_500"), ids)
    }

    // ── achievementsForRoutines ───────────────────────────────────────────────

    @Test
    fun routines_below10_returnsEmpty() {
        assertTrue(AchievementEvaluationService.achievementsForRoutines(5).isEmpty())
    }

    @Test
    fun routines_100_includesAllMilestones() {
        val ids = AchievementEvaluationService.achievementsForRoutines(100)
        assertEquals(listOf("routines_10", "routines_50", "routines_100"), ids)
    }

    // ── achievementsForLevel ──────────────────────────────────────────────────

    @Test
    fun level_below5_returnsEmpty() {
        assertTrue(AchievementEvaluationService.achievementsForLevel(4).isEmpty())
    }

    @Test
    fun level_10_includesLevel5AndLevel10() {
        val ids = AchievementEvaluationService.achievementsForLevel(10)
        assertEquals(listOf("level_5", "level_10"), ids)
    }

    // ── achievementsForGold ───────────────────────────────────────────────────

    @Test
    fun gold_below100_returnsEmpty() {
        assertTrue(AchievementEvaluationService.achievementsForGold(99).isEmpty())
    }

    @Test
    fun gold_1000_includesAllMilestones() {
        val ids = AchievementEvaluationService.achievementsForGold(1000)
        assertEquals(listOf("gold_100", "gold_500", "gold_1000"), ids)
    }

    // ── achievementsForDayClose ───────────────────────────────────────────────

    @Test
    fun dayClose_zero_returnsEmpty() {
        assertTrue(AchievementEvaluationService.achievementsForDayClose(0).isEmpty())
    }

    @Test
    fun dayClose_1_includesClose1() {
        val ids = AchievementEvaluationService.achievementsForDayClose(1)
        assertEquals(listOf("close_1"), ids)
    }

    @Test
    fun dayClose_30_includesAllMilestones() {
        val ids = AchievementEvaluationService.achievementsForDayClose(30)
        assertEquals(listOf("close_1", "close_10", "close_30"), ids)
    }

    // ── achievementsForHappiness ──────────────────────────────────────────────

    @Test
    fun happiness_below90_returnsEmpty() {
        assertTrue(AchievementEvaluationService.achievementsForHappiness(89).isEmpty())
    }

    @Test
    fun happiness_90_includesCompanionHappy() {
        val ids = AchievementEvaluationService.achievementsForHappiness(90)
        assertEquals(listOf("companion_happy"), ids)
    }

    @Test
    fun happiness_100_includesCompanionHappy() {
        val ids = AchievementEvaluationService.achievementsForHappiness(100)
        assertTrue("companion_happy" in ids)
    }
}
