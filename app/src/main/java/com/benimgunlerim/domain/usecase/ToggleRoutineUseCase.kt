package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.domain.service.GrantResult
import com.benimgunlerim.domain.service.RewardGrantService
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Toggles a routine's completion for a given date and grants the appropriate reward.
 *
 * Returns a [Result] when the routine was just completed and a reward was granted.
 * Returns null when un-completing or when the reward was already granted.
 */
class ToggleRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
    private val rewardGrantService: RewardGrantService,
    private val dateTimeProvider: DateTimeProvider,
) {
    data class Result(
        val routineReward: GrantResult,
        val allRoutinesBonus: GrantResult,
    )

    suspend operator fun invoke(
        routine: RoutineEntity,
        completedToday: Boolean,
        date: LocalDate = dateTimeProvider.today(),
        completedRoutineIds: Set<String> = emptySet(),
        allTodayRoutineIds: Collection<String> = emptyList(),
    ): Result? {
        if (completedToday) {
            routineRepository.deleteCompletionLog(routine.id, date)
            return null
        }

        routineRepository.writeCompletionLog(routine, date)

        val prefs = prefsRepository.preferences.first()
        val routineXp = GameEngine.xpForRoutine(routine.targetType)
        val routineReward = rewardGrantService.grantOnce(
            eventKey = "routine:${routine.id}:$date",
            xp = routineXp,
            gold = GameEngine.GOLD_ROUTINE_COMPLETE,
            happinessDelta = GameEngine.HAPPINESS_ROUTINE,
            currentXp = prefs.totalXp,
        )

        if (!routineReward.alreadyGranted) {
            prefsRepository.incrementRoutinesCompleted()
            achievementTracker.checkRoutineCount(prefs.totalRoutinesCompleted + 1)
        }

        // Check all-routines bonus
        val allRoutinesBonus = rewardGrantService.grantAllRoutinesBonusIfEligible(
            routineIds = allTodayRoutineIds,
            completedIds = completedRoutineIds,
            justToggledId = routine.id,
            currentXp = prefs.totalXp + routineReward.xpGranted,
        )

        return Result(routineReward = routineReward, allRoutinesBonus = allRoutinesBonus)
    }
}
