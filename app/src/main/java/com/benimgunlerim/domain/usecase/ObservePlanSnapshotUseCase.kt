package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val DAYS_AFTER_WEEK_START = 6L

data class PlanSnapshot(
    val tasksForDay: List<TaskEntity>,
    val overdueTasks: List<TaskEntity>,
    val weekTasks: List<TaskEntity> = emptyList(),
)

class ObservePlanSnapshotUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val dateTimeProvider: DateTimeProvider,
) {
    operator fun invoke(
        selectedDate: LocalDate,
        today: LocalDate = dateTimeProvider.today(),
    ): Flow<PlanSnapshot> {
        val weekStart = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
        val weekEnd = weekStart.plusDays(DAYS_AFTER_WEEK_START)
        return combine(
            taskRepository.observeRange(selectedDate, selectedDate),
            taskRepository.observeOverdue(today),
            taskRepository.observeRange(weekStart, weekEnd),
        ) { dayTasks, overdue, weekTasks ->
            PlanSnapshot(
                tasksForDay = dayTasks,
                overdueTasks = overdue,
                weekTasks = weekTasks,
            )
        }
    }
}
