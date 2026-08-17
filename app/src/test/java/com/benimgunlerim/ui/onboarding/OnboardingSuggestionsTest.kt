package com.benimgunlerim.ui.onboarding

import com.benimgunlerim.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingSuggestionsTest {

    @Test
    fun suggestedRoutines_duzen_withHafifIntensity_doesNotSelectCloseDayByDefault() {
        val routines = OnboardingSuggestions.suggestedRoutines("duzen", "hafif")
        assertEquals(3, routines.size)
        assertEquals(R.string.onboarding_suggest_morning_routine, routines[0].nameRes)
        assertTrue(routines[0].defaultSelected)
        assertEquals(R.string.onboarding_suggest_daily_plan, routines[1].nameRes)
        assertTrue(routines[1].defaultSelected)
        assertEquals(R.string.onboarding_suggest_close_day, routines[2].nameRes)
        assertFalse(routines[2].defaultSelected)
    }

    @Test
    fun suggestedRoutines_duzen_withDengeliIntensity_selectsCloseDayByDefault() {
        val routines = OnboardingSuggestions.suggestedRoutines("duzen", "dengeli")
        assertEquals(3, routines.size)
        assertTrue(routines[2].defaultSelected)
    }

    @Test
    fun suggestedRoutines_duzenli_withYogunIntensity_selectsWeeklyReviewByDefault() {
        val routines = OnboardingSuggestions.suggestedRoutines("duzenli", "yogun")
        assertEquals(3, routines.size)
        assertEquals(R.string.onboarding_suggest_weekly_review, routines[2].nameRes)
        assertTrue(routines[2].defaultSelected)
    }

    @Test
    fun suggestedRoutines_saglik_returnsWaterWalkAndBreathing() {
        val routines = OnboardingSuggestions.suggestedRoutines("saglik", "dengeli")
        assertEquals(3, routines.size)
        assertEquals(R.string.onboarding_suggest_water, routines[0].nameRes)
        assertEquals(R.string.onboarding_suggest_walk, routines[1].nameRes)
        assertEquals(R.string.onboarding_suggest_breathing, routines[2].nameRes)
    }

    @Test
    fun suggestedRoutines_odak_returnsFocusPomodoroAndDistractions() {
        val routines = OnboardingSuggestions.suggestedRoutines("odak", "hafif")
        assertEquals(3, routines.size)
        assertEquals(R.string.onboarding_task_choose_focus, routines[0].nameRes)
        assertEquals(R.string.onboarding_suggest_pomodoro, routines[1].nameRes)
        assertFalse(routines[2].defaultSelected)
    }

    @Test
    fun suggestedRoutines_unknownNeed_returnsFallback() {
        val routines = OnboardingSuggestions.suggestedRoutines("unknown_need", "hafif")
        assertEquals(2, routines.size)
        assertEquals(R.string.onboarding_suggest_morning_routine, routines[0].nameRes)
        assertEquals(R.string.onboarding_suggest_daily_plan, routines[1].nameRes)
    }

    @Test
    fun suggestedTaskTitle_mapsExpectedResourceForEachNeed() {
        assertEquals(R.string.onboarding_task_plan_day, OnboardingSuggestions.suggestedTaskTitle("duzen"))
        assertEquals(R.string.onboarding_task_set_priorities, OnboardingSuggestions.suggestedTaskTitle("duzenli"))
        assertEquals(R.string.onboarding_task_drink_water, OnboardingSuggestions.suggestedTaskTitle("saglik"))
        assertEquals(R.string.onboarding_task_choose_focus, OnboardingSuggestions.suggestedTaskTitle("odak"))
        assertEquals(R.string.onboarding_task_prepare_list, OnboardingSuggestions.suggestedTaskTitle("basit"))
        assertEquals(R.string.onboarding_task_first, OnboardingSuggestions.suggestedTaskTitle("other"))
    }
}
