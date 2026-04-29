package com.benimgunlerim.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIdsTest {

    // ── Partition ranges ──────────────────────────────────────────────────────

    @Test
    fun forTask_isInTaskRange() {
        val id = NotificationIds.forTask("task-123")
        assertTrue("Expected ID in 10000..99999, got $id", id in 10_000..99_999)
    }

    @Test
    fun forRoutine_isInRoutineRange() {
        val id = NotificationIds.forRoutine("routine-abc")
        assertTrue("Expected ID in 300000..389999, got $id", id in 300_000..389_999)
    }

    @Test
    fun forSnoozeAction_isInSnoozeActionRange() {
        val id = NotificationIds.forSnoozeAction("task", "t1")
        assertTrue("Expected ID in 500000..589999, got $id", id in 500_000..589_999)
    }

    @Test
    fun forSnoozeReshow_isInSnoozeReshowRange() {
        val id = NotificationIds.forSnoozeReshow("task", "t1")
        assertTrue("Expected ID in 600000..689999, got $id", id in 600_000..689_999)
    }

    // ── Positive values ───────────────────────────────────────────────────────

    @Test
    fun forTask_isAlwaysPositive() {
        val ids = listOf("a", "b", "very-long-task-id-with-unicode-çş", "1", "999")
        ids.forEach { key ->
            assertTrue("Expected positive ID for '$key'", NotificationIds.forTask(key) > 0)
        }
    }

    @Test
    fun forRoutine_isAlwaysPositive() {
        val ids = listOf("routine-1", "morning-run", "abc123")
        ids.forEach { key ->
            assertTrue("Expected positive ID for '$key'", NotificationIds.forRoutine(key) > 0)
        }
    }

    // ── Determinism ───────────────────────────────────────────────────────────

    @Test
    fun forTask_isDeterministic() {
        val id1 = NotificationIds.forTask("my-task")
        val id2 = NotificationIds.forTask("my-task")
        assertEquals(id1, id2)
    }

    @Test
    fun forRoutine_isDeterministic() {
        val id1 = NotificationIds.forRoutine("my-routine")
        val id2 = NotificationIds.forRoutine("my-routine")
        assertEquals(id1, id2)
    }

    // ── No cross-partition collisions ─────────────────────────────────────────

    @Test
    fun taskAndRoutineIds_doNotCollide_forSameInput() {
        val taskId = NotificationIds.forTask("same-id")
        val routineId = NotificationIds.forRoutine("same-id")
        assertNotEquals(taskId, routineId)
    }

    @Test
    fun snoozeActionAndReshowIds_doNotCollide_forSameInput() {
        val action = NotificationIds.forSnoozeAction("task", "t1")
        val reshow = NotificationIds.forSnoozeReshow("task", "t1")
        assertNotEquals(action, reshow)
    }
}
