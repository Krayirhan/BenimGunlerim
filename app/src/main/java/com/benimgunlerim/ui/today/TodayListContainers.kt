package com.benimgunlerim.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
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

    // Tek öğede kendi yüzeyi yeter; iki veya daha fazlasında tek dış kapsayıcı yüzey kullanılır.
    if (tasks.size == 1) {
        TaskRow(
            modifier = modifier,
            title = tasks[0].title,
            isCompleted = tasks[0].isCompleted,
            onToggleComplete = { onToggleTask(tasks[0].id) },
            onDelete = { onDeleteTask(tasks[0].id) },
            dueTime = tasks[0].startTime,
            priority = tasks[0].priority,
            category = tasks[0].category,
        )
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
    modifier: Modifier = Modifier,
) {
    if (routines.isEmpty()) return

    val rows = @Composable {
        routines.forEachIndexed { index, routine ->
            val isCompletedToday = routine.id in completedRoutineIds
            RoutineRow(
                title = routine.name,
                isCompletedToday = isCompletedToday,
                // toggleRoutine "completedToday" mevcut durumu bekler (ToggleRoutineUseCase
                // bunu kendi içinde ters çevirir) — negatif göndermek toggle'ı sessizce bozar.
                onToggle = { onToggleRoutine(routine.id, isCompletedToday) },
                onMenuClick = { onOpenRoutineMenu(routine.id) },
                weekHistory = emptyList(),
                streakCount = routine.currentStreak,
                useSurface = false,
            )
            if (index != routines.lastIndex) {
                AppDivider()
            }
        }
    }

    // Tek öğede kendi yüzeyi yeter; iki veya daha fazlasında tek dış kapsayıcı yüzey kullanılır.
    if (routines.size == 1) {
        val routine = routines[0]
        val isCompletedToday = routine.id in completedRoutineIds
        RoutineRow(
            modifier = modifier,
            title = routine.name,
            isCompletedToday = isCompletedToday,
            onToggle = { onToggleRoutine(routine.id, isCompletedToday) },
            onMenuClick = { onOpenRoutineMenu(routine.id) },
            weekHistory = emptyList(),
            streakCount = routine.currentStreak,
        )
    } else {
        AppSurface(modifier = modifier, radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
            Column { rows() }
        }
    }
}
