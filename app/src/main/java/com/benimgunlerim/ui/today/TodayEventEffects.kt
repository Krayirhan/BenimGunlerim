package com.benimgunlerim.ui.today

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.domain.model.GameEvent

/**
 * Wires [TodayViewModel]'s one-shot effect and game-event streams to snackbars
 * and the local dialog/banner state owned by [TodayScreen].
 */
@Composable
internal fun TodayEventEffects(
    viewModel: TodayViewModel,
    snackbarHostState: SnackbarHostState,
    onLevelUp: (GameEvent.LevelUp) -> Unit,
    onAchievementUnlocked: (GameEvent.AchievementUnlocked) -> Unit,
    onBlockCompletion: (title: String, subtitle: String, badge: String) -> Unit,
    onMiniBanner: (Pair<String, String>?) -> Unit,
) {
    val undoLabel = stringResource(R.string.action_undo)
    val taskDeletedMsg = stringResource(R.string.today_task_deleted_undo)
    val taskCompletedUndoMsg = stringResource(R.string.today_task_completed_undo_msg)
    val allTasksTitle = stringResource(R.string.today_all_tasks_completed_title)
    val allRoutinesTitle = stringResource(R.string.today_all_routines_completed_title)
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is TodayUiEffect.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = taskDeletedMsg,
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask(effect.taskId)
                    }
                }
                is TodayUiEffect.TaskCompletedUndo -> {
                    val result = snackbarHostState.showSnackbar(
                        message = taskCompletedUndoMsg,
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoTaskToggle(effect.taskId)
                    }
                }
                is TodayUiEffect.ActionFailed -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(effect.messageRes),
                        duration = SnackbarDuration.Short,
                    )
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.gameEvents.collect { event ->
            when (event) {
                is GameEvent.LevelUp -> onLevelUp(event)
                is GameEvent.AchievementUnlocked -> onAchievementUnlocked(event)
                is GameEvent.AllTasksCompleted -> {
                    onBlockCompletion(
                        allTasksTitle,
                        context.getString(R.string.today_all_tasks_completed_body, event.totalCount, event.totalCount),
                        context.getString(R.string.today_xp_bonus_badge, event.xpBonus),
                    )
                }
                is GameEvent.AllRoutinesCompleted -> {
                    onBlockCompletion(
                        allRoutinesTitle,
                        context.getString(R.string.today_all_routines_completed_body, event.streak),
                        context.getString(R.string.today_xp_bonus_badge, event.xpBonus),
                    )
                }
                is GameEvent.MiniBanner -> {
                    onMiniBanner(context.getString(event.messageRes) to event.icon)
                    kotlinx.coroutines.delay(2800)
                    onMiniBanner(null)
                }
                is GameEvent.RewardEarned -> Unit
            }
        }
    }
}
