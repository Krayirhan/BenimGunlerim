package com.benimgunlerim.notifications

import java.time.LocalDate
import java.time.LocalTime

/**
 * Contract for task reminder scheduling.
 *
 * Implementations are typically backed by [AlarmManager] in production and
 * no-op / recording fakes in tests.
 */
interface TaskReminderSchedulerContract {
    /**
     * Schedules (or reschedules) a reminder for the task identified by [taskId].
     * If [date]/[time] is in the past the call is silently ignored.
     */
    fun schedule(taskId: String, taskTitle: String, date: LocalDate, time: LocalTime)

    /**
     * Cancels a previously scheduled reminder for [taskId].
     * Safe to call even when no alarm exists.
     */
    fun cancel(taskId: String)
}
