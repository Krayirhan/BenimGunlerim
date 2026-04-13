package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
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
}
