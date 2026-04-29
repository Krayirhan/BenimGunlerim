package com.benimgunlerim.notifications

import com.benimgunlerim.data.local.entity.RoutineEntity

/**
 * Contract for routine reminder scheduling.
 *
 * Implementations are typically backed by [AlarmManager] in production and
 * no-op / recording fakes in tests.
 */
interface RoutineReminderSchedulerContract {
    /**
     * Schedules a repeating daily reminder for [routine].
     * If no [RoutineEntity.preferredTime] is set, the call is silently ignored.
     */
    fun schedule(routine: RoutineEntity)

    /**
     * Cancels the repeating reminder for [routine].
     * Safe to call even when no alarm exists.
     */
    fun cancel(routine: RoutineEntity)
}
