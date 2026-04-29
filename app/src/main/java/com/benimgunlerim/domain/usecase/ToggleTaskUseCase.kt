package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.domain.model.TaskCompletionState
import com.benimgunlerim.domain.service.GrantResult
import com.benimgunlerim.domain.service.RewardGrantService
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Toggles a task's completion state and grants the appropriate reward.
 *
 * Returns a [Result] if the task was just **completed** and a reward was granted;
 * returns null when the task was un-completed or the reward was already granted before.
 */
class ToggleTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
    private val rewardGrantService: RewardGrantService,
    private val taskReminderScheduler: TaskReminderSchedulerContract,
    private val dateTimeProvider: DateTimeProvider,
    private val transactionRunner: DatabaseTransactionRunner,
) {
    data class Result(
        val taskReward: GrantResult,
        val allTasksBonus: GrantResult,
    )

    suspend operator fun invoke(task: TaskEntity): Result? {
        val wasCompleted = task.completionState == TaskCompletionState.COMPLETED.value
        val prefs = prefsRepository.preferences.first()

        if (wasCompleted) {
            // Un-completing: reset state, cancel log, reschedule reminder
            transactionRunner.runInTransaction {
                taskRepository.setCompletionState(task, TaskCompletionState.PENDING)
                taskRepository.deleteCompletionLog(task)
            }
            if (task.reminderTime != null) {
                runCatching {
                    val date = java.time.LocalDate.parse(task.plannedDate)
                    val time = java.time.LocalTime.parse(task.reminderTime)
                    taskReminderScheduler.schedule(task.id, task.title, date, time)
                }
            }
            return null
        }

        // Completing: set state, write log, cancel reminder (state+log are atomic)
        transactionRunner.runInTransaction {
            taskRepository.setCompletionState(task, TaskCompletionState.COMPLETED)
            taskRepository.writeCompletionLog(task)
        }
        taskReminderScheduler.cancel(task.id)

        val today = dateTimeProvider.today()
        val taskXp = GameEngine.xpForTask(task.priority)
        val taskReward = rewardGrantService.grantOnce(
            eventKey = "task:${task.id}:${task.plannedDate}",
            xp = taskXp,
            gold = GameEngine.GOLD_TASK_COMPLETE,
            happinessDelta = GameEngine.HAPPINESS_TASK,
            currentXp = prefs.totalXp,
        )

        if (!taskReward.alreadyGranted) {
            prefsRepository.incrementTasksCompleted()
            achievementTracker.checkTaskCount(prefs.totalTasksCompleted + 1)
            achievementTracker.checkGold(prefs.gold + GameEngine.GOLD_TASK_COMPLETE)
            achievementTracker.checkHappiness(prefs.happiness + GameEngine.HAPPINESS_TASK)
        }

        // Check all-tasks bonus
        val todayTasks = taskRepository.observeByDate(today).first()
        val completedIds = todayTasks.filter {
            it.completionState == TaskCompletionState.COMPLETED.value
        }.map { it.id }.toSet()

        val allTasksBonus = rewardGrantService.grantAllTasksBonusIfEligible(
            taskIds = todayTasks.map { it.id },
            completedIds = completedIds,
            justToggledId = task.id,
            currentXp = prefs.totalXp + taskReward.xpGranted,
        )

        return Result(taskReward = taskReward, allTasksBonus = allTasksBonus)
    }
}
