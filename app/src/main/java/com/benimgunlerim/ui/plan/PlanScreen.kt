@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.DynamicCalendarBadge
import com.benimgunlerim.ui.components.molecules.EmptyState
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.AddTaskSheet
import com.benimgunlerim.ui.components.organisms.WeekPicker
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanScreen(
    viewModel: PlanViewModel = hiltViewModel(),
    onNavigateToToday: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val undoLabel = stringResource(R.string.action_undo)
    val taskDeletedMsg = stringResource(R.string.plan_task_deleted_undo)
    val dayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM EEEE", Locale("tr")) }

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is PlanUiEffect.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = taskDeletedMsg,
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask(effect.taskId)
                    }
                }
                else -> Unit
            }
        }
    }

    ScreenScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag(TestTags.PlanFab),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.plan_add_task_fab_cd),
                )
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.PlanRoot),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            item(key = "week_picker") {
                WeekPicker(
                    selectedDate = state.selectedDate,
                    modifier = Modifier.testTag(TestTags.PlanWeekPicker),
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    onPreviousWeek = { viewModel.selectPreviousWeek() },
                    onNextWeek = { viewModel.selectNextWeek() },
                    onSelectToday = { viewModel.selectToday() },
                    taskCountByDate = state.weeklyTaskCounts,
                    weekStart = state.weekStart,
                )
            }

            // Selected Day Summary Info Bar
            item(key = "day_summary_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTokens.Spacing.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.selectedDate.format(dayFormatter),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.plan_day_summary_format, state.tasksForDay.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.overdueTasks.isNotEmpty()) {
                item(key = "overdue_tasks") {
                    SectionBlock(
                        title = stringResource(R.string.plan_overdue_tasks_title),
                        trailingContent = {
                            TextButton(onClick = { viewModel.moveOverdueTasksToDate(state.selectedDate) }) {
                                Text(stringResource(R.string.plan_move_overdue_to_selected))
                            }
                        },
                    ) {
                        PlanTaskListContainer(
                            tasks = state.overdueTasks,
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                        )
                    }
                }
            }

            if (state.tasksForDay.isEmpty() && state.overdueTasks.isEmpty()) {
                item(key = "empty_day") {
                    EmptyState(
                        iconContent = { DynamicCalendarBadge(date = state.selectedDate) },
                        title = stringResource(R.string.plan_empty_day_title),
                        description = stringResource(R.string.plan_empty_day_desc),
                        action = {
                            AppButton(
                                text = stringResource(R.string.plan_empty_day_cta),
                                onClick = { showAddSheet = true },
                            )
                        },
                    )
                }
            } else {
                if (state.tasksForDay.isNotEmpty()) {
                    item(key = "tasks_for_day") {
                        SectionBlock(title = stringResource(R.string.plan_tasks_for_day_title)) {
                            PlanTaskListContainer(
                                tasks = state.tasksForDay,
                                onToggleTask = { viewModel.toggleTask(it) },
                                onDeleteTask = { viewModel.deleteTask(it) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTaskSheet(
            onDismiss = { showAddSheet = false },
            onSave = { title, dateStr, startTime, category, priority, reminderTime ->
                val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(state.selectedDate)
                viewModel.addTask(title, date, null, startTime, category, priority, reminderTime)
            },
            initialDate = state.selectedDate.toString(),
        )
    }
}
