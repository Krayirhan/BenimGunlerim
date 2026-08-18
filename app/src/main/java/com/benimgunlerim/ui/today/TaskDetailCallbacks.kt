package com.benimgunlerim.ui.today

import com.benimgunlerim.data.local.entity.SubTaskEntity
import java.time.LocalDate

internal data class TaskDetailCallbacks(
    val onSave: (title: String, note: String?, date: LocalDate, startTime: String?, category: String?, priority: Int, reminderTime: String?) -> Unit,
    val onMoveTomorrow: () -> Unit,
    val onDelete: () -> Unit,
    val onAddSubTask: (String) -> Unit,
    val onToggleSubTask: (SubTaskEntity) -> Unit,
    val onDeleteSubTask: (SubTaskEntity) -> Unit,
)
