package com.benimgunlerim.ui.today

internal data class TodayListActions(
    val onToggleRoutine: (TodayRoutineUi) -> Unit,
    val onRoutineProgressChange: (TodayRoutineUi, Float, Boolean) -> Unit,
    val onOpenRoutineDetail: (TodayRoutineUi) -> Unit,
    val onToggleTask: (TodayTaskUi) -> Unit,
    val onOpenTask: (TodayTaskUi) -> Unit,
    val onDeleteTask: (TodayTaskUi) -> Unit,
    val onMoveOverdueToday: (TodayTaskUi) -> Unit,
    val onMoveAllOverdueToday: () -> Unit,
    val onMoveAllOverdueTomorrow: () -> Unit,
    val onAddTask: () -> Unit,
    val onNavigateToRoutines: () -> Unit,
    val onNavigateToPlan: () -> Unit,
)

