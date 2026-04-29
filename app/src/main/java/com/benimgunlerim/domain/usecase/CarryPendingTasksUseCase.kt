package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.model.TaskCompletionState
import javax.inject.Inject

/**
 * Moves all overdue pending tasks to tomorrow.
 *
 * Returns the number of tasks carried.
 */
class CarryPendingTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val dateTimeProvider: DateTimeProvider,
    private val transactionRunner: DatabaseTransactionRunner,
) {
    suspend operator fun invoke(): Int {
        val today = dateTimeProvider.today()
        val tomorrow = today.plusDays(1)
        val pending = taskRepository.getPendingBefore(today)
        if (pending.isEmpty()) return 0
        transactionRunner.runInTransaction {
            pending.forEach { task ->
                taskRepository.update(
                    task.copy(
                        plannedDate = tomorrow.toString(),
                        postponedFromDate = task.postponedFromDate ?: task.plannedDate,
                        completionState = TaskCompletionState.PENDING.value,
                        completedAt = null,
                        updatedAt = dateTimeProvider.currentTimeMillis(),
                    ),
                )
                completionLogRepository.deleteForDate("task", task.id, task.plannedDate)
            }
        }
        return pending.size
    }
}
