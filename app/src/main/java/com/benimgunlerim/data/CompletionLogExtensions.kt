package com.benimgunlerim.data

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.model.CompletionStatus
import java.time.LocalDate

fun List<CompletionLogEntity>.currentStreak(today: LocalDate = LocalDate.now()): Int {
    val completedDates = filter { it.status == CompletionStatus.COMPLETED.value }.mapNotNull {
        runCatching { LocalDate.parse(it.date) }.getOrNull()
    }.toSet()
    var streak = 0
    var cursor = today
    while (cursor in completedDates) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}

fun List<CompletionLogEntity>.currentStreakForEntity(
    entityType: String,
    entityId: String,
    today: LocalDate = LocalDate.now(),
): Int =
    filter { it.entityType == entityType && it.entityId == entityId }.currentStreak(today)
