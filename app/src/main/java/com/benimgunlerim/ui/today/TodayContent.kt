package com.benimgunlerim.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.benimgunlerim.domain.model.TaskCompletionState
import com.benimgunlerim.ui.components.ScreenBackground
import com.benimgunlerim.ui.theme.CandyPrimary
import java.time.LocalDate

@Composable
internal fun TodayContent(
    state: TodayUiState,
    today: LocalDate,
    completed: Int,
    total: Int,
    dayIsClosed: Boolean,
    padding: PaddingValues,
    haptic: HapticFeedback,
    viewModel: TodayViewModel,
    onNavigateToRoutines: () -> Unit,
    onNavigateToPlan: () -> Unit,
    onOpenRoutineDetail: (String) -> Unit,
    onOpenTask: (String) -> Unit,
    onShowAddTaskSheet: () -> Unit,
    onShowMissedDaySheet: () -> Unit,
    onShowCloseDaySheet: () -> Unit,
    onConfirmDeleteCompletedTask: (TodayTaskUi) -> Unit,
) {
    if (state.isLoading) {
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxSize().background(ScreenBackground()).padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            CircularProgressIndicator(color = CandyPrimary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground())
            .padding(padding),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.snapshotLoadError) {
            item(key = "snapshot_err") {
                SnapshotErrorBanner(onRetry = { viewModel.retrySnapshotLoad() })
            }
        }
        item(key = "header") {
            TodayHeaderCard(
                today = today,
                streak = state.currentStreak,
                happiness = state.gameState.happiness,
                completed = completed,
                total = total,
                progress = state.progress,
            )
        }
        item(key = "list") {
            val listActions = TodayListActions(
                onToggleRoutine = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleRoutine(it.id, completedToday = it.id in state.completedRoutineIds)
                },
                onRoutineProgressChange = { routine, value, wasCompleted ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.updateRoutineProgress(routine.id, value, wasCompleted)
                },
                onOpenRoutineDetail = { onOpenRoutineDetail(it.id) },
                onToggleTask = { task ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleTask(task.id)
                },
                onOpenTask = { onOpenTask(it.id) },
                onDeleteTask = { task ->
                    if (task.completionState == TaskCompletionState.COMPLETED.value) {
                        onConfirmDeleteCompletedTask(task)
                    } else {
                        viewModel.deleteTask(task.id)
                    }
                },
                onMoveOverdueToday = { viewModel.moveTaskToDate(it.id, today) },
                onMoveAllOverdueToday = { viewModel.moveAllOverdueTo(today) },
                onMoveAllOverdueTomorrow = { viewModel.moveAllOverdueTo(today.plusDays(1)) },
                onAddTask = onShowAddTaskSheet,
                onNavigateToRoutines = onNavigateToRoutines,
                onNavigateToPlan = onNavigateToPlan,
            )
            TodayList(
                today = today,
                routines = state.routines,
                tasks = state.tasks,
                overdueTasks = state.overdueTasks,
                completedRoutineIds = state.completedRoutineIds,
                completionLogs = state.completionLogs,
                snapshotLoadError = state.snapshotLoadError,
                dayLocked = dayIsClosed,
                actions = listActions,
            )
        }
        item(key = "closeDay") {
            CloseDayCard(
                completed = completed,
                total = total,
                progress = state.progress,
                isClosed = dayIsClosed,
                mood = state.todayState?.mood,
                closedNote = state.todayState?.note,
                closedEnergyLevel = state.todayState?.energyLevel,
                canCloseDay = state.canCloseDay,
                dailySummaryTime = state.dailySummaryTime,
            ) { onShowCloseDaySheet() }
        }
        val capturedMissedDay = state.missedDay
        if (capturedMissedDay != null) {
            item(key = "missedDay") {
                MissedDayBanner(
                    date = capturedMissedDay,
                    onReview = onShowMissedDaySheet,
                    onDismiss = { viewModel.autoSaveMissedDay(capturedMissedDay) },
                )
            }
        }
        item(key = "fab_safe_space") {
            Spacer(Modifier.height(16.dp))
        }
    }
}
