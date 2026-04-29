package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.domain.model.TaskCompletionState
import javax.inject.Inject

class SetTaskPendingUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.setCompletionStateById(taskId, TaskCompletionState.PENDING)
    }
}
