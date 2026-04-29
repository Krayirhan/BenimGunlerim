package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import javax.inject.Inject

class AddSubTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(taskId: String, title: String) {
        taskRepository.addSubTask(taskId, title)
    }
}
