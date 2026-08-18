package com.benimgunlerim

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.benimgunlerim.ui.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutinesScreenTest {

    private val intent = Intent(
        InstrumentationRegistry.getInstrumentation().targetContext,
        MainActivity::class.java,
    ).putExtra("force_onboarding_completed", true)

    @get:Rule
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> =
        AndroidComposeTestRule(
            activityRule = ActivityScenarioRule(intent),
        ) { rule ->
            var activity: MainActivity? = null
            rule.scenario.onActivity { activity = it }
            activity!!
        }

    @Test
    fun routines_root_is_visible_after_nav() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavRoutines)
            .performClick()
        composeTestRule
            .onNodeWithTag(TestTags.RoutinesRoot)
            .assertIsDisplayed()
    }

    @Test
    fun routines_fab_is_visible_on_routines_tab() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavRoutines)
            .performClick()
        composeTestRule
            .onNodeWithTag(TestTags.RoutinesFab)
            .assertIsDisplayed()
    }

    @Test
    fun routines_add_sheet_opens_on_fab_click() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavRoutines)
            .performClick()
        composeTestRule
            .onNodeWithTag(TestTags.RoutinesFab)
            .performClick()
        composeTestRule
            .onNodeWithText(
                InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.routine_sheet_add_title),
            )
            .assertIsDisplayed()
    }
}
