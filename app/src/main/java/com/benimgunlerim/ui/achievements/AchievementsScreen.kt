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
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.ui.components.layout.ScreenScaffold
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

    ScreenScaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            SectionBlock(title = "Başarım Özeti") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    StatPill(
                        emoji = "🏆",
                        value = "${state.unlockedCount} / ${state.totalCount}",
                        label = "Kazanıldı",
                    )
                }
            }

            SectionBlock(title = "Tüm Başarımlar") {
                state.achievements.forEach { item ->
                    AchievementRow(
                        title = item.def.title,
                        description = item.def.description,
                        icon = item.def.emoji,
                        isUnlocked = item.isUnlocked,
                        progress = if (item.isUnlocked) 1f else 0f,
                        progressText = if (item.isUnlocked) "Tamamlandı" else "Kilitli",
                    )
                }
            }
        }
    }
}
