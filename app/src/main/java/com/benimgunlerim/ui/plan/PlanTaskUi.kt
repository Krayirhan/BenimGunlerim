package com.benimgunlerim.ui.plan

data class PlanTaskUi(
    val id: String,
    val title: String,
    val plannedDate: String?,
    val isCompleted: Boolean,
)
