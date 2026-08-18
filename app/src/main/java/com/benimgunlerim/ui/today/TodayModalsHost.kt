package com.benimgunlerim.ui.today

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.ui.components.calm.BrainDumpDialog
import com.benimgunlerim.ui.components.calm.ResetDialog
import com.benimgunlerim.ui.components.gamification.AchievementDialog
import com.benimgunlerim.ui.components.gamification.BlockCompletionSheet
import com.benimgunlerim.ui.components.gamification.LevelUpDialog
import com.benimgunlerim.ui.components.organisms.AddRoutineSheet
import com.benimgunlerim.ui.components.organisms.AddTaskSheet
import com.benimgunlerim.ui.components.organisms.RoutineActionsSheet
import com.benimgunlerim.ui.components.organisms.RoutineArchiveConfirmDialog
import java.time.LocalDate

/**
 * Hosts every dialog/bottom-sheet/modal that [TodayScreen] can show, driven by
 * hoisted [TodayModalStates] so this file owns no state of its own.
 */
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
internal fun TodayModalsHost(
    state: TodayUiState,
    today: LocalDate,
    viewModel: TodayViewModel,
    completedCount: Int,
    totalCount: Int,
    modals: TodayModalStates,
) {
    if (modals.showFabMenu.value) {
        TodayFabMenuSheet(
            isLightDayMode = state.isLightDayMode,
            onDismiss = { modals.showFabMenu.value = false },
            onAddTaskClick = {
                modals.showFabMenu.value = false
                modals.showAddTaskSheet.value = true
            },
            onBrainDumpClick = {
                modals.showFabMenu.value = false
                modals.showBrainDumpDialog.value = true
            },
            onResetClick = {
                modals.showFabMenu.value = false
                modals.showResetDialog.value = true
            },
            onToggleLightDayClick = {
                modals.showFabMenu.value = false
                viewModel.toggleLightDayMode(!state.isLightDayMode)
            },
        )
    }

    if (modals.showResetDialog.value) {
        ResetDialog(
            onDismiss = { modals.showResetDialog.value = false },
            onEnableLightDay = { viewModel.toggleLightDayMode(true) },
            onPickTask = { /* user continues to task list */ },
        )
    }

    if (modals.showBrainDumpDialog.value) {
        BrainDumpDialog(
            onDismiss = { modals.showBrainDumpDialog.value = false },
            onAddTasks = { titles -> viewModel.addTasksFromBrainDump(titles) },
        )
    }

    if (modals.showAddTaskSheet.value) {
        AddTaskSheet(
            onDismiss = { modals.showAddTaskSheet.value = false },
            onSave = { title, _, startTime, category, priority, reminderTime ->
                viewModel.addTask(title, null, today, startTime, category, priority, reminderTime)
            },
            initialDate = today.toString(),
        )
    }

    if (modals.showCloseSheet.value) {
        CloseSheetModal(
            completedCount = completedCount,
            totalCount = totalCount,
            overdueCount = state.overdueTasks.size,
            onDismiss = { modals.showCloseSheet.value = false },
            onSave = { draft ->
                viewModel.saveDailySummaryWithOptionalCarry(draft)
                modals.showCloseSheet.value = false
            },
        )
    }

    if (modals.showMissedDaySheet.value && state.missedDay != null) {
        val missed = state.missedDay
        MissedDayReviewSheet(
            date = missed,
            completedCount = state.missedDayCompletedCount,
            totalCount = state.missedDayTotalCount,
            pendingTaskCount = state.missedDayPendingTaskCount,
            onDismiss = { modals.showMissedDaySheet.value = false },
            onCloseAndStart = { mood, carryOver ->
                viewModel.closeMissedDayWithReview(date = missed, mood = mood, carryOverPendingTasks = carryOver)
                modals.showMissedDaySheet.value = false
            },
            onArchiveAsIs = {
                viewModel.autoSaveMissedDay(missed)
                modals.showMissedDaySheet.value = false
            },
        )
    }

    modals.menuRoutineId.value?.let { routineId ->
        RoutineActionsSheet(
            onDismiss = { modals.menuRoutineId.value = null },
            onEdit = {
                modals.menuRoutineId.value = null
                modals.editingRoutine.value = state.routines.firstOrNull { it.id == routineId }
            },
            onSkip = {
                viewModel.skipRoutine(routineId)
                modals.menuRoutineId.value = null
            },
            onDelete = {
                modals.archiveRoutineId.value = routineId
                modals.menuRoutineId.value = null
            },
        )
    }

    modals.archiveRoutineId.value?.let { routineId ->
        RoutineArchiveConfirmDialog(
            onDismiss = { modals.archiveRoutineId.value = null },
            onConfirm = {
                viewModel.archiveRoutine(routineId)
                modals.archiveRoutineId.value = null
            },
        )
    }

    modals.deleteTaskId.value?.let { taskId ->
        val task = state.tasks.firstOrNull { it.id == taskId }
            ?: state.overdueTasks.firstOrNull { it.id == taskId }
        if (task != null) {
            TaskDeleteConfirmDialog(
                taskTitle = task.title,
                onDismiss = { modals.deleteTaskId.value = null },
                onConfirm = {
                    viewModel.deleteTask(taskId)
                    modals.deleteTaskId.value = null
                },
            )
        } else {
            modals.deleteTaskId.value = null
        }
    }

    modals.editingRoutine.value?.let { routine ->
        AddRoutineSheet(
            onDismiss = { modals.editingRoutine.value = null },
            onSave = { title, targetDays, reminderTime, _, _, _ ->
                viewModel.updateRoutine(routine.id, title, targetDays, reminderTime)
            },
            isEditMode = true,
            initialTitle = routine.name,
            initialDays = routine.targetDays,
            initialReminderTime = routine.preferredTime,
        )
    }

    if (state.gameState.celebrationEffectsEnabled) modals.levelUpEvent.value?.let { ev ->
        LevelUpDialog(
            level = ev.level,
            title = stringResource(ev.titleRes),
            xpBonus = ev.xpBonus,
            onDismiss = { modals.levelUpEvent.value = null },
        )
    }

    if (state.gameState.celebrationEffectsEnabled) modals.achievementEvent.value?.let { ev ->
        AchievementDialog(
            emoji = ev.emoji,
            title = stringResource(ev.titleRes),
            description = stringResource(ev.descriptionRes),
            xpReward = ev.xpReward,
            showParticles = true,
            onDismiss = { modals.achievementEvent.value = null },
        )
    }

    if (state.gameState.celebrationEffectsEnabled) modals.blockCompletionData.value?.let { (title, subtitle, badge) ->
        BlockCompletionSheet(
            title = title,
            subtitle = subtitle,
            badgeText = badge,
            isLightDayMode = state.isLightDayMode,
            onCloseDayClick = {
                modals.blockCompletionData.value = null
                modals.showCloseSheet.value = true
            },
            onContinueClick = { modals.blockCompletionData.value = null },
            onDismiss = { modals.blockCompletionData.value = null },
        )
    }
}
