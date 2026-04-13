package com.benimgunlerim.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.currentStreak
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.ALL_ACHIEVEMENTS
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProgressUiState(
    val last30Days: List<DailyStateEntity> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val averageScore: Int = 0,
    val weeklyScore: Int = 0,
    val moodTrend: List<Pair<String, String?>> = emptyList(),
    val energyTrend: List<Pair<String, Int?>> = emptyList(),
    val routineHitRate: Float = 0f,
    val taskHitRate: Float = 0f,
    val gameState: UserPreferences = UserPreferences(),
    val unlockedAchievements: List<AchievementDef> = emptyList(),
    val totalAchievements: Int = ALL_ACHIEVEMENTS.size,
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    repository: BenimGunlerimRepository,
    prefsRepository: UserPreferencesRepository,
    achievementTracker: AchievementTracker,
) : ViewModel() {
    val uiState: StateFlow<ProgressUiState> = combine(
        repository.observeRecentDailyStates(limit = 30),
        repository.observeAllCompletionLogs(),
        prefsRepository.preferences,
        achievementTracker.unlockedAchievements,
    ) { last30Days, allLogs, prefs, unlocked ->
        val weekDays = last30Days.take(7)
        ProgressUiState(
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())
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
    val completed = filtered.count { it.status == "completed" }
    return completed.toFloat() / filtered.size
}

