package com.benimgunlerim

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.benimgunlerim.ui.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutinesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

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
            .onNodeWithText("Yeni rutin")
            .assertIsDisplayed()
    }
}
