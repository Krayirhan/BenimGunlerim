package com.benimgunlerim.ui.today

import com.benimgunlerim.R
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.service.RewardDisplayService
import com.benimgunlerim.domain.usecase.AutoCloseMissedDayUseCase
import com.benimgunlerim.domain.usecase.CarryPendingTasksUseCase
import com.benimgunlerim.domain.usecase.CloseDayUseCase
import com.benimgunlerim.domain.usecase.SaveMissedDaySummaryUseCase
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Day-close and missed-day actions for [TodayViewModel], extracted so the
 * ViewModel itself stays focused on state assembly.
 */
@Suppress("LongParameterList")
internal class TodayDayCloseActions(
    private val scope: CoroutineScope,
    private val dateTimeProvider: DateTimeProvider,
    private val analyticsTracker: AnalyticsTracker,
    private val feedbackManager: FeedbackManager,
    private val rewardDisplayService: RewardDisplayService,
    private val closeDayUseCase: CloseDayUseCase,
    private val carryPendingTasksUseCase: CarryPendingTasksUseCase,
    private val autoCloseMissedDayUseCase: AutoCloseMissedDayUseCase,
    private val saveMissedDaySummaryUseCase: SaveMissedDaySummaryUseCase,
    private val uiStateValue: () -> TodayUiState,
    private val emitEffect: (TodayUiEffect) -> Unit,
) {
    /**
     * Optionally moves overdue pending tasks to tomorrow first, then persists the daily summary with
     * the carried count. Order is fixed so the summary row always matches what was carried.
     */
    @Suppress("LongParameterList")
    fun saveDailySummaryWithOptionalCarry(
        note: String,
        mood: Int,
        energy: Int = 3,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
        carryOverdueToTomorrow: Boolean,
    ) {
        scope.launch {
            runCatching {
                val carried = if (carryOverdueToTomorrow) carryPendingTasksUseCase() else 0
                val state = uiStateValue()
                val result = closeDayUseCase(
                    date = dateTimeProvider.today(),
                    mood = mood,
                    note = note.trim(),
                    completionRate = state.progress,
                    energy = energy,
                    bestMoment = bestMoment.trim(),
                    challenge = challenge.trim(),
                    tomorrowIntention = tomorrowIntention.trim(),
                    carriedCount = carried,
                    streak = state.currentStreak,
                )
                analyticsTracker.track(AnalyticsEvent("daily_summary_completed"))
                feedbackManager.celebrationBurst()
                if (!result.dayCloseReward.alreadyGranted) {
                    rewardDisplayService.onDailyBonusEarned(
                        xp = result.dayCloseReward.xpGranted,
                        gold = result.dayCloseReward.goldGranted,
                    )
                }
                if (!result.perfectDayReward.alreadyGranted) {
                    rewardDisplayService.onDailyBonusEarned(
                        xp = result.perfectDayReward.xpGranted,
                        gold = result.perfectDayReward.goldGranted,
                    )
                }
                emitEffect(TodayUiEffect.DaySaved("day_saved"))
            }.onFailure {
                emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_save_day))
            }
        }
    }

    fun autoSaveMissedDay(date: LocalDate) {
        scope.launch { autoCloseMissedDayUseCase(date) }
    }

    fun closeMissedDayWithReview(
        date: LocalDate,
        mood: Int,
        carryOverPendingTasks: Boolean,
    ) {
        scope.launch {
            runCatching {
                saveMissedDaySummaryUseCase(
                    date = date,
                    note = "",
                    mood = mood,
                    energy = 3,
                    carryOverPendingTasks = carryOverPendingTasks,
                )
                emitEffect(TodayUiEffect.DaySaved("missed_day_saved"))
            }.onFailure {
                emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_save_day))
            }
        }
    }

    @Suppress("LongParameterList")
    fun saveMissedDaySummary(
        date: LocalDate,
        note: String,
        mood: Int,
        energy: Int,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
    ) {
        scope.launch {
            runCatching {
                saveMissedDaySummaryUseCase(date, note, mood, energy, bestMoment, challenge, tomorrowIntention)
                emitEffect(TodayUiEffect.DaySaved("day_saved"))
            }.onFailure {
                emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_save_day))
            }
        }
    }
}
