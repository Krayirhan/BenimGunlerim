package com.benimgunlerim.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.organisms.RoutineRow
import com.benimgunlerim.ui.components.organisms.TaskRow
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun TaskListContainer(
    tasks: List<TodayTaskUi>,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tasks.isEmpty()) return

    val rows = @Composable {
        tasks.forEachIndexed { index, task ->
            key(task.id) {
                TaskRow(
                    title = task.title,
                    isCompleted = task.isCompleted,
                    onToggleComplete = { onToggleTask(task.id) },
                    onDelete = { onDeleteTask(task.id) },
                    dueTime = task.startTime,
                    priority = task.priority,
                    category = task.category,
                    useSurface = false,
                )
                if (index != tasks.lastIndex) {
                    AppDivider()
                }
            }
        }
    }

    if (tasks.size == 1) {
        val task = tasks[0]
        key(task.id) {
            TaskRow(
                modifier = modifier,
                title = task.title,
                isCompleted = task.isCompleted,
                onToggleComplete = { onToggleTask(task.id) },
                onDelete = { onDeleteTask(task.id) },
                dueTime = task.startTime,
                priority = task.priority,
                category = task.category,
            )
        }
    } else {
        AppSurface(modifier = modifier, radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
            Column { rows() }
        }
    }
}

@Composable
fun RoutineListContainer(
    routines: List<TodayRoutineUi>,
    completedRoutineIds: Set<String>,
    onToggleRoutine: (String, Boolean) -> Unit,
    onOpenRoutineMenu: (String) -> Unit,
    onUpdateRoutineProgress: ((String, Float, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (routines.isEmpty()) return

    val rows = @Composable {
        routines.forEachIndexed { index, routine ->
            key(routine.id) {
                val isCompletedToday = routine.id in completedRoutineIds
                RoutineRow(
                    title = routine.name,
                    isCompletedToday = isCompletedToday,
                    onToggle = { onToggleRoutine(routine.id, isCompletedToday) },
                    onMenuClick = { onOpenRoutineMenu(routine.id) },
                    weekHistory = emptyList(),
                    streakCount = routine.currentStreak,
                    targetType = routine.targetType,
                    targetValue = routine.targetValue,
                    targetUnit = routine.targetUnit,
                    currentValue = routine.currentValue,
                    onProgressChange = onUpdateRoutineProgress?.let { updater ->
                        { nextValue -> updater(routine.id, nextValue, isCompletedToday) }
                    },
                    useSurface = false,
                )
                if (index != routines.lastIndex) {
                    AppDivider()
                }
            }
        }
    }

    if (routines.size == 1) {
        val routine = routines[0]
        key(routine.id) {
            val isCompletedToday = routine.id in completedRoutineIds
            RoutineRow(
                modifier = modifier,
                title = routine.name,
                isCompletedToday = isCompletedToday,
                onToggle = { onToggleRoutine(routine.id, isCompletedToday) },
                onMenuClick = { onOpenRoutineMenu(routine.id) },
                weekHistory = emptyList(),
                streakCount = routine.currentStreak,
                targetType = routine.targetType,
                targetValue = routine.targetValue,
                targetUnit = routine.targetUnit,
                currentValue = routine.currentValue,
                onProgressChange = onUpdateRoutineProgress?.let { updater ->
                    { nextValue -> updater(routine.id, nextValue, isCompletedToday) }
                },
            )
        }
    } else {
        AppSurface(modifier = modifier, radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
            Column { rows() }
        }
    }
}
