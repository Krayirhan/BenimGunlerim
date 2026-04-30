package com.benimgunlerim

import android.content.Intent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.benimgunlerim.ui.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayScreenTest {

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
    fun today_close_day_card_is_visible() {
        composeTestRule
            .onNodeWithTag(TestTags.TodayCloseDayCard)
            .assertIsDisplayed()
    }

    @Test
    fun today_snapshot_error_banner_hidden_when_load_ok() {
        composeTestRule.onAllNodesWithTag(TestTags.TodaySnapshotErrorBanner).assertCountEquals(0)
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

    @Test
    fun deleting_completed_task_shows_confirmation_dialog() {
        val title = "Tamamlanmis test gorevi"

        composeTestRule.onNodeWithTag(TestTags.TodayFab).performClick()
        composeTestRule.onNodeWithText("Görev adı").performTextInput(title)
        composeTestRule.onNodeWithText("Kaydet").performClick()

        // Mark it completed first.
        composeTestRule.onNodeWithContentDescription("Tamamlandı olarak işaretle").performClick()

        // Try to delete from overflow menu.
        composeTestRule.onNodeWithContentDescription("Görev menüsü").performClick()
        composeTestRule.onNodeWithText("Sil").performClick()

        composeTestRule.onNodeWithText("Tamamlanan görevi sil?").assertIsDisplayed()
    }
}