package com.benimgunlerim.domain.service

import com.benimgunlerim.di.ApplicationScope
import com.benimgunlerim.domain.AchievementTracker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Application-level event coordinator that connects global achievement unlocks
 * to the reward and celebration pipeline across all screen lifecycles.
 */
@Singleton
class AppEventCoordinator @Inject constructor(
    @ApplicationScope private val externalScope: CoroutineScope,
    private val achievementTracker: AchievementTracker,
    private val rewardDisplayService: RewardDisplayService,
) {
    @Synchronized
    fun start() {
        if (started) return
        started = true
        externalScope.launch {
            achievementTracker.newUnlock.collect { def ->
                rewardDisplayService.onAchievementUnlocked(def)
            }
        }
    }

    @Volatile
    private var started = false
}
