package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.currentStreak
import com.benimgunlerim.data.currentStreakForEntity
import com.benimgunlerim.data.isScheduledFor
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.ProgressCalculator
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.TaskCompletionState
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class TodaySnapshot(
    val tasks: List<TaskEntity>,
    val routines: List<RoutineEntity>,
    val completionLogs: List<CompletionLogEntity>,
    val completedRoutineIds: Set<String>,
    val progress: Float,
    val currentStreak: Int,
    val routineStreaks: Map<String, Int> = emptyMap(),
    val gameState: UserPreferences,
    val todayState: DailyStateEntity?,
    val overdueTasks: List<TaskEntity>,
)

class ObserveTodaySnapshotUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val dateTimeProvider: DateTimeProvider,
) {
    operator fun invoke(today: LocalDate = dateTimeProvider.today()): Flow<TodaySnapshot> = combine(
        taskRepository.observeByDate(today),
        routineRepository.observeActive(),
        combine(
            completionLogRepository.observeByDate(today),
            completionLogRepository.observeAll(),
        ) { todayLogs, allLogs -> todayLogs to allLogs },
        prefsRepository.preferences,
        combine(dailyStateRepository.observeToday(), taskRepository.observeOverdue(today)) { ds, ov -> ds to ov },
    ) { tasks, routines, logsPair, prefs, (todayState, overdue) ->
        val (todayLogs, allLogs) = logsPair
        val todaysRoutines = routines.filter { it.isScheduledFor(today.dayOfWeek) }
        val completedRoutineIds = todayLogs.completedRoutineIds()
        val completedTasks = tasks.count { it.completionState == TaskCompletionState.COMPLETED.value }
        val completedRoutines = todaysRoutines.count { it.id in completedRoutineIds }
        TodaySnapshot(
            tasks = tasks,
            routines = todaysRoutines,
            completionLogs = todayLogs,
            completedRoutineIds = completedRoutineIds,
            progress = ProgressCalculator.dailyProgress(
                totalTasks = tasks.size,
                completedTasks = completedTasks,
                totalRoutines = todaysRoutines.size,
                completedRoutines = completedRoutines,
            ),
            currentStreak = allLogs.currentStreak(today),
            routineStreaks = todaysRoutines.associate { routine ->
                routine.id to allLogs.currentStreakForEntity(
                    CompletionEntityType.ROUTINE.value,
                    routine.id,
                    today,
                )
            },
            gameState = prefs,
            todayState = todayState,
            overdueTasks = overdue,
        )
    }
}

private fun List<CompletionLogEntity>.completedRoutineIds(): Set<String> =
    filter { it.entityType == CompletionEntityType.ROUTINE.value && it.status == CompletionStatus.COMPLETED.value }
        .map { it.entityId }
        .toSet()
