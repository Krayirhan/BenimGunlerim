package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.SubTaskEntity
import javax.inject.Inject

class ToggleSubTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(subTask: SubTaskEntity) {
        taskRepository.toggleSubTask(subTask)
    }
}
