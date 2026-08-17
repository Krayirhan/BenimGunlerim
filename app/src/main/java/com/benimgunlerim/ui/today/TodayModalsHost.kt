package com.benimgunlerim.ui.today

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.benimgunlerim.domain.model.GameEvent
import com.benimgunlerim.ui.components.calm.BrainDumpDialog
import com.benimgunlerim.ui.components.calm.ResetDialog
import com.benimgunlerim.ui.components.gamification.AchievementDialog
import com.benimgunlerim.ui.components.gamification.BlockCompletionSheet
import com.benimgunlerim.ui.components.gamification.LevelUpDialog
import com.benimgunlerim.ui.components.organisms.AddRoutineSheet
import com.benimgunlerim.ui.components.organisms.AddTaskSheet
import com.benimgunlerim.ui.components.organisms.RoutineActionsSheet
import java.time.LocalDate

/**
 * Hosts every dialog/bottom-sheet/modal that [TodayScreen] can show, driven by
 * hoisted [MutableState] so this file owns no state of its own.
 */
@Suppress("LongParameterList")
@Composable
internal fun TodayModalsHost(
    state: TodayUiState,
    today: LocalDate,
    viewModel: TodayViewModel,
    completedCount: Int,
    totalCount: Int,
    showFabMenu: MutableState<Boolean>,
    showResetDialog: MutableState<Boolean>,
    showBrainDumpDialog: MutableState<Boolean>,
    showAddTaskSheet: MutableState<Boolean>,
    showCloseSheet: MutableState<Boolean>,
    showMissedDaySheet: MutableState<Boolean>,
    menuRoutineId: MutableState<String?>,
    editingRoutine: MutableState<TodayRoutineUi?>,
    levelUpEvent: MutableState<GameEvent.LevelUp?>,
    achievementEvent: MutableState<GameEvent.AchievementUnlocked?>,
    blockCompletionData: MutableState<Triple<String, String, String>?>,
) {
    if (showFabMenu.value) {
        TodayFabMenuSheet(
            isLightDayMode = state.isLightDayMode,
            onDismiss = { showFabMenu.value = false },
            onAddTaskClick = {
                showFabMenu.value = false
                showAddTaskSheet.value = true
            },
            onBrainDumpClick = {
                showFabMenu.value = false
                showBrainDumpDialog.value = true
            },
            onResetClick = {
                showFabMenu.value = false
                showResetDialog.value = true
            },
            onToggleLightDayClick = {
                showFabMenu.value = false
                viewModel.toggleLightDayMode(!state.isLightDayMode)
            },
        )
    }

    if (showResetDialog.value) {
        ResetDialog(
            onDismiss = { showResetDialog.value = false },
            onEnableLightDay = { viewModel.toggleLightDayMode(true) },
            onPickTask = { /* user continues to task list */ },
        )
    }

    if (showBrainDumpDialog.value) {
        BrainDumpDialog(
            onDismiss = { showBrainDumpDialog.value = false },
            onAddTasks = { titles -> viewModel.addTasksFromBrainDump(titles) },
        )
    }

    if (showAddTaskSheet.value) {
        AddTaskSheet(
            onDismiss = { showAddTaskSheet.value = false },
            onSave = { title, _, startTime, category, priority, reminderTime ->
                viewModel.addTask(title, null, today, startTime, category, priority, reminderTime)
            },
            initialDate = today.toString(),
        )
    }

    if (showCloseSheet.value) {
        CloseSheetModal(
            completedCount = completedCount,
            totalCount = totalCount,
            overdueCount = state.overdueTasks.size,
            onDismiss = { showCloseSheet.value = false },
            onSave = { mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks ->
                viewModel.saveDailySummaryWithOptionalCarry(
                    note = note,
                    mood = mood,
                    energy = energy,
                    bestMoment = bestMoment,
                    challenge = challenge,
                    tomorrowIntention = tomorrowIntention,
                    carryOverdueToTomorrow = carryTasks,
                )
                showCloseSheet.value = false
            },
        )
    }

    if (showMissedDaySheet.value && state.missedDay != null) {
        val missed = state.missedDay
        MissedDayReviewSheet(
            date = missed,
            completedCount = state.missedDayCompletedCount,
            totalCount = state.missedDayTotalCount,
            pendingTaskCount = state.missedDayPendingTaskCount,
            onDismiss = { showMissedDaySheet.value = false },
            onCloseAndStart = { mood, carryOver ->
                viewModel.closeMissedDayWithReview(date = missed, mood = mood, carryOverPendingTasks = carryOver)
                showMissedDaySheet.value = false
            },
            onArchiveAsIs = {
                viewModel.autoSaveMissedDay(missed)
                showMissedDaySheet.value = false
            },
        )
    }

    menuRoutineId.value?.let { routineId ->
        RoutineActionsSheet(
            onDismiss = { menuRoutineId.value = null },
            onEdit = {
                menuRoutineId.value = null
                editingRoutine.value = state.routines.firstOrNull { it.id == routineId }
            },
            onSkip = {
                viewModel.skipRoutine(routineId)
                menuRoutineId.value = null
            },
            onDelete = {
                viewModel.archiveRoutine(routineId)
                menuRoutineId.value = null
            },
        )
    }

    editingRoutine.value?.let { routine ->
        AddRoutineSheet(
            onDismiss = { editingRoutine.value = null },
            onSave = { title, targetDays, reminderTime, _, _, _ ->
                viewModel.updateRoutine(routine.id, title, targetDays, reminderTime)
            },
            isEditMode = true,
            initialTitle = routine.name,
            initialDays = routine.targetDays,
            initialReminderTime = routine.preferredTime,
        )
    }

    levelUpEvent.value?.let { ev ->
        LevelUpDialog(
            level = ev.level,
            title = ev.title,
            xpBonus = ev.xpBonus,
            onDismiss = { levelUpEvent.value = null },
        )
    }

    achievementEvent.value?.let { ev ->
        AchievementDialog(
            emoji = ev.emoji,
            title = ev.title,
            description = ev.description,
            xpReward = ev.xpReward,
            onDismiss = { achievementEvent.value = null },
        )
    }

    blockCompletionData.value?.let { (title, subtitle, badge) ->
        BlockCompletionSheet(
            title = title,
            subtitle = subtitle,
            badgeText = badge,
            isLightDayMode = state.isLightDayMode,
            onCloseDayClick = {
                blockCompletionData.value = null
                showCloseSheet.value = true
            },
            onContinueClick = { blockCompletionData.value = null },
            onDismiss = { blockCompletionData.value = null },
        )
    }
}
