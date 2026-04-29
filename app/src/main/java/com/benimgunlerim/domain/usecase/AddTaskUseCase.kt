package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Creates a new task and schedules a reminder if [reminderTime] is provided.
 *
 * Returns the persisted [TaskEntity].
 */
class AddTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskReminderScheduler: TaskReminderSchedulerContract,
    private val dateTimeProvider: DateTimeProvider,
) {
    suspend operator fun invoke(
        title: String,
        date: LocalDate = dateTimeProvider.today(),
        note: String? = null,
        startTime: String? = null,
        category: String? = null,
        priority: Int = 2,
        reminderTime: String? = null,
    ): TaskEntity {
        val task = taskRepository.addTask(
            title = title,
            date = date,
            note = note,
            startTime = startTime,
            category = category,
            priority = priority,
            reminderTime = reminderTime,
        )
        if (task.reminderTime != null) {
            runCatching {
                taskReminderScheduler.schedule(
                    taskId = task.id,
                    taskTitle = task.title,
                    date = LocalDate.parse(task.plannedDate),
                    time = LocalTime.parse(task.reminderTime),
                )
            }
        }
        return task
    }
}
