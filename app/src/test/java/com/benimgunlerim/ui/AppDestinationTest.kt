package com.benimgunlerim.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {

    @Test
    fun routineDetailPattern_exposesExpectedRouteAndArgKey() {
        assertEquals(
            "routine_detail/{routineId}",
            AppDestination.RoutineDetailPattern.route,
        )
        assertEquals(
            "routineId",
            AppDestination.RoutineDetailPattern.ARG_ROUTINE_ID,
        )
    }

    @Test
    fun createRoute_buildsDetailRouteWithRoutineId() {
        val route = AppDestination.RoutineDetailPattern.createRoute("routine-42")
        assertEquals("routine_detail/routine-42", route)
    }
}
