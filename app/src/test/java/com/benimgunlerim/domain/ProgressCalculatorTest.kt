package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {
    @Test
    fun dailyProgressReturnsZeroWhenThereAreNoItems() {
        assertEquals(0f, ProgressCalculator.dailyProgress(0, 0, 0, 0), 0.001f)
    }

    @Test
    fun dailyProgressCombinesTasksAndRoutines() {
        val progress = ProgressCalculator.dailyProgress(
            totalTasks = 3,
            completedTasks = 2,
            totalRoutines = 1,
            completedRoutines = 1,
        )

        assertEquals(0.75f, progress, 0.001f)
    }

    @Test
    fun dailyProgressClampsCompletedCount() {
        val progress = ProgressCalculator.dailyProgress(
            totalTasks = 1,
            completedTasks = 3,
            totalRoutines = 1,
            completedRoutines = 3,
        )

        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun dailyProgress_onlyTasks_noRoutines() {
        val progress = ProgressCalculator.dailyProgress(
            totalTasks = 4,
            completedTasks = 1,
            totalRoutines = 0,
            completedRoutines = 0,
        )
        assertEquals(0.25f, progress, 0.001f)
    }

    @Test
    fun dailyProgress_onlyRoutines_noTasks() {
        val progress = ProgressCalculator.dailyProgress(
            totalTasks = 0,
            completedTasks = 0,
            totalRoutines = 2,
            completedRoutines = 1,
        )
        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun dailyProgress_nothingCompleted_returnsZero() {
        val progress = ProgressCalculator.dailyProgress(
            totalTasks = 5,
            completedTasks = 0,
            totalRoutines = 3,
            completedRoutines = 0,
        )
        assertEquals(0f, progress, 0.001f)
    }
}
