package com.benimgunlerim.domain.service

import android.util.Log
import com.benimgunlerim.data.UserPreferencesSource
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.model.GameEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardDisplayService @Inject constructor(
    private val feedbackManager: FeedbackManager,
    private val preferencesSource: UserPreferencesSource,
) {
    private val _gameEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 16)
    val gameEvents: Flow<GameEvent> = _gameEvents.asSharedFlow()

    companion object {
        private const val REWARD_SOUND_VOLUME = 0.7f
        private const val TAG = "RewardDisplayService"
    }

    suspend fun onTaskCompleted(
        taskId: String,
        taskReward: GrantResult,
        allTasksBonus: GrantResult? = null,
    ) {
        processGrantResult(
            grantResult = taskReward,
            eventLabel = "task_completion",
            logContextId = taskId,
        )

        allTasksBonus?.let {
            processGrantResult(
                grantResult = it,
                eventLabel = "all_tasks_bonus",
                logContextId = taskId,
            )
        }
    }

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

    suspend fun onAchievementUnlocked(
        def: AchievementDef,
    ) {
        if (!effectsEnabled()) return
        feedbackManager.celebrationBurst()
        _gameEvents.tryEmit(
            GameEvent.AchievementUnlocked(
                id = def.id,
                emoji = def.emoji,
                title = def.title,
                description = def.description,
                xpReward = def.xpReward,
            ),
        )
    }

    suspend fun onAchievementUnlocked(
        emoji: String,
        title: String,
    ) {
        if (!effectsEnabled()) return
        feedbackManager.celebrationBurst()
        _gameEvents.tryEmit(
            GameEvent.AchievementUnlocked(
                emoji = emoji,
                title = title,
            ),
        )
    }

    suspend fun emitMiniBanner(message: String, icon: String = "✨") {
        if (!effectsEnabled()) return
        feedbackManager.tapLight()
        _gameEvents.tryEmit(GameEvent.MiniBanner(message, icon))
    }

    suspend fun emitAllTasksCompleted(totalCount: Int, xpBonus: Int = 25) {
        if (!effectsEnabled()) return
        feedbackManager.celebrationBurst()
        _gameEvents.tryEmit(GameEvent.AllTasksCompleted(totalCount, xpBonus))
    }

    suspend fun emitAllRoutinesCompleted(streak: Int, xpBonus: Int = 30) {
        if (!effectsEnabled()) return
        feedbackManager.celebrationBurst()
        _gameEvents.tryEmit(GameEvent.AllRoutinesCompleted(streak, xpBonus))
    }

    suspend fun onDailyBonusEarned(
        xp: Int,
        gold: Int,
    ) {
        if (effectsEnabled()) feedbackManager.playSound("reward", REWARD_SOUND_VOLUME)
        _gameEvents.tryEmit(GameEvent.RewardEarned(xp, gold))
    }

    private suspend fun processGrantResult(
        grantResult: GrantResult,
        eventLabel: String,
        logContextId: String,
    ) {
        when (grantResult) {
            is GrantResult.Granted -> {
                val effectsEnabled = effectsEnabled()
                if (effectsEnabled) feedbackManager.tapMedium()
                _gameEvents.tryEmit(GameEvent.RewardEarned(grantResult.xpGranted, grantResult.goldGranted))

                if (effectsEnabled) {
                    grantResult.leveledUp?.let { level ->
                        feedbackManager.levelUpVibration()
                        _gameEvents.tryEmit(
                            GameEvent.LevelUp(
                                level = level.level,
                                title = level.title,
                            ),
                        )
                    }
                }

                Log.d(
                    TAG,
                    "Reward displayed: $eventLabel (xp=${grantResult.xpGranted}, " +
                        "gold=${grantResult.goldGranted}, levelUp=${grantResult.leveledUp?.level})",
                )
            }

            is GrantResult.AlreadyGranted -> {
                Log.d(TAG, "Reward already granted for: $eventLabel (context=$logContextId)")
            }
        }
    }

    private suspend fun effectsEnabled(): Boolean =
        preferencesSource.preferences.first().celebrationEffectsEnabled
}
