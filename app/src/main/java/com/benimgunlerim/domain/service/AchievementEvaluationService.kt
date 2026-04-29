package com.benimgunlerim.domain.service

/**
 * Pure, stateless evaluation service that maps domain event values to the
 * set of achievement IDs that *should be checked* for a potential unlock.
 *
 * This class has no persistence dependency — it only computes which
 * achievement IDs are relevant for a given condition.  The caller
 * (typically [com.benimgunlerim.domain.AchievementTracker]) is responsible
 * for reading the database and calling `tryUnlock` on the returned IDs.
 *
 * Having this as a separate, injectable object allows achievement evaluation
 * rules to be unit-tested without a database dependency.
 */
object AchievementEvaluationService {

    /** Achievement IDs triggered by streak milestones. */
    fun achievementsForStreak(streak: Int): List<String> = buildList {
        if (streak >= 3) add("streak_3")
        if (streak >= 7) add("streak_7")
        if (streak >= 14) add("streak_14")
        if (streak >= 30) add("streak_30")
    }

    /** Achievement IDs triggered by cumulative completed-task count. */
    fun achievementsForTasks(completedCount: Int): List<String> = buildList {
        if (completedCount >= 10) add("tasks_10")
        if (completedCount >= 50) add("tasks_50")
        if (completedCount >= 100) add("tasks_100")
        if (completedCount >= 500) add("tasks_500")
    }

    /** Achievement IDs triggered by cumulative completed-routine count. */
    fun achievementsForRoutines(completedCount: Int): List<String> = buildList {
        if (completedCount >= 10) add("routines_10")
        if (completedCount >= 50) add("routines_50")
        if (completedCount >= 100) add("routines_100")
    }

    /** Achievement IDs triggered when the player reaches a level milestone. */
    fun achievementsForLevel(level: Int): List<String> = buildList {
        if (level >= 5) add("level_5")
        if (level >= 10) add("level_10")
        if (level >= 20) add("level_20")
    }

    /** Achievement IDs triggered by cumulative gold accumulated. */
    fun achievementsForGold(gold: Int): List<String> = buildList {
        if (gold >= 100) add("gold_100")
        if (gold >= 500) add("gold_500")
        if (gold >= 1000) add("gold_1000")
    }

    /** Achievement IDs triggered by the total number of days closed. */
    fun achievementsForDayClose(closeCount: Int): List<String> = buildList {
        if (closeCount >= 1) add("close_1")
        if (closeCount >= 10) add("close_10")
        if (closeCount >= 30) add("close_30")
    }

    /** Achievement IDs triggered by companion happiness level. */
    fun achievementsForHappiness(happiness: Int): List<String> = buildList {
        if (happiness >= 90) add("companion_happy")
    }
}
