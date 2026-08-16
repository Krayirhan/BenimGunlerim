package com.benimgunlerim.domain.model

sealed class GameEvent {
    data class RewardEarned(val xp: Int, val gold: Int) : GameEvent()
    data class LevelUp(val level: Int, val title: String, val xpBonus: Int = 100) : GameEvent()
    data class AchievementUnlocked(
        val id: String = "",
        val emoji: String,
        val title: String,
        val description: String = "",
        val xpReward: Int = 50,
    ) : GameEvent()
    data class AllTasksCompleted(val totalCount: Int, val xpBonus: Int = 25) : GameEvent()
    data class AllRoutinesCompleted(val streak: Int, val xpBonus: Int = 30) : GameEvent()
    data class MiniBanner(val message: String, val icon: String = "✨") : GameEvent()
}
