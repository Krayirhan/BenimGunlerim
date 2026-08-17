package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import javax.inject.Inject

/**
 * Restores a previously deleted task as pending, together with any subtasks
 * that were snapshotted by [DeleteTaskUseCase] before the cascade delete.
 */
class RestoreTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val transactionRunner: DatabaseTransactionRunner,
) {
    suspend operator fun invoke(task: TaskEntity, subTasks: List<SubTaskEntity> = emptyList()) {
        transactionRunner.runInTransaction {
            taskRepository.restore(task)
            taskRepository.restoreSubTasks(subTasks)
        }
    }
}
