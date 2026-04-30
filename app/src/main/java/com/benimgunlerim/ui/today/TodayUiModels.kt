package com.benimgunlerim.ui.today

data class TodayTaskUi(
    val id: String,
    val title: String,
    val note: String?,
    val plannedDate: String,
    val startTime: String?,
    val category: String?,
    val color: String?,
    val priority: Int,
    val completionState: String,
    val reminderTime: String?,
)

data class TodayRoutineUi(
    val id: String,
    val name: String,
    val preferredTime: String?,
    val color: String?,
    val targetType: String,
    val targetValue: Int?,
    val targetUnit: String?,
    val currentStreak: Int,
    val bestStreak: Int,
)
