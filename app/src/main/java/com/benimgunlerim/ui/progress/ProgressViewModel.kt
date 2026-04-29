package com.benimgunlerim.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.ALL_ACHIEVEMENTS
import com.benimgunlerim.domain.usecase.ObserveProgressSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProgressUiState(
    val last30Days: List<ProgressDayUi> = emptyList(),
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
    observeProgressSnapshot: ObserveProgressSnapshotUseCase,
) : ViewModel() {
    val uiState: StateFlow<ProgressUiState> = observeProgressSnapshot()
        .map { snapshot ->
        ProgressUiState(
            last30Days = snapshot.last30Days.map {
                ProgressDayUi(
                    date = it.date,
                    dailyScore = it.dailyScore,
                    completionRate = it.completionRate,
                    note = it.note,
                )
            },
            currentStreak = snapshot.currentStreak,
            bestStreak = snapshot.bestStreak,
            averageScore = snapshot.averageScore,
            weeklyScore = snapshot.weeklyScore,
            moodTrend = snapshot.moodTrend,
            energyTrend = snapshot.energyTrend,
            routineHitRate = snapshot.routineHitRate,
            taskHitRate = snapshot.taskHitRate,
            gameState = snapshot.gameState,
            unlockedAchievements = snapshot.unlockedAchievements,
            totalAchievements = snapshot.totalAchievements,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())
}

