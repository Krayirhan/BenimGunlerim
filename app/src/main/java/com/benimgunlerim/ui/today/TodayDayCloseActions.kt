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

internal data class TodayDayCloseDependencies(
    val dateTimeProvider: DateTimeProvider,
    val analyticsTracker: AnalyticsTracker,
    val feedbackManager: FeedbackManager,
    val rewardDisplayService: RewardDisplayService,
    val closeDayUseCase: CloseDayUseCase,
    val carryPendingTasksUseCase: CarryPendingTasksUseCase,
    val autoCloseMissedDayUseCase: AutoCloseMissedDayUseCase,
    val saveMissedDaySummaryUseCase: SaveMissedDaySummaryUseCase,
)

/**
 * Day-close and missed-day actions for [TodayViewModel], extracted so the
 * ViewModel itself stays focused on state assembly.
 */
internal class TodayDayCloseActions(
    private val scope: CoroutineScope,
    private val deps: TodayDayCloseDependencies,
    private val uiStateValue: () -> TodayUiState,
    private val emitEffect: (TodayUiEffect) -> Unit,
) {
    /**
     * Optionally moves overdue pending tasks to tomorrow first, then persists the daily summary with
     * the carried count. Order is fixed so the summary row always matches what was carried.
     */
    fun saveDailySummaryWithOptionalCarry(draft: CloseDayDraft) {
        scope.launch {
            runCatching {
                val carried = if (draft.carryOverdueTasks) deps.carryPendingTasksUseCase() else 0
                val state = uiStateValue()
                val result = deps.closeDayUseCase(
                    date = deps.dateTimeProvider.today(),
                    mood = draft.mood,
                    note = draft.note.trim(),
                    completionRate = state.progress,
                    energy = draft.energy,
                    bestMoment = draft.bestMoment.trim(),
                    challenge = draft.challenge.trim(),
                    tomorrowIntention = draft.tomorrowIntention.trim(),
                    carriedCount = carried,
                    streak = state.currentStreak,
                )
                deps.analyticsTracker.track(AnalyticsEvent("daily_summary_completed"))
                if (!result.dayCloseReward.alreadyGranted) {
                    deps.rewardDisplayService.onDailyBonusEarned(
                        xp = result.dayCloseReward.xpGranted,
                        gold = result.dayCloseReward.goldGranted,
                    )
                }
                if (!result.perfectDayReward.alreadyGranted) {
                    deps.rewardDisplayService.onDailyBonusEarned(
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
        scope.launch { deps.autoCloseMissedDayUseCase(date) }
    }

    fun closeMissedDayWithReview(
        date: LocalDate,
        mood: Int,
        carryOverPendingTasks: Boolean,
    ) {
        scope.launch {
            runCatching {
                deps.saveMissedDaySummaryUseCase(
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

    fun saveMissedDaySummary(
        date: LocalDate,
        draft: CloseDayDraft,
    ) {
        scope.launch {
            runCatching {
                deps.saveMissedDaySummaryUseCase(
                    date = date,
                    note = draft.note,
                    mood = draft.mood,
                    energy = draft.energy,
                    bestMoment = draft.bestMoment,
                    challenge = draft.challenge,
                    tomorrowIntention = draft.tomorrowIntention,
                )
                emitEffect(TodayUiEffect.DaySaved("day_saved"))
            }.onFailure {
                emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_save_day))
            }
        }
    }

    fun saveMissedDaySummary(
        date: LocalDate,
        note: String,
        mood: Int,
        energy: Int,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
    ) {
        saveMissedDaySummary(
            date = date,
            draft = CloseDayDraft(
                mood = mood,
                energy = energy,
                note = note,
                bestMoment = bestMoment,
                challenge = challenge,
                tomorrowIntention = tomorrowIntention,
            ),
        )
    }
}
