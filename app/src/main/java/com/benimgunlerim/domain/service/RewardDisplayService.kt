package com.benimgunlerim.domain.service

import android.util.Log
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.model.GameEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates reward and achievement display events.
 * Extracted from TodayViewModel to separate UI display logic from state management.
 *
 * Responsibilities:
 * - Emit GameEvent (RewardEarned, LevelUp) based on GrantResult
 * - Coordinate with FeedbackManager for haptic/audio feedback
 * - Handle achievement unlock display
 * - Deduplicate already-granted rewards
 */
@Singleton
class RewardDisplayService @Inject constructor(
    private val feedbackManager: FeedbackManager,
) {
    private val _gameEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 5)
    val gameEvents: Flow<GameEvent> = _gameEvents.asSharedFlow()

    companion object {
        private const val REWARD_SOUND_VOLUME = 0.7f
        private const val TAG = "RewardDisplayService"
    }

    /**
     * Process task completion grant result and emit appropriate display events.
     * Handles task completion reward, all-tasks bonus, and level-up indicators.
     */
    suspend fun onTaskCompleted(
        taskId: String,
        taskReward: GrantResult,
        allTasksBonus: GrantResult? = null,
    ) {
        // Emit main task completion reward
        processGrantResult(
            grantResult = taskReward,
            eventLabel = "task_completion",
            logContextId = taskId,
        )

        // Emit all-tasks bonus if applicable
        allTasksBonus?.let {
            processGrantResult(
                grantResult = it,
                eventLabel = "all_tasks_bonus",
                logContextId = taskId,
            )
        }
    }

    /**
     * Process routine completion grant result and emit appropriate display events.
     */
    suspend fun onRoutineCompleted(
        routineId: String,
        grantResult: GrantResult,
    ) {
        processGrantResult(
            grantResult = grantResult,
            eventLabel = "routine_completion",
            logContextId = routineId,
        )
    }

    /**
     * Process achievement unlock and emit display event.
     */
    suspend fun onAchievementUnlocked(
        emoji: String,
        title: String,
    ) {
        feedbackManager.celebrationBurst()
        _gameEvents.tryEmit(GameEvent.AchievementUnlocked(emoji, title))
    }

    /**
     * Process daily bonus (e.g., perfect day, all routines completed).
     */
    suspend fun onDailyBonusEarned(
        xp: Int,
        gold: Int,
    ) {
        feedbackManager.playSound("reward", REWARD_SOUND_VOLUME)
        _gameEvents.tryEmit(GameEvent.RewardEarned(xp, gold))
    }

    private suspend fun processGrantResult(
        grantResult: GrantResult,
        eventLabel: String,
        logContextId: String,
    ) {
        when (grantResult) {
            is GrantResult.Granted -> {
                // Haptic feedback for reward
                feedbackManager.tapMedium()

                // Emit reward earned event
                _gameEvents.tryEmit(GameEvent.RewardEarned(grantResult.xpGranted, grantResult.goldGranted))

                // Handle level up if applicable
                grantResult.leveledUp?.let { level ->
                    feedbackManager.levelUpVibration()
                    _gameEvents.tryEmit(
                        GameEvent.LevelUp(
                            level = level.level,
                            title = level.title,
                        ),
                    )
                }

                Log.d(TAG, "Reward displayed: $eventLabel (xp=${grantResult.xpGranted}, " +
                    "gold=${grantResult.goldGranted}, levelUp=${grantResult.leveledUp?.level})")
            }

            is GrantResult.AlreadyGranted -> {
                Log.d(TAG, "Reward already granted for: $eventLabel (context=$logContextId)")
                // No display event for duplicate rewards
            }
        }
    }
}
