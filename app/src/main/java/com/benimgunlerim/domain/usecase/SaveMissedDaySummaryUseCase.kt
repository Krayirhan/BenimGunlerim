package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.isScheduledFor
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.ProgressCalculator
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.TaskCompletionState
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SaveMissedDaySummaryUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val closeDayUseCase: CloseDayUseCase,
    private val dateTimeProvider: DateTimeProvider,
) {
    suspend operator fun invoke(
        date: LocalDate,
        note: String = "",
        mood: Int = 2,
        energy: Int = 3,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
        carryOverPendingTasks: Boolean = true,
    ) {
        val today = dateTimeProvider.today()

        // 1. Calculate actual completion rate for the missed day
        val dayTasks = taskRepository.observeByDate(date).first()
        val completedTasks = dayTasks.count { it.completionState == TaskCompletionState.COMPLETED.value }
        val activeRoutines = routineRepository.observeActive().first()
        val scheduledRoutines = activeRoutines.filter { it.isScheduledFor(date.dayOfWeek) }
        val dayLogs = completionLogRepository.observeByDate(date).first()
        val completedRoutines = scheduledRoutines.count { r ->
            dayLogs.any {
                it.entityType == CompletionEntityType.ROUTINE.value &&
                it.entityId == r.id &&
                it.status == CompletionStatus.COMPLETED.value
            }
        }

        val realRate = ProgressCalculator.dailyProgress(
            totalTasks = dayTasks.size,
            completedTasks = completedTasks,
            totalRoutines = scheduledRoutines.size,
            completedRoutines = completedRoutines,
        )

        // 2. If requested, carry over pending tasks from the missed day to today
        var carriedCount = 0
        if (carryOverPendingTasks) {
            val pendingTasks = dayTasks.filter { it.completionState == TaskCompletionState.PENDING.value }
            pendingTasks.forEach { task ->
                taskRepository.moveToDate(task, today)
                carriedCount++
            }
        }

        // 3. Delegate to closeDayUseCase so rewards and achievements are preserved
        closeDayUseCase(
            date = date,
            mood = mood,
            note = note,
            completionRate = realRate,
            energy = energy,
            bestMoment = bestMoment,
            challenge = challenge,
            tomorrowIntention = tomorrowIntention,
            carriedCount = carriedCount,
        )
    }
}
