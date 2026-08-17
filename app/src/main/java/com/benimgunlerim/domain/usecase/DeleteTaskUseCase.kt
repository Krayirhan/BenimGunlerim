package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import javax.inject.Inject

/**
 * Deletes a task together with its completion logs and cancels its reminder.
 *
 * Room's `ON DELETE CASCADE` removes subtasks along with the task, so the
 * subtask rows are snapshotted before the delete transaction runs — the
 * returned list lets the caller restore them via [RestoreTaskUseCase] on undo.
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val taskReminderScheduler: TaskReminderSchedulerContract,
    private val transactionRunner: DatabaseTransactionRunner,
) {
    suspend operator fun invoke(task: TaskEntity): List<SubTaskEntity> {
        // Cancel reminder first (outside transaction — not a DB operation)
        taskReminderScheduler.cancel(task.id)
        // Snapshot subtasks and delete task + logs atomically, so the snapshot
        // can't drift from what actually gets cascade-deleted.
        var subTasks: List<SubTaskEntity> = emptyList()
        transactionRunner.runInTransaction {
            subTasks = taskRepository.getSubTasks(task.id)
            taskRepository.delete(task)
            completionLogRepository.deleteForEntity(CompletionEntityType.TASK.value, task.id)
        }
        return subTasks
    }
}
