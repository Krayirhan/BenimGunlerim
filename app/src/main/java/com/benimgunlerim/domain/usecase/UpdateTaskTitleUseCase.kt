package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import javax.inject.Inject

class UpdateTaskTitleUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(task: TaskEntity, title: String) {
        taskRepository.updateTitle(task, title)
    }
}
