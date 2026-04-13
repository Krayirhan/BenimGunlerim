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
class TodayScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun today_screen_root_is_visible() {
        composeTestRule
            .onNodeWithTag(TestTags.TodayRoot)
            .assertIsDisplayed()
    }

    @Test
    fun today_bottom_nav_item_is_visible() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavToday)
            .assertIsDisplayed()
    }

    @Test
    fun today_fab_is_visible() {
        composeTestRule
            .onNodeWithTag(TestTags.TodayFab)
            .assertIsDisplayed()
    }

    @Test
    fun routines_tab_is_accessible_via_nav_tag() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavRoutines)
            .performClick()
        composeTestRule
            .onNodeWithTag(TestTags.RoutinesRoot)
            .assertIsDisplayed()
    }
}
