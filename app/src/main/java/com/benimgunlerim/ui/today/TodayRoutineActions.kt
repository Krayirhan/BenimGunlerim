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

/**
 * Routine actions for [TodayViewModel], extracted so the ViewModel itself
 * stays focused on state assembly. Reads current state via [uiStateValue] and
 * [routineEntitiesById], holds no StateFlow of its own.
 */
@Suppress("LongParameterList")
internal class TodayRoutineActions(
    private val scope: CoroutineScope,
    private val dateTimeProvider: DateTimeProvider,
    private val analyticsTracker: AnalyticsTracker,
    private val feedbackManager: FeedbackManager,
    private val achievementTracker: AchievementTracker,
    private val rewardDisplayService: RewardDisplayService,
    private val toggleRoutineUseCase: ToggleRoutineUseCase,
    private val updateRoutineProgressUseCase: UpdateRoutineProgressUseCase,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val skipRoutineUseCase: SkipRoutineUseCase,
    private val archiveRoutineUseCase: ArchiveRoutineUseCase,
    private val routineEntitiesById: () -> Map<String, RoutineEntity>,
    private val uiStateValue: () -> TodayUiState,
    private val isTodayClosed: () -> Boolean,
) {
    fun toggleRoutine(routine: RoutineEntity, completedToday: Boolean) {
        toggleRoutine(routine.id, completedToday)
    }

    fun toggleRoutine(routineId: String, completedToday: Boolean) {
        if (isTodayClosed()) return
        val routine = routineEntitiesById()[routineId] ?: return
        scope.launch {
            val state = uiStateValue()
            val completedRoutinesBefore = state.completedRoutineIds.size
            val totalRoutines = state.routines.size

            val result = toggleRoutineUseCase(
                routine = routine,
                completedToday = completedToday,
                completedRoutineIds = state.completedRoutineIds,
                allTodayRoutineIds = state.routines.map { it.id },
            ) ?: return@launch

            analyticsTracker.track(AnalyticsEvent("routine_completed"))
            feedbackManager.tapMedium()
            rewardDisplayService.onRoutineCompleted(
                routineId = routine.id,
                grantResult = result.routineReward,
            )

            if (!completedToday) {
                achievementTracker.checkFirstRoutine()
                if (completedRoutinesBefore == 0) {
                    rewardDisplayService.emitMiniBanner("Bugünün ilk rutini tamam. Ritmin başladı.", "🔄")
                } else if (completedRoutinesBefore + 1 == totalRoutines && totalRoutines > 1) {
                    rewardDisplayService.emitAllRoutinesCompleted(state.currentStreak)
                }
            }

            if (!result.allRoutinesBonus.alreadyGranted) {
                rewardDisplayService.onDailyBonusEarned(
                    xp = result.allRoutinesBonus.xpGranted,
                    gold = result.allRoutinesBonus.goldGranted,
                )
            }
        }
    }

    fun updateRoutineProgress(routine: RoutineEntity, value: Float, wasCompleted: Boolean) {
        updateRoutineProgress(routine.id, value, wasCompleted)
    }

    fun updateRoutineProgress(routineId: String, value: Float, wasCompleted: Boolean) {
        if (isTodayClosed()) return
        val routine = routineEntitiesById()[routineId] ?: return
        scope.launch {
            val state = uiStateValue()
            val result = updateRoutineProgressUseCase(
                routine = routine,
                value = value,
                wasCompleted = wasCompleted,
                allTodayRoutineIds = state.routines.map { it.id },
                completedRoutineIds = state.completedRoutineIds,
            ) ?: return@launch

            analyticsTracker.track(AnalyticsEvent("routine_completed"))
            feedbackManager.tapMedium()
            rewardDisplayService.onRoutineCompleted(
                routineId = routine.id,
                grantResult = result.routineReward,
            )
            if (!result.allRoutinesBonus.alreadyGranted) {
                rewardDisplayService.onDailyBonusEarned(
                    xp = result.allRoutinesBonus.xpGranted,
                    gold = result.allRoutinesBonus.goldGranted,
                )
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
            updateRoutineUseCase(
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
        scope.launch { skipRoutineUseCase(routine, dateTimeProvider.today()) }
    }

    fun archiveRoutine(routineId: String) {
        val routine = routineEntitiesById()[routineId] ?: return
        scope.launch { archiveRoutineUseCase(routine) }
    }
}
