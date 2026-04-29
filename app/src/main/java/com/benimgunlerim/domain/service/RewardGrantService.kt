package com.benimgunlerim.domain.service

import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.GameEngine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result returned after attempting to grant a one-time reward for a domain event.
 *
 * Modeled as a sealed result so callers can distinguish a real grant from a
 * no-op, while still using the convenient aggregate properties below.
 */
sealed class GrantResult {
    open val xpGranted: Int = 0
    open val goldGranted: Int = 0
    open val leveledUp: GameEngine.LevelInfo? = null
    val alreadyGranted: Boolean get() = this is AlreadyGranted

    data class Granted(
        override val xpGranted: Int,
        override val goldGranted: Int = 0,
        override val leveledUp: GameEngine.LevelInfo? = null,
    ) : GrantResult()

    object AlreadyGranted : GrantResult()
}

/**
 * Centralised service for granting one-time rewards and detecting level-ups.
 *
 * By concentrating these calls here, the ViewModel is freed from reward logic and each
 * domain scenario (task toggle, routine toggle, day close, etc.) gains a tested path.
 */
@Singleton
class RewardGrantService @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
    private val dateTimeProvider: DateTimeProvider,
) {
    /**
     * Grants a one-time reward for [eventKey] and returns a [GrantResult].
     *
     * If the event was already rewarded [GrantResult.alreadyGranted] is true and
     * [GrantResult.xpGranted] / [GrantResult.goldGranted] are 0.
     *
     * @param currentXp Caller's snapshot of total XP **before** this grant (used for level-up detection).
     */
    suspend fun grantOnce(
        eventKey: String,
        xp: Int,
        gold: Int = 0,
        happinessDelta: Int = 0,
        currentXp: Int,
    ): GrantResult {
        val granted = prefsRepository.grantRewardOnce(
            eventKey = eventKey,
            xp = xp,
            gold = gold,
            happinessDelta = happinessDelta,
        )
        if (!granted) return GrantResult.AlreadyGranted

        val oldLevel = GameEngine.calculateLevel(currentXp)
        val newLevel = GameEngine.calculateLevel(currentXp + xp)
        return GrantResult.Granted(
            xpGranted = xp,
            goldGranted = gold,
            leveledUp = if (newLevel.level > oldLevel.level) newLevel else null,
        )
    }

    /**
     * Attempts to unlock the "all tasks completed" daily bonus.
     * Caller must supply the current day's [taskIds] and the id of the task just toggled so the
     * check is optimistic (the DB write may not yet be reflected in the list).
     */
    suspend fun grantAllTasksBonusIfEligible(
        taskIds: Collection<String>,
        completedIds: Collection<String>,
        justToggledId: String,
        currentXp: Int,
    ): GrantResult {
        val notYetCompleted = taskIds.filter { id ->
            id != justToggledId && id !in completedIds
        }
        if (taskIds.isEmpty() || notYetCompleted.isNotEmpty()) return GrantResult.AlreadyGranted
        val today = dateTimeProvider.today()
        return grantOnce(
            eventKey = "allTasks:$today",
            xp = GameEngine.XP_ALL_TASKS_BONUS,
            currentXp = currentXp,
        )
    }

    /**
     * Attempts to unlock the "all routines completed" daily bonus.
     */
    suspend fun grantAllRoutinesBonusIfEligible(
        routineIds: Collection<String>,
        completedIds: Collection<String>,
        justToggledId: String,
        currentXp: Int,
    ): GrantResult {
        val notYetCompleted = routineIds.filter { id ->
            id != justToggledId && id !in completedIds
        }
        if (routineIds.isEmpty() || notYetCompleted.isNotEmpty()) return GrantResult.AlreadyGranted
        val today = dateTimeProvider.today()
        return grantOnce(
            eventKey = "allRoutines:$today",
            xp = GameEngine.XP_ALL_ROUTINES_BONUS,
            currentXp = currentXp,
        )
    }
}
