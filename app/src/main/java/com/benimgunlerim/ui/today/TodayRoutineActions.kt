package com.benimgunlerim.ui.today

import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.service.RewardDisplayService
import com.benimgunlerim.domain.usecase.ArchiveRoutineUseCase
import com.benimgunlerim.domain.usecase.SkipRoutineUseCase
import com.benimgunlerim.domain.usecase.ToggleRoutineUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineProgressUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineUseCase
import java.time.DayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal data class TodayRoutineDependencies(
    val dateTimeProvider: DateTimeProvider,
    val analyticsTracker: AnalyticsTracker,
    val feedbackManager: FeedbackManager,
    val achievementTracker: AchievementTracker,
    val rewardDisplayService: RewardDisplayService,
    val toggleRoutineUseCase: ToggleRoutineUseCase,
    val updateRoutineProgressUseCase: UpdateRoutineProgressUseCase,
    val updateRoutineUseCase: UpdateRoutineUseCase,
    val skipRoutineUseCase: SkipRoutineUseCase,
    val archiveRoutineUseCase: ArchiveRoutineUseCase,
)

/**
 * Routine actions for [TodayViewModel], extracted so the ViewModel itself
 * stays focused on state assembly. Reads current state via [uiStateValue] and
 * [routineEntitiesById], holds no StateFlow of its own.
 */
internal class TodayRoutineActions(
    private val scope: CoroutineScope,
    private val deps: TodayRoutineDependencies,
    private val routineEntitiesById: () -> Map<String, RoutineEntity>,
    private val uiStateValue: () -> TodayUiState,
    private val isTodayClosed: () -> Boolean,
) {
    private val inFlightToggleIds = mutableSetOf<String>()
    private val inFlightProgressIds = mutableSetOf<String>()

    fun toggleRoutine(routine: RoutineEntity, completedToday: Boolean) {
        toggleRoutine(routine.id, completedToday)
    }

    fun toggleRoutine(routineId: String, completedToday: Boolean) {
        if (isTodayClosed()) return
        val routine = routineEntitiesById()[routineId] ?: return
        if (!claim(inFlightToggleIds, routineId)) return
        scope.launch {
            try {
                val state = uiStateValue()
                val completedRoutinesBefore = state.completedRoutineIds.size
                val totalRoutines = state.routines.size

                val result = deps.toggleRoutineUseCase(
                    routine = routine,
                    completedToday = completedToday,
                    completedRoutineIds = state.completedRoutineIds,
                    allTodayRoutineIds = state.routines.map { it.id },
                ) ?: return@launch

                deps.analyticsTracker.track(AnalyticsEvent("routine_completed"))
                deps.rewardDisplayService.onRoutineCompleted(
                    routineId = routine.id,
                    grantResult = result.routineReward,
                )

                if (!completedToday) {
                    deps.achievementTracker.checkFirstRoutine()
                    if (completedRoutinesBefore == 0) {
                        deps.rewardDisplayService.emitMiniBanner("Bugünün ilk rutini tamam. Ritmin başladı.", "🔄")
                    } else if (completedRoutinesBefore + 1 == totalRoutines && totalRoutines > 1) {
                        deps.rewardDisplayService.emitAllRoutinesCompleted(state.currentStreak)
                    }
                }

                if (!result.allRoutinesBonus.alreadyGranted) {
                    deps.rewardDisplayService.onDailyBonusEarned(
                        xp = result.allRoutinesBonus.xpGranted,
                        gold = result.allRoutinesBonus.goldGranted,
                    )
                }
            } finally {
                release(inFlightToggleIds, routineId)
            }
        }
    }

    fun updateRoutineProgress(routine: RoutineEntity, value: Float, wasCompleted: Boolean) {
        updateRoutineProgress(routine.id, value, wasCompleted)
    }

    fun updateRoutineProgress(routineId: String, value: Float, wasCompleted: Boolean) {
        if (isTodayClosed()) return
        val routine = routineEntitiesById()[routineId] ?: return
        if (!claim(inFlightProgressIds, routineId)) return
        scope.launch {
            try {
                val state = uiStateValue()
                val result = deps.updateRoutineProgressUseCase(
                    routine = routine,
                    value = value,
                    wasCompleted = wasCompleted,
                    allTodayRoutineIds = state.routines.map { it.id },
                    completedRoutineIds = state.completedRoutineIds,
                ) ?: return@launch

                deps.analyticsTracker.track(AnalyticsEvent("routine_completed"))
                deps.rewardDisplayService.onRoutineCompleted(
                    routineId = routine.id,
                    grantResult = result.routineReward,
                )
                if (!result.allRoutinesBonus.alreadyGranted) {
                    deps.rewardDisplayService.onDailyBonusEarned(
                        xp = result.allRoutinesBonus.xpGranted,
                        gold = result.allRoutinesBonus.goldGranted,
                    )
                }
            } finally {
                release(inFlightProgressIds, routineId)
            }
        }
    }

    fun updateRoutine(
        routineId: String,
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
    ) {
        val routine = routineEntitiesById()[routineId] ?: return
        scope.launch {
            deps.updateRoutineUseCase(
                routine = routine,
                name = name,
                targetDays = targetDays,
                preferredTime = preferredTime,
                targetType = routine.targetType,
                targetValue = routine.targetValue,
                targetUnit = routine.targetUnit,
            )
        }
    }

    fun skipRoutine(routineId: String) {
        val routine = routineEntitiesById()[routineId] ?: return
        scope.launch { deps.skipRoutineUseCase(routine, deps.dateTimeProvider.today()) }
    }

    fun archiveRoutine(routineId: String) {
        val routine = routineEntitiesById()[routineId] ?: return
        scope.launch { deps.archiveRoutineUseCase(routine) }
    }

    private fun claim(ids: MutableSet<String>, id: String): Boolean = synchronized(ids) { ids.add(id) }

    private fun release(ids: MutableSet<String>, id: String) {
        synchronized(ids) { ids.remove(id) }
    }
}
