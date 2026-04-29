package com.benimgunlerim.data

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletionLogExtensionsTest {
    @Test
    fun currentStreakCountsBackwardsFromToday() {
        val today = LocalDate.of(2026, 4, 12)
        val logs = listOf(
            completionLog(today),
            completionLog(today.minusDays(1)),
            completionLog(today.minusDays(2)),
            completionLog(today.minusDays(4)),
        )
        assertEquals(3, logs.currentStreak(today))
    }

    @Test
    fun currentStreakReturnsZeroWhenTodayIsMissing() {
        val today = LocalDate.of(2026, 4, 12)
        val logs = listOf(completionLog(today.minusDays(1)), completionLog(today.minusDays(2)))
        assertEquals(0, logs.currentStreak(today))
    }

    @Test
    fun currentStreakForEntityIgnoresOtherEntities() {
        val today = LocalDate.of(2026, 4, 12)
        val logs = listOf(
            completionLog(today, entityType = "routine", entityId = "routine-1"),
            completionLog(today.minusDays(1), entityType = "routine", entityId = "routine-1"),
            completionLog(today.minusDays(2), entityType = "routine", entityId = "routine-2"),
            completionLog(today.minusDays(2), entityType = "task", entityId = "routine-1"),
        )
        assertEquals(2, logs.currentStreakForEntity("routine", "routine-1", today))
    }

    @Test
    fun routineTargetDaysAreParsedSafely() {
        val routine = RoutineEntity(
            id = "routine-1",
            name = "Su iç",
            description = null,
            targetDays = "MONDAY,WEDNESDAY,INVALID",
            preferredTime = "09:00",
            color = null,
            isArchived = false,
            createdAt = 0,
            updatedAt = 0,
        )
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), routine.targetDaySet())
        assertTrue(routine.isScheduledFor(DayOfWeek.MONDAY))
    }

    @Test
    fun routineIsScheduledFor_returnsFalseWhenTokenHasWhitespace() {
        val routine = RoutineEntity(
            id = "routine-1",
            name = "Test",
            description = null,
            targetDays = " MONDAY ,TUESDAY",
            preferredTime = null,
            color = null,
            isArchived = false,
            createdAt = 0,
            updatedAt = 0,
        )
        assertFalse(routine.isScheduledFor(DayOfWeek.MONDAY))
        assertTrue(routine.targetDaySet().contains(DayOfWeek.TUESDAY))
    }

    @Test
    fun shortTr_returnsCapitalizedShortName() {
        val label = DayOfWeek.MONDAY.shortTr()
        assertTrue(label.isNotBlank())
        assertEquals(label.first().uppercaseChar(), label.first())
    }

    @Test
    fun currentStreak_ignoresInvalidDatesAndNonCompletedStatuses() {
        val today = LocalDate.of(2026, 4, 12)
        val logs = listOf(
            completionLog(today, status = "completed"),
            completionLog(today.minusDays(1), status = "skipped"),
            completionLog(today.minusDays(1), status = "completed").copy(date = "invalid-date"),
            completionLog(today.minusDays(2), status = "completed"),
        )
        assertEquals(1, logs.currentStreak(today))
    }

    private fun completionLog(
        date: LocalDate,
        entityType: String = "task",
        entityId: String = "task-1",
        status: String = "completed",
    ): CompletionLogEntity =
        CompletionLogEntity(
            id = "log-$entityType-$entityId-$date",
            entityType = entityType,
            entityId = entityId,
            date = date.toString(),
            completedAt = 0,
            status = status,
            note = null,
        )
}
