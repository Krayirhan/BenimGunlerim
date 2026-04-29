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
 * Updates routine progress and grants a reward when the target is first reached.
 *
 * Unlike [ToggleRoutineUseCase], this use-case delegates log writing to
 * [RoutineRepository.setProgress] and only adds the reward layer on top.
 *
 * Returns a [Result] if the target was just reached and a reward was granted; null otherwise.
 */
class UpdateRoutineProgressUseCase @Inject constructor(
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
        value: Float,
        wasCompleted: Boolean,
        allTodayRoutineIds: Collection<String> = emptyList(),
        completedRoutineIds: Set<String> = emptySet(),
        date: LocalDate = dateTimeProvider.today(),
    ): Result? {
        // Writes partial / completed log — never returns null
        routineRepository.setProgress(routine, value, date)

        val target = routine.targetValue?.toFloat()?.takeIf { it > 0f } ?: 1f
        // Only grant reward when transitioning to completed for the first time
        if (wasCompleted || value < target) return null

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

        val allRoutinesBonus = rewardGrantService.grantAllRoutinesBonusIfEligible(
            routineIds = allTodayRoutineIds,
            completedIds = completedRoutineIds,
            justToggledId = routine.id,
            currentXp = prefs.totalXp + routineReward.xpGranted,
        )

        return Result(routineReward = routineReward, allRoutinesBonus = allRoutinesBonus)
    }
}
