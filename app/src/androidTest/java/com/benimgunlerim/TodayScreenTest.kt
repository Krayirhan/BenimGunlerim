package com.benimgunlerim

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun today_screen_bottom_nav_is_visible() {
        composeTestRule
            .onNodeWithText("Bugün")
            .assertIsDisplayed()
    }

    @Test
    fun routines_tab_is_accessible() {
        composeTestRule
            .onNodeWithText("Rutinler")
            .performClick()
        composeTestRule
            .onNodeWithText("Rutinler")
            .assertIsDisplayed()
    }
}
