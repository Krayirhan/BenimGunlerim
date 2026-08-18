package com.benimgunlerim.ui.today

import java.time.LocalDate
import java.time.LocalTime

data class TaskEditDraft(
    val title: String,
    val note: String? = null,
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val category: String? = null,
    val priority: Int = 0,
    val reminderTime: LocalTime? = null,
    val isLightTask: Boolean = false,
)
