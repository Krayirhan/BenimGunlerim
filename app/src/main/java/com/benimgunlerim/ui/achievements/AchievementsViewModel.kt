package com.benimgunlerim.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.ALL_ACHIEVEMENTS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AchievementWithStatus(
    val def: AchievementDef,
    val isUnlocked: Boolean,
)

data class AchievementsUiState(
    val achievements: List<AchievementWithStatus> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = ALL_ACHIEVEMENTS.size,
    val isLoading: Boolean = true,
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementTracker: AchievementTracker,
) : ViewModel() {

    val uiState: StateFlow<AchievementsUiState> =
        achievementTracker.allProgress
            .map { progress ->
                val achievements = ALL_ACHIEVEMENTS.map { def ->
                    AchievementWithStatus(
                        def = def,
                        isUnlocked = progress[def.id] == true,
                    )
                }
                AchievementsUiState(
                    achievements = achievements,
                    unlockedCount = achievements.count { it.isUnlocked },
                    totalCount = achievements.size,
                    isLoading = false,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AchievementsUiState(),
            )
}
