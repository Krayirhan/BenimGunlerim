package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.currentStreak
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.ALL_ACHIEVEMENTS
import com.benimgunlerim.domain.model.CompletionStatus
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ProgressSnapshot(
    val last30Days: List<DailyStateEntity>,
    val currentStreak: Int,
    val bestStreak: Int,
    val averageScore: Int,
    val weeklyScore: Int,
    val moodTrend: List<Pair<String, String?>>,
    val energyTrend: List<Pair<String, Int?>>,
    val routineHitRate: Float,
    val taskHitRate: Float,
    val gameState: UserPreferences,
    val unlockedAchievements: List<AchievementDef>,
    val totalAchievements: Int = ALL_ACHIEVEMENTS.size,
)

class ObserveProgressSnapshotUseCase @Inject constructor(
    private val dailyStateRepository: DailyStateRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
) {
    operator fun invoke(): Flow<ProgressSnapshot> = combine(
        dailyStateRepository.observeRecent(limit = 30),
        completionLogRepository.observeAll(),
        prefsRepository.preferences,
        achievementTracker.unlockedAchievements,
    ) { last30Days, allLogs, prefs, unlocked ->
        val weekDays = last30Days.take(7)
        ProgressSnapshot(
            last30Days = last30Days,
            currentStreak = allLogs.currentStreak(),
            bestStreak = last30Days.bestStreak(),
            averageScore = if (last30Days.isEmpty()) 0 else last30Days.map { it.dailyScore }.average().toInt(),
            weeklyScore = if (weekDays.isEmpty()) 0 else weekDays.map { it.dailyScore }.average().toInt(),
            moodTrend = last30Days.take(14).map { it.date to it.mood },
            energyTrend = last30Days.take(14).map { it.date to it.energyLevel },
            routineHitRate = allLogs.hitRate("routine"),
            taskHitRate = allLogs.hitRate("task"),
            gameState = prefs,
            unlockedAchievements = unlocked,
        )
    }
}

private fun List<DailyStateEntity>.bestStreak(): Int {
    if (isEmpty()) return 0
    val sorted = sortedBy { it.date }
    var best = 0
    var current = 0
    var prevDate: LocalDate? = null
    for (state in sorted) {
        val date = runCatching { LocalDate.parse(state.date) }.getOrNull() ?: continue
        current = if (prevDate != null && date == prevDate!!.plusDays(1)) current + 1 else 1
        if (current > best) best = current
        prevDate = date
    }
    return best
}

private fun List<CompletionLogEntity>.hitRate(entityType: String): Float {
    val filtered = filter { it.entityType == entityType }
    if (filtered.isEmpty()) return 0f
    val completed = filtered.count { it.status == CompletionStatus.COMPLETED.value }
    return completed.toFloat() / filtered.size
}
