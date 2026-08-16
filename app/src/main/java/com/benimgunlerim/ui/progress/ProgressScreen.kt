@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.BarChart
import com.benimgunlerim.ui.components.molecules.BarChartEntry
import com.benimgunlerim.ui.components.molecules.MetricCard
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.AchievementRow
import com.benimgunlerim.ui.components.organisms.LevelHeroCard
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    onNavigateToAchievements: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val level = (state.gameState.totalXp / 100) + 1
    val maxXp = level * 100
    val today = remember { LocalDate.now() }
    val primaryColor = MaterialTheme.colorScheme.primary

    // Always build 7 days chart entries
    val weekChartEntries = remember(state.last30Days, today, primaryColor) {
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dateStr = date.toString()
            val dayRecord = state.last30Days.firstOrNull { it.date == dateStr }
            val score = dayRecord?.dailyScore?.toFloat() ?: 0f
            val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("tr")).uppercase()

            BarChartEntry(
                label = label,
                value = score,
                color = primaryColor,
                isHighlighted = offset == 0,
            )
        }
    }

    val maxChartScore = remember(weekChartEntries) {
        (weekChartEntries.maxOfOrNull { it.value } ?: 100f).coerceAtLeast(100f)
    }

    val daysUnit = stringResource(R.string.progress_unit_days)
    val completedText = stringResource(R.string.progress_unit_completed)

    ScreenScaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            LevelHeroCard(
                level = level,
                levelTitle = stringResource(R.string.today_level_default_title),
                currentXp = state.gameState.totalXp,
                maxXp = maxXp,
                streak = state.currentStreak,
                gold = state.gameState.gold,
            )

            // 4'lü Metrik Izgarası
            SectionBlock(title = stringResource(R.string.progress_consistency_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                    ) {
                        MetricCard(
                            title = stringResource(R.string.progress_metric_active_streak),
                            value = "${state.currentStreak}",
                            unit = daysUnit,
                            modifier = Modifier.weight(1f),
                        )
                        MetricCard(
                            title = stringResource(R.string.progress_metric_best_streak),
                            value = "${state.bestStreak}",
                            unit = daysUnit,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                    ) {
                        MetricCard(
                            title = stringResource(R.string.progress_balance_routine),
                            value = "${(state.routineHitRate * 100).toInt()}%",
                            unit = "tamamlandı",
                            modifier = Modifier.weight(1f),
                        )
                        MetricCard(
                            title = stringResource(R.string.progress_balance_task),
                            value = "${(state.taskHitRate * 100).toInt()}%",
                            unit = "tamamlandı",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Haftalık Ritim Grafiği
            SectionBlock(title = stringResource(R.string.progress_chart_title)) {
                AppSurface(
                    radius = AppTokens.Radius.md,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BarChart(
                        entries = weekChartEntries,
                        maxValue = maxChartScore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppTokens.Spacing.xs),
                    )
                }
            }

            // Başarımlar
            SectionBlock(
                title = stringResource(R.string.progress_achievements_title),
                trailingContent = {
                    Text(
                        text = stringResource(
                            R.string.progress_achievements_subtitle,
                            state.totalAchievements,
                            state.unlockedAchievements.size,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            ) {
                if (state.unlockedAchievements.isNotEmpty()) {
                    AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
                        Column {
                            state.unlockedAchievements.forEachIndexed { index, achievement ->
                                if (index > 0) AppDivider()
                                AchievementRow(
                                    title = achievement.title,
                                    description = achievement.description,
                                    icon = achievement.emoji,
                                    isUnlocked = true,
                                    progress = 1f,
                                    progressText = completedText,
                                )
                            }
                        }
                    }
                } else {
                    AppSurface(radius = AppTokens.Radius.md) {
                        Text(
                            text = stringResource(R.string.progress_achievements_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
