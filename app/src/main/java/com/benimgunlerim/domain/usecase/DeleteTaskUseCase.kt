package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import javax.inject.Inject

/**
 * Deletes a task together with its completion logs and cancels its reminder.
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val taskReminderScheduler: TaskReminderSchedulerContract,
    private val transactionRunner: DatabaseTransactionRunner,
) {
    suspend operator fun invoke(task: TaskEntity) {
        // Cancel reminder first (outside transaction — not a DB operation)
        taskReminderScheduler.cancel(task.id)
        // Delete task row and its logs atomically
        transactionRunner.runInTransaction {
            taskRepository.delete(task)
            completionLogRepository.deleteForEntity(CompletionEntityType.TASK.value, task.id)
        }
    }
}
