package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.isScheduledFor
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.ProgressCalculator
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.TaskCompletionState
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AutoCloseMissedDayUseCase @Inject constructor(
    private val dailyStateRepository: DailyStateRepository,
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val dateTimeProvider: DateTimeProvider,
) {
    suspend operator fun invoke(date: LocalDate) {
        val dateStr = date.toString()
        val existing = dailyStateRepository.getByDate(date)
        if (existing?.closedAt != null) return

        val dayTasks = taskRepository.observeByDate(date).first()
        val completedTasks = dayTasks.count { it.completionState == TaskCompletionState.COMPLETED.value }
        val activeRoutines = routineRepository.observeActive().first()
        val scheduledRoutines = activeRoutines.filter { it.isScheduledFor(date.dayOfWeek) }
        val dayLogs = completionLogRepository.observeByDate(date).first()
        val completedRoutines = scheduledRoutines.count { r ->
            dayLogs.any {
                it.entityType == CompletionEntityType.ROUTINE.value &&
                it.entityId == r.id &&
                it.status == CompletionStatus.COMPLETED.value
            }
        }

        val realRate = ProgressCalculator.dailyProgress(
            totalTasks = dayTasks.size,
            completedTasks = completedTasks,
            totalRoutines = scheduledRoutines.size,
            completedRoutines = completedRoutines,
        )

        dailyStateRepository.upsert(
            DailyStateEntity(
                date = dateStr,
                mood = "normal",
                energyLevel = 3,
                completionRate = realRate,
                note = null,
                reflection = null,
                dailyScore = (realRate * 100).toInt(),
                closedAt = dateTimeProvider.currentTimeMillis(),
            ),
        )
    }
}
