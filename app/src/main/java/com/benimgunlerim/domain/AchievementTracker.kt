package com.benimgunlerim.domain

import androidx.annotation.StringRes
import com.benimgunlerim.R
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

data class AchievementDef(
    val id: String,
    val emoji: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val xpReward: Int = 50,
    val goldReward: Int = 20,
)

val ALL_ACHIEVEMENTS = listOf(
    // First step milestones
    AchievementDef("first_task", "🌱", R.string.achievement_first_task_title, R.string.achievement_first_task_desc, 25, 10),
    AchievementDef("first_routine", "🔄", R.string.achievement_first_routine_title, R.string.achievement_first_routine_desc, 25, 10),
    AchievementDef("first_plan", "📝", R.string.achievement_first_plan_title, R.string.achievement_first_plan_desc, 20, 10),
    // Streak milestones
    AchievementDef("streak_3", "🔥", R.string.achievement_streak_3_title, R.string.achievement_streak_3_desc, 30, 15),
    AchievementDef("streak_7", "⚡", R.string.achievement_streak_7_title, R.string.achievement_streak_7_desc, 50, 30),
    AchievementDef("streak_14", "💪", R.string.achievement_streak_14_title, R.string.achievement_streak_14_desc, 100, 50),
    AchievementDef("streak_30", "⭐", R.string.achievement_streak_30_title, R.string.achievement_streak_30_desc, 200, 100),
    // Task milestones
    AchievementDef("tasks_10", "📋", R.string.achievement_tasks_10_title, R.string.achievement_tasks_10_desc, 30, 15),
    AchievementDef("tasks_25", "✨", R.string.achievement_tasks_25_title, R.string.achievement_tasks_25_desc, 50, 25),
    AchievementDef("tasks_50", "📝", R.string.achievement_tasks_50_title, R.string.achievement_tasks_50_desc, 80, 40),
    AchievementDef("tasks_100", "🏆", R.string.achievement_tasks_100_title, R.string.achievement_tasks_100_desc, 150, 75),
    AchievementDef("tasks_500", "👑", R.string.achievement_tasks_500_title, R.string.achievement_tasks_500_desc, 300, 150),
    // Routine milestones
    AchievementDef("routines_10", "🔄", R.string.achievement_routines_10_title, R.string.achievement_routines_10_desc, 30, 15),
    AchievementDef("routines_50", "💎", R.string.achievement_routines_50_title, R.string.achievement_routines_50_desc, 80, 40),
    AchievementDef("routines_100", "🌟", R.string.achievement_routines_100_title, R.string.achievement_routines_100_desc, 150, 75),
    // Perfect day & List clear
    AchievementDef("perfect_1", "✨", R.string.achievement_perfect_1_title, R.string.achievement_perfect_1_desc, 50, 25),
    AchievementDef("list_cleared", "🧹", R.string.achievement_list_cleared_title, R.string.achievement_list_cleared_desc, 40, 20),
    AchievementDef("perfect_5", "🌈", R.string.achievement_perfect_5_title, R.string.achievement_perfect_5_desc, 100, 50),
    AchievementDef("perfect_20", "🎯", R.string.achievement_perfect_20_title, R.string.achievement_perfect_20_desc, 200, 100),
    // Calmness & Mental Health
    AchievementDef("calm_deep_breath", "🫁", R.string.achievement_calm_deep_breath_title, R.string.achievement_calm_deep_breath_desc, 25, 10),
    AchievementDef("calm_short_pause", "🧘", R.string.achievement_calm_short_pause_title, R.string.achievement_calm_short_pause_desc, 40, 20),
    AchievementDef("calm_light_day", "🌿", R.string.achievement_calm_light_day_title, R.string.achievement_calm_light_day_desc, 30, 15),
    AchievementDef("calm_brain_dump", "💡", R.string.achievement_calm_brain_dump_title, R.string.achievement_calm_brain_dump_desc, 25, 10),
    AchievementDef("calm_gentle_close", "🌙", R.string.achievement_calm_gentle_close_title, R.string.achievement_calm_gentle_close_desc, 35, 15),
    // Level milestones
    AchievementDef("level_5", "⚡", R.string.achievement_level_5_title, R.string.achievement_level_5_desc, 50, 25),
    AchievementDef("level_10", "🚀", R.string.achievement_level_10_title, R.string.achievement_level_10_desc, 100, 50),
    AchievementDef("level_20", "🌙", R.string.achievement_level_20_title, R.string.achievement_level_20_desc, 200, 100),
    // Gold milestones
    AchievementDef("gold_100", "🪙", R.string.achievement_gold_100_title, R.string.achievement_gold_100_desc, 30, 0),
    AchievementDef("gold_500", "💰", R.string.achievement_gold_500_title, R.string.achievement_gold_500_desc, 80, 0),
    AchievementDef("gold_1000", "🏦", R.string.achievement_gold_1000_title, R.string.achievement_gold_1000_desc, 150, 0),
    // Day close
    AchievementDef("close_1", "🌙", R.string.achievement_close_1_title, R.string.achievement_close_1_desc, 25, 10),
    AchievementDef("close_10", "📖", R.string.achievement_close_10_title, R.string.achievement_close_10_desc, 60, 30),
    AchievementDef("close_30", "📚", R.string.achievement_close_30_title, R.string.achievement_close_30_desc, 150, 75),
    // Companion
    AchievementDef("companion_happy", "❤️", R.string.achievement_companion_happy_title, R.string.achievement_companion_happy_desc, 50, 25),
    // Early riser
    AchievementDef("early_task", "🌅", R.string.achievement_early_task_title, R.string.achievement_early_task_desc, 40, 20),
    // Shop
    AchievementDef("first_buy", "🛍️", R.string.achievement_first_buy_title, R.string.achievement_first_buy_desc, 30, 0),
    // Mood
    AchievementDef("mood_5_happy", "😄", R.string.achievement_mood_5_happy_title, R.string.achievement_mood_5_happy_desc, 50, 25),
)

private val achievementMap = ALL_ACHIEVEMENTS.associateBy { it.id }

@Singleton
class AchievementTracker @Inject constructor(
    private val achievementDao: AchievementDao,
    private val dateTimeProvider: DateTimeProvider,
) {
    private val _newUnlock = MutableSharedFlow<AchievementDef>(extraBufferCapacity = 5)
    val newUnlock = _newUnlock.asSharedFlow()

    val unlockedAchievements: Flow<List<AchievementDef>> =
        achievementDao.observeUnlocked().map { entities ->
            entities.mapNotNull { achievementMap[it.id] }
        }

    val allProgress: Flow<Map<String, Boolean>> =
        achievementDao.observeAll().map { entities ->
            ALL_ACHIEVEMENTS.associate { def ->
                def.id to (entities.any { it.id == def.id && it.unlockedAt != null })
            }
        }

    suspend fun tryUnlock(id: String): AchievementDef? {
        val def = achievementMap[id] ?: return null
        // Ensure row exists
        achievementDao.insert(AchievementEntity(id = id))
        val updated = achievementDao.unlock(id, dateTimeProvider.currentTimeMillis())
        return if (updated > 0) {
            _newUnlock.tryEmit(def)
            def
        } else null
    }

    suspend fun checkStreak(streak: Int) {
        if (streak >= 3) tryUnlock("streak_3")
        if (streak >= 7) tryUnlock("streak_7")
        if (streak >= 14) tryUnlock("streak_14")
        if (streak >= 30) tryUnlock("streak_30")
    }

    suspend fun checkFirstTask() {
        tryUnlock("first_task")
    }

    suspend fun checkFirstRoutine() {
        tryUnlock("first_routine")
    }

    suspend fun checkFirstPlan() {
        tryUnlock("first_plan")
    }

    suspend fun checkListCleared() {
        tryUnlock("list_cleared")
    }

    suspend fun checkCalmDeepBreath() {
        tryUnlock("calm_deep_breath")
    }

    suspend fun checkCalmShortPause(count: Int) {
        if (count >= 3) tryUnlock("calm_short_pause")
    }

    suspend fun checkCalmLightDay() {
        tryUnlock("calm_light_day")
    }

    suspend fun checkCalmBrainDump() {
        tryUnlock("calm_brain_dump")
    }

    suspend fun checkCalmGentleClose() {
        tryUnlock("calm_gentle_close")
    }

    suspend fun checkTaskCount(count: Int) {
        if (count >= 1) tryUnlock("first_task")
        if (count >= 10) tryUnlock("tasks_10")
        if (count >= 25) tryUnlock("tasks_25")
        if (count >= 50) tryUnlock("tasks_50")
        if (count >= 100) tryUnlock("tasks_100")
        if (count >= 500) tryUnlock("tasks_500")
    }

    suspend fun checkRoutineCount(count: Int) {
        if (count >= 1) tryUnlock("first_routine")
        if (count >= 10) tryUnlock("routines_10")
        if (count >= 50) tryUnlock("routines_50")
        if (count >= 100) tryUnlock("routines_100")
    }

    suspend fun checkPerfectDay(count: Int) {
        if (count >= 1) tryUnlock("perfect_1")
        if (count >= 5) tryUnlock("perfect_5")
        if (count >= 20) tryUnlock("perfect_20")
    }

    suspend fun checkLevel(level: Int) {
        if (level >= 5) tryUnlock("level_5")
        if (level >= 10) tryUnlock("level_10")
        if (level >= 20) tryUnlock("level_20")
    }

    suspend fun checkGold(gold: Int) {
        if (gold >= 100) tryUnlock("gold_100")
        if (gold >= 500) tryUnlock("gold_500")
        if (gold >= 1000) tryUnlock("gold_1000")
    }

    suspend fun checkDayClose(count: Int) {
        if (count >= 1) tryUnlock("close_1")
        if (count >= 10) tryUnlock("close_10")
        if (count >= 30) tryUnlock("close_30")
    }

    suspend fun checkHappiness(happiness: Int) {
        if (happiness >= 90) tryUnlock("companion_happy")
    }
}
