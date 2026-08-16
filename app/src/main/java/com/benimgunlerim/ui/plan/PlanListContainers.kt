package com.benimgunlerim.ui.plan

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.organisms.TaskRow
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun PlanTaskListContainer(
    tasks: List<PlanTaskUi>,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tasks.isEmpty()) return

    if (tasks.size == 1) {
        val task = tasks.first()
        TaskRow(
            title = task.title,
            isCompleted = task.isCompleted,
            onToggleComplete = { onToggleTask(task.id) },
            onDelete = { onDeleteTask(task.id) },
            dueTime = task.startTime,
            priority = task.priority,
            category = task.category,
            useSurface = true,
            modifier = modifier,
        )
    } else {
        AppSurface(radius = AppTokens.Radius.md, modifier = modifier) {
            Column {
                tasks.forEachIndexed { index, task ->
                    if (index > 0) AppDivider()
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
                }
            }
        }
    }
}
