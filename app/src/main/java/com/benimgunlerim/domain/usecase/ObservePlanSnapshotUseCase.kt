package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PlanSnapshot(
    val tasksForDay: List<TaskEntity>,
    val overdueTasks: List<TaskEntity>,
)

class ObservePlanSnapshotUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val dateTimeProvider: DateTimeProvider,
) {
    operator fun invoke(
        selectedDate: LocalDate,
        today: LocalDate = dateTimeProvider.today(),
    ): Flow<PlanSnapshot> = combine(
        taskRepository.observeRange(selectedDate, selectedDate),
        taskRepository.observeOverdue(today),
    ) { dayTasks, overdue ->
        PlanSnapshot(
            tasksForDay = dayTasks,
            overdueTasks = overdue,
        )
    }
}
