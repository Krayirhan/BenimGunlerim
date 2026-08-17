package com.benimgunlerim.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.benimgunlerim.R

data class SuggestedRoutine(
    val nameRes: Int,
    val icon: ImageVector,
    val defaultSelected: Boolean = true,
)

object OnboardingSuggestions {

    fun suggestedRoutines(needId: String, intensityId: String): List<SuggestedRoutine> =
        when (needId) {
            "duzen" -> listOf(
                SuggestedRoutine(R.string.onboarding_suggest_morning_routine, Icons.Rounded.WbSunny, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_daily_plan, Icons.Rounded.CalendarMonth, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_close_day, Icons.Rounded.CheckCircle, defaultSelected = intensityId != "hafif"),
            )
            "duzenli" -> listOf(
                SuggestedRoutine(R.string.onboarding_task_plan_day, Icons.Rounded.CalendarMonth, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_priorities, Icons.Rounded.CenterFocusStrong, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_weekly_review, Icons.Rounded.EditNote, defaultSelected = intensityId == "yogun"),
            )
            "saglik" -> listOf(
                SuggestedRoutine(R.string.onboarding_suggest_water, Icons.Rounded.WaterDrop, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_walk, Icons.Rounded.DirectionsWalk, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_breathing, Icons.Rounded.SelfImprovement, defaultSelected = intensityId != "hafif"),
            )
            "odak" -> listOf(
                SuggestedRoutine(R.string.onboarding_task_choose_focus, Icons.Rounded.CenterFocusStrong, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_pomodoro, Icons.Rounded.Timer, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_remove_distractions, Icons.Rounded.Notifications, defaultSelected = intensityId != "hafif"),
            )
            "basit" -> listOf(
                SuggestedRoutine(R.string.onboarding_task_plan_day, Icons.Rounded.CalendarMonth, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_task_prepare_list, Icons.Rounded.EditNote, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_close_day, Icons.Rounded.CheckCircle, defaultSelected = intensityId != "hafif"),
            )
            else -> listOf(
                SuggestedRoutine(R.string.onboarding_suggest_morning_routine, Icons.Rounded.WbSunny, defaultSelected = true),
                SuggestedRoutine(R.string.onboarding_suggest_daily_plan, Icons.Rounded.CalendarMonth, defaultSelected = true),
            )
        }

    fun suggestedTaskTitle(needId: String): Int = when (needId) {
        "duzen" -> R.string.onboarding_task_plan_day
        "duzenli" -> R.string.onboarding_task_set_priorities
        "saglik" -> R.string.onboarding_task_drink_water
        "odak" -> R.string.onboarding_task_choose_focus
        "basit" -> R.string.onboarding_task_prepare_list
        else -> R.string.onboarding_task_first
    }
}
