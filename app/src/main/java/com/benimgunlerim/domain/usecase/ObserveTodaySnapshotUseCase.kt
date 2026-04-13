package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.currentStreak
import com.benimgunlerim.data.isScheduledFor
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.ProgressCalculator
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
    val gameState: UserPreferences,
    val todayState: DailyStateEntity?,
    val overdueTasks: List<TaskEntity>,
)

class ObserveTodaySnapshotUseCase @Inject constructor(
    private val repository: BenimGunlerimRepository,
    private val prefsRepository: UserPreferencesRepository,
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<TodaySnapshot> = combine(
        repository.observeTasks(today),
        repository.observeActiveRoutines(),
        repository.observeCompletionLogs(today),
        prefsRepository.preferences,
        combine(repository.observeTodayState(), repository.observeOverdueTasks()) { ds, ov -> ds to ov },
    ) { tasks, routines, logs, prefs, (todayState, overdue) ->
        val todaysRoutines = routines.filter { it.isScheduledFor(today.dayOfWeek) }
        val completedRoutineIds = logs.completedRoutineIds()
        val completedTasks = tasks.count { it.completionState == "completed" }
        val completedRoutines = todaysRoutines.count { it.id in completedRoutineIds }
        TodaySnapshot(
            tasks = tasks,
            routines = todaysRoutines,
            completionLogs = logs,
            completedRoutineIds = completedRoutineIds,
            progress = ProgressCalculator.dailyProgress(
                totalTasks = tasks.size,
                completedTasks = completedTasks,
                totalRoutines = todaysRoutines.size,
                completedRoutines = completedRoutines,
            ),
            currentStreak = logs.currentStreak(today),
            gameState = prefs,
            todayState = todayState,
            overdueTasks = overdue,
        )
    }
}

private fun List<CompletionLogEntity>.completedRoutineIds(): Set<String> =
    filter { it.entityType == "routine" && it.status == "completed" }
        .map { it.entityId }
        .toSet()
