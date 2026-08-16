package com.benimgunlerim.ui

sealed class AppDestination(val route: String) {
    data object Onboarding : AppDestination("onboarding")
    data object Today : AppDestination("today")
    data object Plan : AppDestination("plan")
    data object Routines : AppDestination("routines")
    data object Progress : AppDestination("progress")
    data object Settings : AppDestination("settings")
    data object Achievements : AppDestination("achievements")
    data object Shop : AppDestination("shop")
    data object RoutineDetailPattern : AppDestination("routine_detail/{routineId}") {
        const val ARG_ROUTINE_ID: String = "routineId"
        fun createRoute(routineId: String): String = "routine_detail/$routineId"
    }
}
