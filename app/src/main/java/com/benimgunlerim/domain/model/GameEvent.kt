package com.benimgunlerim.domain.model

sealed class GameEvent {
    data class RewardEarned(val xp: Int, val gold: Int) : GameEvent()
    data class LevelUp(val level: Int, val title: String) : GameEvent()
    data class AchievementUnlocked(val emoji: String, val title: String) : GameEvent()
}
