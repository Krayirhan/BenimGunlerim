package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import javax.inject.Inject

class ObserveSubTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(taskId: String) = taskRepository.observeSubTasks(taskId)
}
