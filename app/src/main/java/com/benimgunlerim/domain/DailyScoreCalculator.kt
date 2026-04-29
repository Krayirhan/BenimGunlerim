package com.benimgunlerim.domain

/**
 * Pure domain service for calculating the XP/gold score of a closed day.
 *
 * No persistence, no Android dependencies — deterministic and easy to
 * unit-test.  The [CloseDayUseCase] should delegate scoring to this object
 * so the math can be validated independently.
 */
object DailyScoreCalculator {

    data class DayScore(
        /** Whether the day achieved 100% completion. */
        val isPerfectDay: Boolean,
        /** Total XP to be granted for this day close. */
        val xp: Int,
        /** Total gold to be granted for this day close. */
        val gold: Int,
    )

    /**
     * Calculates the XP/gold reward for closing a day.
     *
     * A "perfect day" requires [completionRate] >= 1.0f.
     * Perfect days receive both the base day-close reward AND the
     * [GameEngine.XP_PERFECT_DAY] / [GameEngine.GOLD_PERFECT_DAY] bonuses.
     */
    fun calculate(completionRate: Float): DayScore {
        val isPerfect = completionRate >= 1f
        return DayScore(
            isPerfectDay = isPerfect,
            xp = GameEngine.XP_DAY_CLOSE + if (isPerfect) GameEngine.XP_PERFECT_DAY else 0,
            gold = if (isPerfect) GameEngine.GOLD_PERFECT_DAY else 0,
        )
    }

    /**
     * Returns `true` when no summary has been saved for a past day,
     * indicating a "missed day" that should be surfaced to the user.
     *
     * @param closedAt   The timestamp when the day was closed, or `null`.
     * @param isInPast   Whether the date in question is before today.
     */
    fun isMissedDay(closedAt: Long?, isInPast: Boolean): Boolean =
        closedAt == null && isInPast
}
