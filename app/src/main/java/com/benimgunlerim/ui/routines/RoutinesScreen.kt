@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.EmptyState
import com.benimgunlerim.ui.components.molecules.ProgressBar
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.AddRoutineSheet
import com.benimgunlerim.ui.components.organisms.RoutineRow
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel(),
    onOpenRoutineDetail: (String) -> Unit = {},
) {
    val routines by viewModel.routines.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    val completedTodayCount = remember(routines) {
        routines.count { it.last7Days.lastOrNull() == true }
    }
    val totalRoutinesCount = routines.size
    val progress = if (totalRoutinesCount > 0) completedTodayCount.toFloat() / totalRoutinesCount else 0f

    val everyDayLabel = stringResource(R.string.routines_every_day)
    val daysCountLabel = stringResource(R.string.routines_days_count)

    ScreenScaffold(
        modifier = Modifier.testTag(TestTags.RoutinesRoot),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag(TestTags.RoutinesFab),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.routines_add_cd),
                )
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            if (routines.isEmpty()) {
                EmptyState(
                    emoji = "🔥",
                    title = stringResource(R.string.routines_empty_title),
                    description = stringResource(R.string.routines_empty_body),
                    action = {
                        AppButton(
                            text = stringResource(R.string.routines_empty_cta),
                            onClick = { showAddSheet = true },
                        )
                    },
                )
            } else {
                // Alışkanlık Karnesi (Summary Card)
                AppSurface(
                    radius = AppTokens.Radius.md,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.routines_summary_streak_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(
                                    R.string.routines_summary_today_done,
                                    completedTodayCount,
                                    totalRoutinesCount,
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        ProgressBar(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Aktif Rutinler Listesi (Single Surface)
                SectionBlock(title = stringResource(R.string.routines_section_title)) {
                    AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
                        Column {
                            routines.forEachIndexed { index, routine ->
                                key(routine.id) {
                                    if (index > 0) AppDivider()

                                    val isCompletedToday = routine.last7Days.lastOrNull() == true
                                    val targetSummary = if (routine.targetDays.size == 7) {
                                        everyDayLabel
                                    } else {
                                        String.format(daysCountLabel, routine.targetDays.size)
                                    }

                                    RoutineRow(
                                        title = routine.name,
                                        isCompletedToday = isCompletedToday,
                                        onToggle = { viewModel.toggleRoutine(routine.id) },
                                        weekHistory = routine.last7Days,
                                        targetDays = routine.targetDays,
                                        streakCount = routine.currentStreak,
                                        preferredTime = routine.preferredTime,
                                        targetDaysSummary = targetSummary,
                                        targetType = routine.targetType,
                                        targetValue = routine.targetValue,
                                        targetUnit = routine.targetUnit,
                                        currentValue = routine.currentValue,
                                        onProgressChange = { nextValue ->
                                            viewModel.updateRoutineProgress(routine.id, nextValue)
                                        },
                                        useSurface = false,
                                        onClick = { onOpenRoutineDetail(routine.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddRoutineSheet(
            onDismiss = { showAddSheet = false },
            onSave = { title, targetDays, reminderTime, category, targetCount, unit ->
                viewModel.addRoutine(title, targetDays, reminderTime)
            },
        )
    }
}
