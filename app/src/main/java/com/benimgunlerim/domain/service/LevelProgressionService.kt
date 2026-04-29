package com.benimgunlerim.domain.service

import com.benimgunlerim.domain.GameEngine
import javax.inject.Inject

/**
 * Injectable service that wraps [GameEngine]'s level-progression logic.
 *
 * Prefer injecting this over calling [GameEngine.calculateLevel] directly so
 * that classes using level-progression can be tested with a mock/fake
 * without depending on the static [GameEngine] object.
 */
class LevelProgressionService @Inject constructor() {

    /** Returns the current [GameEngine.LevelInfo] for the given total XP. */
    fun calculateLevel(totalXp: Int): GameEngine.LevelInfo =
        GameEngine.calculateLevel(totalXp)

    /**
     * Returns the new [GameEngine.LevelInfo] if adding [gained] XP to
     * [previousXp] results in a level-up; otherwise returns `null`.
     *
     * Example usage in a use-case:
     * ```
     * val levelUp = levelProgressionService.checkLevelUp(prefs.totalXp, xpGained)
     * levelUp?.let { _gameEvents.emit(GameEvent.LevelUp(it.level, it.title)) }
     * ```
     */
    fun checkLevelUp(previousXp: Int, gained: Int): GameEngine.LevelInfo? {
        val before = GameEngine.calculateLevel(previousXp)
        val after = GameEngine.calculateLevel(previousXp + gained)
        return if (after.level > before.level) after else null
    }
}
