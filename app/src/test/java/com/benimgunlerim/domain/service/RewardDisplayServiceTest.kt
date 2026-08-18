package com.benimgunlerim.domain.service

import com.benimgunlerim.R
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesSource
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.FeedbackManager
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RewardDisplayServiceTest {
    private val feedbackManager = mockk<FeedbackManager>(relaxed = true)
    private val preferences = MutableStateFlow(UserPreferences(celebrationEffectsEnabled = true))
    private val preferencesSource = object : UserPreferencesSource {
        override val preferences = this@RewardDisplayServiceTest.preferences
    }

    @Test
    fun disabledEffects_suppressCelebrationFeedbackAndEvents() = runTest {
        preferences.value = UserPreferences(celebrationEffectsEnabled = false)
        val service = RewardDisplayService(feedbackManager, preferencesSource)

        service.onAchievementUnlocked(sampleAchievement)
        service.emitMiniBanner(R.string.today_mini_banner_first_task)
        service.emitAllTasksCompleted(totalCount = 2)

        verify(exactly = 0) { feedbackManager.celebrationBurst() }
        verify(exactly = 0) { feedbackManager.tapLight() }
    }

    @Test
    fun disabledEffects_doNotPlayRewardSound() = runTest {
        preferences.value = UserPreferences(celebrationEffectsEnabled = false)
        val service = RewardDisplayService(feedbackManager, preferencesSource)

        service.onDailyBonusEarned(xp = 10, gold = 5)

        verify(exactly = 0) { feedbackManager.playSound(any(), any()) }
    }

    @Test
    fun enabledEffects_triggerAchievementFeedback() = runTest {
        val service = RewardDisplayService(feedbackManager, preferencesSource)

        service.onAchievementUnlocked(sampleAchievement)

        verify(exactly = 1) { feedbackManager.celebrationBurst() }
    }

    private companion object {
        val sampleAchievement = AchievementDef(
            id = "test",
            emoji = "*",
            titleRes = R.string.achievement_first_task_title,
            descriptionRes = R.string.achievement_first_task_desc,
        )
    }
}
