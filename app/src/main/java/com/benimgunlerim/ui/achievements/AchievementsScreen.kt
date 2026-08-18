package com.benimgunlerim.ui.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.layout.DetailScreenScaffold
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.molecules.StatPill
import com.benimgunlerim.ui.components.organisms.AchievementRow
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun AchievementsScreen(
    onBack: () -> Unit = {},
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    DetailScreenScaffold(
        title = stringResource(R.string.progress_achievements_title),
        onBack = onBack,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = AppTokens.Spacing.screenHorizontal, vertical = AppTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            SectionBlock(title = stringResource(R.string.achievements_summary_title)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    StatPill(
                        emoji = "🏆",
                        value = "${state.unlockedCount} / ${state.totalCount}",
                        label = stringResource(R.string.achievements_summary_unlocked_label),
                    )
                }
            }

            SectionBlock(title = stringResource(R.string.achievements_all_title)) {
                val completedLabel = stringResource(R.string.achievements_status_completed)
                val lockedLabel = stringResource(R.string.achievements_status_locked)
                state.achievements.forEach { item ->
                    AchievementRow(
                        title = item.def.title,
                        description = item.def.description,
                        icon = item.def.emoji,
                        isUnlocked = item.isUnlocked,
                        progress = if (item.isUnlocked) 1f else 0f,
                        progressText = if (item.isUnlocked) completedLabel else lockedLabel,
                    )
                }
            }
        }
    }
}
