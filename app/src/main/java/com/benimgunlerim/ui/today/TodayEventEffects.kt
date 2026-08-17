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
                        message = "✓ Görev tamamlandı · +10 XP",
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
                        "Bugünün görevleri tamamlandı 🎉",
                        "${event.totalCount} / ${event.totalCount} görev tamamlandı",
                        "+${event.xpBonus} XP Bonusu",
                    )
                }
                is GameEvent.AllRoutinesCompleted -> {
                    onBlockCompletion(
                        "Bugünün rutinleri tamamlandı",
                        "Ritmini korudun. Seri: ${event.streak} gün",
                        "+${event.xpBonus} XP Bonusu",
                    )
                }
                is GameEvent.MiniBanner -> {
                    onMiniBanner(event.message to event.icon)
                    kotlinx.coroutines.delay(2800)
                    onMiniBanner(null)
                }
                is GameEvent.RewardEarned -> Unit
            }
        }
    }
}
