package com.benimgunlerim.domain.model

import androidx.annotation.StringRes

sealed class GameEvent {
    data class RewardEarned(val xp: Int, val gold: Int) : GameEvent()
    data class LevelUp(val level: Int, @StringRes val titleRes: Int, val xpBonus: Int = 100) : GameEvent()
    data class AchievementUnlocked(
        val id: String = "",
        val emoji: String,
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
        val xpReward: Int = 50,
    ) : GameEvent()
    data class AllTasksCompleted(val totalCount: Int, val xpBonus: Int = 25) : GameEvent()
    data class AllRoutinesCompleted(val streak: Int, val xpBonus: Int = 30) : GameEvent()
    data class MiniBanner(@StringRes val messageRes: Int, val icon: String = "✨") : GameEvent()
}
