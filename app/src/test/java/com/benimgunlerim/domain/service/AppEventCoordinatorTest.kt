package com.benimgunlerim.domain.service

import com.benimgunlerim.R
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.AchievementTracker
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppEventCoordinatorTest {

    private val achievementTracker: AchievementTracker = mockk()
    private val rewardDisplayService: RewardDisplayService = mockk(relaxed = true)

    @Test
    fun start_forwardsUnlockedAchievementsToRewardDisplayService() = runTest {
        val unlockFlow = MutableSharedFlow<AchievementDef>(extraBufferCapacity = 5)
        every { achievementTracker.newUnlock } returns unlockFlow

        val coordinator = AppEventCoordinator(
            externalScope = backgroundScope,
            achievementTracker = achievementTracker,
            rewardDisplayService = rewardDisplayService,
        )
        coordinator.start()
        testScheduler.runCurrent()

        val def = AchievementDef("first_task", "🌱", R.string.achievement_first_task_title, R.string.achievement_first_task_desc, 25, 10)
        unlockFlow.emit(def)
        testScheduler.runCurrent()

        coVerify { rewardDisplayService.onAchievementUnlocked(def) }
    }
}
