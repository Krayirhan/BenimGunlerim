package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import javax.inject.Inject

/**
 * Restores a previously deleted task as pending.
 */
class RestoreTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(task: TaskEntity) = taskRepository.restore(task)
}
