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
class RoutinesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun routines_fab_is_visible_on_routines_tab() {
        // Navigate to Routines tab
        composeTestRule
            .onNodeWithText("Rutinler")
            .performClick()

        // FAB should be visible (contentDescription set on FAB)
        composeTestRule
            .onNodeWithContentDescription("Rutin ekle")
            .assertIsDisplayed()
    }

    @Test
    fun routines_add_sheet_opens_on_fab_click() {
        composeTestRule
            .onNodeWithText("Rutinler")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Rutin ekle")
            .performClick()

        // Bottom sheet title should appear
        composeTestRule
            .onNodeWithText("Yeni rutin")
            .assertIsDisplayed()
    }
}
