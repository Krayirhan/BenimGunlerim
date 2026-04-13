package com.benimgunlerim.data

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BenimGunlerimRepositoryTest {
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
        val logs = listOf(
            completionLog(today.minusDays(1)),
            completionLog(today.minusDays(2)),
        )

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

    private fun completionLog(
        date: LocalDate,
        entityType: String = "task",
        entityId: String = "task-1",
    ): CompletionLogEntity =
        CompletionLogEntity(
            id = "log-$entityType-$entityId-$date",
            entityType = entityType,
            entityId = entityId,
            date = date.toString(),
            completedAt = 0,
            status = "completed",
            note = null,
        )
}
