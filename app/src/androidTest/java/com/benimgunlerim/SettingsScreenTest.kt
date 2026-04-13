package com.benimgunlerim

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.benimgunlerim.ui.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
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
    fun settings_screen_is_visible_after_nav() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavSettings)
            .performClick()
        composeTestRule
            .onNodeWithTag(TestTags.SettingsRoot)
            .assertIsDisplayed()
    }

    @Test
    fun local_data_actions_are_visible() {
        composeTestRule
            .onNodeWithTag(TestTags.BottomNavSettings)
            .performClick()
        composeTestRule
            .onNodeWithTag(TestTags.SettingsRoot)
            .performScrollToNode(hasTestTag(TestTags.SettingsExportButton))
        composeTestRule
            .onNodeWithTag(TestTags.SettingsExportButton)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TestTags.SettingsImportButton)
            .assertExists()
    }
}
