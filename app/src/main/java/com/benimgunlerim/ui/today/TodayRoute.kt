package com.benimgunlerim.ui.today

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TodayRoute(
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onOpenRoutineDetail: (String) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    TodayScreen(
        viewModel = viewModel,
        onNavigateToRoutines = onNavigateToRoutines,
        onNavigateToPlan = onNavigateToPlan,
        onNavigateToSettings = onNavigateToSettings,
        onOpenRoutineDetail = onOpenRoutineDetail,
    )
}
