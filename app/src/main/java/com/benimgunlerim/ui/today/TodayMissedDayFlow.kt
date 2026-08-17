package com.benimgunlerim.ui.today

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.isScheduledFor
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.TaskCompletionState
import com.benimgunlerim.domain.usecase.ObserveDailyStateUseCase
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Builds the "yesterday, if still unclosed" summary shown as a nudge banner on Today.
 * Re-evaluated whenever [currentDateFlow] advances (i.e. at midnight).
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun buildMissedDayFlow(
    currentDateFlow: Flow<LocalDate>,
    observeDailyStateUseCase: ObserveDailyStateUseCase,
    taskRepository: TaskRepository,
    routineRepository: RoutineRepository,
    completionLogRepository: CompletionLogRepository,
): Flow<MissedDayData?> = currentDateFlow.flatMapLatest { today ->
    val yesterday = today.minusDays(1)
    observeDailyStateUseCase(yesterday).flatMapLatest { state ->
        if (state?.closedAt != null) {
            flowOf(null)
        } else {
            combine(
                taskRepository.observeByDate(yesterday),
                routineRepository.observeActive(),
                completionLogRepository.observeByDate(yesterday),
            ) { tasks, routines, logs ->
                val completedTasks = tasks.count { it.completionState == TaskCompletionState.COMPLETED.value }
                val pendingTasks = tasks.count { it.completionState == TaskCompletionState.PENDING.value }
                val scheduledRoutines = routines.filter { it.isScheduledFor(yesterday.dayOfWeek) }
                val completedRoutines = scheduledRoutines.count { r ->
                    logs.any {
                        it.entityType == CompletionEntityType.ROUTINE.value &&
                            it.entityId == r.id &&
                            it.status == CompletionStatus.COMPLETED.value
                    }
                }
                MissedDayData(
                    date = yesterday,
                    completedCount = completedTasks + completedRoutines,
                    totalCount = tasks.size + scheduledRoutines.size,
                    pendingTaskCount = pendingTasks,
                )
            }
        }
    }
}
