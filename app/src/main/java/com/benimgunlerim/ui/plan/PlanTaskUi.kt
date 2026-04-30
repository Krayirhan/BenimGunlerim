package com.benimgunlerim.ui.plan

data class PlanTaskUi(
    val id: String,
    val title: String,
    val note: String?,
    val plannedDate: String?,
    val startTime: String?,
    val category: String?,
    val priority: Int,
    val reminderTime: String?,
    val isCompleted: Boolean,
)
