package com.benimgunlerim.data

import com.benimgunlerim.data.local.entity.RoutineEntity
import java.time.DayOfWeek

// ── RoutineEntity extensions ──────────────────────────────────────────────────

/**
 * Parses the comma-separated [RoutineEntity.targetDays] string into a
 * [Set] of [DayOfWeek] values, silently ignoring any unrecognised tokens.
 *
 * Example: `"MONDAY,WEDNESDAY,INVALID"` → `{MONDAY, WEDNESDAY}`
 */
fun RoutineEntity.targetDaySet(): Set<DayOfWeek> =
    targetDays.split(",")
        .mapNotNull { token ->
            runCatching { DayOfWeek.valueOf(token) }.getOrNull()
        }
        .toSet()

/**
 * Returns `true` when this routine is scheduled on the given [day].
 */
fun RoutineEntity.isScheduledFor(day: DayOfWeek): Boolean = day in targetDaySet()

// ── DayOfWeek extension ───────────────────────────────────────────────────────

/**
 * Returns a short Turkish abbreviation for the day of the week.
 *
 * Used in UI chip labels and day-summary strings.
 */
fun DayOfWeek.shortTr(): String = when (this) {
    DayOfWeek.MONDAY -> "Pzt"
    DayOfWeek.TUESDAY -> "Sal"
    DayOfWeek.WEDNESDAY -> "Çar"
    DayOfWeek.THURSDAY -> "Per"
    DayOfWeek.FRIDAY -> "Cum"
    DayOfWeek.SATURDAY -> "Cmt"
    DayOfWeek.SUNDAY -> "Paz"
}
