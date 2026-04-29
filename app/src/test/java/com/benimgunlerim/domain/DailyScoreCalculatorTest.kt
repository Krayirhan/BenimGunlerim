package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyScoreCalculatorTest {

    @Test
    fun calculate_zeroCompletion_notPerfect_baseXpOnly() {
        val score = DailyScoreCalculator.calculate(0f)

        assertFalse(score.isPerfectDay)
        assertEquals(GameEngine.XP_DAY_CLOSE, score.xp)
        assertEquals(0, score.gold)
    }

    @Test
    fun calculate_halfCompletion_notPerfect() {
        val score = DailyScoreCalculator.calculate(0.5f)

        assertFalse(score.isPerfectDay)
        assertEquals(GameEngine.XP_DAY_CLOSE, score.xp)
        assertEquals(0, score.gold)
    }

    @Test
    fun calculate_justBelowFull_notPerfect() {
        val score = DailyScoreCalculator.calculate(0.99f)

        assertFalse(score.isPerfectDay)
    }

    @Test
    fun calculate_exactlyFull_isPerfect() {
        val score = DailyScoreCalculator.calculate(1f)

        assertTrue(score.isPerfectDay)
        assertEquals(GameEngine.XP_DAY_CLOSE + GameEngine.XP_PERFECT_DAY, score.xp)
        assertEquals(GameEngine.GOLD_PERFECT_DAY, score.gold)
    }

    @Test
    fun calculate_overFull_isPerfect() {
        val score = DailyScoreCalculator.calculate(1.5f)

        assertTrue(score.isPerfectDay)
    }

    @Test
    fun calculate_perfectXp_greaterThanBaseXp() {
        val base = DailyScoreCalculator.calculate(0f).xp
        val perfect = DailyScoreCalculator.calculate(1f).xp

        assertTrue(perfect > base)
    }

    @Test
    fun isMissedDay_closedAtNotNull_returnsFalse() {
        assertFalse(DailyScoreCalculator.isMissedDay(1_000L, isInPast = true))
    }

    @Test
    fun isMissedDay_closedNullButNotInPast_returnsFalse() {
        assertFalse(DailyScoreCalculator.isMissedDay(null, isInPast = false))
    }

    @Test
    fun isMissedDay_closedNullAndInPast_returnsTrue() {
        assertTrue(DailyScoreCalculator.isMissedDay(null, isInPast = true))
    }
}
