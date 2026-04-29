package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import java.time.LocalDate
import javax.inject.Inject

class MoveTaskToDateUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(task: TaskEntity, date: LocalDate) {
        taskRepository.moveToDate(task, date)
    }
}
