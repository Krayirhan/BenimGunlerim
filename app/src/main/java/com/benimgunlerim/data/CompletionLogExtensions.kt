package com.benimgunlerim.data

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.model.CompletionStatus
import java.time.LocalDate

fun List<CompletionLogEntity>.currentStreak(today: LocalDate): Int {
    val completedDates = filter { it.status == CompletionStatus.COMPLETED.value }.mapNotNull {
        runCatching { LocalDate.parse(it.date) }.getOrNull()
    }.toSet()

    return if (today in completedDates) {
        var streak = 0
        var cursor = today
        while (cursor in completedDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        streak
    } else if (today.minusDays(1) in completedDates) {
        var streak = 0
        var cursor = today.minusDays(1)
        while (cursor in completedDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        streak
    } else {
        0
    }
}

fun List<CompletionLogEntity>.currentStreakForEntity(
    entityType: String,
    entityId: String,
    today: LocalDate,
): Int =
    filter { it.entityType == entityType && it.entityId == entityId }.currentStreak(today)
