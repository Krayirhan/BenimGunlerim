package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.domain.service.GrantResult
import com.benimgunlerim.domain.service.RewardGrantService
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Saves a daily summary entry and grants day-close / perfect-day rewards as applicable.
 */
class CloseDayUseCase @Inject constructor(
    private val dailyStateRepository: DailyStateRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
    private val rewardGrantService: RewardGrantService,
    private val dateTimeProvider: DateTimeProvider,
) {
    data class Result(
        val dayCloseReward: GrantResult,
        val perfectDayReward: GrantResult,
    )

    /**
     * @param date          The date being closed (defaults to today).
     * @param mood          Mood index 0–4 (cok_kotu, kotu, normal, iyi, harika).
     * @param note          Free-text reflection.
     * @param completionRate Day's completion rate 0f–1f.
     * @param streak        Current streak count for achievement checks.
     */
    suspend operator fun invoke(
        date: LocalDate = dateTimeProvider.today(),
        mood: Int,
        note: String,
        completionRate: Float,
        energy: Int = 3,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
        carriedCount: Int = 0,
        streak: Int = 0,
    ): Result {
        val moodLabels = listOf("cok_kotu", "kotu", "normal", "iyi", "harika")
        val moodLabel = moodLabels.getOrElse(mood) { "normal" }

        dailyStateRepository.saveSummary(
            date = date,
            mood = moodLabel,
            note = note,
            completionRate = completionRate,
            energyLevel = energy.coerceIn(1, 5),
            bestMoment = bestMoment.ifBlank { null },
            challenge = challenge.ifBlank { null },
            tomorrowIntention = tomorrowIntention.ifBlank { null },
            carriedTaskCount = carriedCount,
        )

        val prefs = prefsRepository.preferences.first()

        val dayCloseReward = rewardGrantService.grantOnce(
            eventKey = "dayClose:$date",
            xp = GameEngine.XP_DAY_CLOSE,
            currentXp = prefs.totalXp,
        )
        if (!dayCloseReward.alreadyGranted) {
            prefsRepository.incrementDaysClosed()
            if (mood == 4) prefsRepository.incrementHappyMoodCount()
            achievementTracker.checkDayClose(prefs.totalDaysClosed + 1)
            achievementTracker.checkStreak(streak)
            if (mood == 4) achievementTracker.checkHappiness(100)
        }

        val xpAfterDayClose = prefs.totalXp + dayCloseReward.xpGranted
        val perfectDayReward = if (completionRate >= 1f) {
            val result = rewardGrantService.grantOnce(
                eventKey = "perfectDay:$date",
                xp = GameEngine.XP_ALL_TASKS_BONUS + GameEngine.XP_ALL_ROUTINES_BONUS,
                gold = GameEngine.GOLD_PERFECT_DAY,
                happinessDelta = GameEngine.HAPPINESS_STREAK,
                currentXp = xpAfterDayClose,
            )
            if (!result.alreadyGranted) {
                prefsRepository.incrementPerfectDays()
                achievementTracker.checkPerfectDay(prefs.totalPerfectDays + 1)
            }
            result
        } else {
            GrantResult.AlreadyGranted
        }

        return Result(dayCloseReward = dayCloseReward, perfectDayReward = perfectDayReward)
    }
}
