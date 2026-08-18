package com.benimgunlerim.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.molecules.AlertBanner
import com.benimgunlerim.ui.components.molecules.AlertBannerSeverity
import com.benimgunlerim.ui.components.molecules.EmptyState
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.CloseDayCard
import com.benimgunlerim.ui.components.organisms.HeaderProgressCard
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class TodayListSummaryState(
    val dayIsClosed: Boolean,
    val isEmptyToday: Boolean,
    val totalCount: Int,
    val completedCount: Int,
    val userLevel: Int,
    val miniBannerData: Pair<String, String>?,
    val celebrationEffectsEnabled: Boolean,
    val dismissContextualReset: Boolean,
)

internal data class TodayListEventCallbacks(
    val onNavigateToRoutines: () -> Unit,
    val onResetClick: () -> Unit,
    val onDismissContextualReset: () -> Unit,
    val onMissedDayReviewClick: () -> Unit,
    val onAddTaskClick: () -> Unit,
    val onOpenRoutineMenu: (String) -> Unit,
    val onCloseDayClick: () -> Unit,
    val onRequestDeleteTask: (String) -> Unit,
)

@Suppress("LongMethod")
@Composable
internal fun TodayContentList(
    modifier: Modifier,
    contentPadding: PaddingValues,
    state: TodayUiState,
    today: LocalDate,
    viewModel: TodayViewModel,
    summaryState: TodayListSummaryState,
    callbacks: TodayListEventCallbacks,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
    ) {
        item(key = "mini_celebration") {
            if (summaryState.celebrationEffectsEnabled) summaryState.miniBannerData?.let { (msg, icon) ->
                com.benimgunlerim.ui.components.gamification.MiniCelebrationBanner(
                    message = msg,
                    icon = icon,
                    visible = true,
                )
            }
        }
        if (state.missedDay != null) {
            item(key = "missed_day") {
                val missed = state.missedDay!!
                val dayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale("tr")) }
                AlertBanner(
                    message = stringResource(R.string.today_missed_day_msg, missed.format(dayFormatter)),
                    severity = AlertBannerSeverity.Warning,
                    title = stringResource(R.string.today_missed_day_title),
                    actionLabel = stringResource(R.string.today_missed_day_action),
                    action = callbacks.onMissedDayReviewClick,
                    modifier = Modifier.testTag(TestTags.TodayMissedDayBanner),
                )
            }
        }

        if (state.isLightDayMode) {
            item(key = "light_day_banner") {
                LightDayBanner(onDisableClick = { viewModel.toggleLightDayMode(false) })
            }
        } else if (!summaryState.dismissContextualReset && summaryState.totalCount >= 4 && summaryState.completedCount == 0) {
            item(key = "contextual_reset") {
                ContextualResetCard(
                    isLightDayMode = state.isLightDayMode,
                    onResetClick = callbacks.onResetClick,
                    onDismissClick = callbacks.onDismissContextualReset,
                )
            }
        }

        item(key = "header_progress_card") {
            HeaderProgressCard(
                streakCount = state.currentStreak,
                completedTasks = summaryState.completedCount,
                totalTasks = summaryState.totalCount,
                level = summaryState.userLevel,
                xp = state.gameState.totalXp,
                isLightDayMode = state.isLightDayMode,
            )
        }

        if (state.overdueTasks.isNotEmpty()) {
            item(key = "overdue_tasks_section") {
                SectionBlock(
                    title = stringResource(R.string.today_overdue_tasks_title),
                    trailingContent = {
                        TextButton(
                            onClick = { viewModel.moveAllOverdueTo(today) },
                            modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min),
                        ) {
                            Text(stringResource(R.string.today_move_all_overdue_to_today))
                        }
                    },
                ) {
                    TaskListContainer(
                        tasks = state.overdueTasks,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onDeleteTask = callbacks.onRequestDeleteTask,
                    )
                }
            }
        }

        if (summaryState.isEmptyToday) {
            item(key = "empty_today") {
                EmptyState(
                    emoji = "✨",
                    title = stringResource(R.string.today_empty_title),
                    description = stringResource(R.string.today_empty_desc),
                    action = {
                        AppButton(
                            text = stringResource(R.string.today_empty_cta),
                            onClick = callbacks.onAddTaskClick,
                        )
                    },
                )
            }
        } else {
            if (state.tasks.isNotEmpty()) {
                item(key = "tasks_section") {
                    SectionBlock(title = stringResource(R.string.today_tasks_title)) {
                        TaskListContainer(
                            tasks = state.tasks,
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = callbacks.onRequestDeleteTask,
                        )
                    }
                }
            }

            if (state.routines.isNotEmpty()) {
                item(key = "routines_section") {
                    SectionBlock(
                        title = stringResource(R.string.today_routines_title),
                        trailingContent = {
                            TextButton(
                                onClick = callbacks.onNavigateToRoutines,
                                modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min),
                            ) {
                                Text(
                                    text = stringResource(R.string.today_routines_view_all),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrandPrimary,
                                )
                            }
                        },
                    ) {
                        RoutineListContainer(
                            routines = state.routines,
                            completedRoutineIds = state.completedRoutineIds,
                            onToggleRoutine = { id, done -> viewModel.toggleRoutine(id, done) },
                            onOpenRoutineMenu = callbacks.onOpenRoutineMenu,
                            onUpdateRoutineProgress = { id, value, wasCompleted ->
                                viewModel.updateRoutineProgress(id, value, wasCompleted)
                            },
                        )
                    }
                }
            }
        }

        item(key = "close_day_card") {
            CloseDayCard(
                isDayClosed = summaryState.dayIsClosed,
                onCloseDayClick = callbacks.onCloseDayClick,
            )
        }
    }
}
