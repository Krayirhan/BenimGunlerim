package com.benimgunlerim.ui.routines

import java.time.DayOfWeek

data class RoutineCardUi(
    val id: String,
    val name: String,
    val targetDays: Set<DayOfWeek>,
    val preferredTime: String?,
    val targetType: String,
    val targetValue: Int?,
    val targetUnit: String?,
    val currentStreak: Int,
    val last7Days: List<Boolean>,
    val currentValue: Float = 0f,
)

data class ArchivedRoutineUi(
    val id: String,
    val name: String,
)

data class RoutineDetailRoutineUi(
    val id: String,
    val name: String,
    val targetDays: Set<DayOfWeek>,
    val preferredTime: String?,
    val targetType: String,
    val targetValue: Int?,
    val targetUnit: String?,
)
