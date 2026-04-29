package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Updates an existing task's fields and reschedules its reminder accordingly.
 *
 * Returns the updated [TaskEntity].
 */
class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskReminderScheduler: TaskReminderSchedulerContract,
    private val dateTimeProvider: DateTimeProvider,
) {
    suspend operator fun invoke(
        task: TaskEntity,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ): TaskEntity {
        val updated = taskRepository.updateFull(
            task = task,
            title = title,
            note = note,
            plannedDate = date,
            startTime = startTime,
            category = category,
            priority = priority,
            reminderTime = reminderTime,
        )
        // Always cancel the old alarm regardless of whether reminder changed
        taskReminderScheduler.cancel(updated.id)
        if (updated.reminderTime != null) {
            runCatching {
                taskReminderScheduler.schedule(
                    taskId = updated.id,
                    taskTitle = updated.title,
                    date = LocalDate.parse(updated.plannedDate),
                    time = LocalTime.parse(updated.reminderTime),
                )
            }
        }
        return updated
    }
}
