package com.benimgunlerim.ui.today

import com.benimgunlerim.R
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.service.RewardDisplayService
import com.benimgunlerim.domain.usecase.AddSubTaskUseCase
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.AddTasksBatchUseCase
import com.benimgunlerim.domain.usecase.DeleteSubTaskUseCase
import com.benimgunlerim.domain.usecase.DeleteTaskUseCase
import com.benimgunlerim.domain.usecase.MoveTaskToDateUseCase
import com.benimgunlerim.domain.usecase.ObserveSubTasksUseCase
import com.benimgunlerim.domain.usecase.RestoreTaskUseCase
import com.benimgunlerim.domain.usecase.SetTaskPendingUseCase
import com.benimgunlerim.domain.usecase.ToggleSubTaskUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskTitleUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskUseCase
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Snapshot of a deleted task and its subtasks, kept in memory to power Undo. */
private data class DeletedTaskSnapshot(
    val task: TaskEntity,
    val subTasks: List<SubTaskEntity>,
)

/**
 * Task + subtask actions for [TodayViewModel], extracted so the ViewModel itself
 * stays focused on state assembly. Holds no StateFlow of its own — reads current
 * state via [uiStateValue] and [taskEntitiesById], emits one-shot effects via [emitEffect].
 */
@Suppress("LongParameterList")
internal class TodayTaskActions(
    private val scope: CoroutineScope,
    private val dateTimeProvider: DateTimeProvider,
    private val analyticsTracker: AnalyticsTracker,
    private val feedbackManager: FeedbackManager,
    private val achievementTracker: AchievementTracker,
    private val rewardDisplayService: RewardDisplayService,
    private val addTaskUseCase: AddTaskUseCase,
    private val addTasksBatchUseCase: AddTasksBatchUseCase,
    private val updateTaskTitleUseCase: UpdateTaskTitleUseCase,
    private val moveTaskToDateUseCase: MoveTaskToDateUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase,
    private val setTaskPendingUseCase: SetTaskPendingUseCase,
    private val observeSubTasksUseCase: ObserveSubTasksUseCase,
    private val addSubTaskUseCase: AddSubTaskUseCase,
    private val toggleSubTaskUseCase: ToggleSubTaskUseCase,
    private val deleteSubTaskUseCase: DeleteSubTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val taskEntitiesById: () -> Map<String, TaskEntity>,
    private val uiStateValue: () -> TodayUiState,
    private val isTodayClosed: () -> Boolean,
    private val emitEffect: (TodayUiEffect) -> Unit,
) {
    private val deletedTasksById = mutableMapOf<String, DeletedTaskSnapshot>()

    fun addTasksFromBrainDump(taskTitles: List<String>) {
        scope.launch {
            runCatching {
                addTasksBatchUseCase(titles = taskTitles, date = dateTimeProvider.today(), priority = 1)
            }.onFailure {
                emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_generic))
            }
        }
    }

    fun addTask(
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) {
        if (isTodayClosed()) return
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        scope.launch {
            runCatching {
                addTaskUseCase(
                    title = cleanTitle,
                    date = date,
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                    startTime = startTime?.trim()?.takeIf { it.isNotBlank() },
                    category = category?.trim()?.takeIf { it.isNotBlank() },
                    priority = priority,
                    reminderTime = reminderTime?.trim()?.takeIf { it.isNotBlank() },
                )
            }.onSuccess {
                analyticsTracker.track(AnalyticsEvent("task_created"))
            }.onFailure {
                emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_generic))
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        if (isTodayClosed()) return
        scope.launch {
            val wasPending = task.completionState != com.benimgunlerim.domain.model.TaskCompletionState.COMPLETED.value
            val currentTasks = uiStateValue().tasks
            val completedCountBefore = currentTasks.count { it.isCompleted }
            val totalTasks = currentTasks.size

            analyticsTracker.track(AnalyticsEvent("task_completed"))
            feedbackManager.tapMedium()
            val result = toggleTaskUseCase(task) ?: return@launch
            rewardDisplayService.onTaskCompleted(
                taskId = task.id,
                taskReward = result.taskReward,
                allTasksBonus = result.allTasksBonus,
            )
            if (wasPending) {
                emitEffect(TodayUiEffect.TaskCompletedUndo(task.id))
                achievementTracker.checkFirstTask()

                if (completedCountBefore == 0) {
                    rewardDisplayService.emitMiniBanner("İlk adım tamamlandı. Devamı daha kolay.", "🌱")
                } else if (completedCountBefore + 1 == totalTasks && totalTasks > 1) {
                    rewardDisplayService.emitAllTasksCompleted(totalTasks)
                    achievementTracker.checkListCleared()
                }
            }
        }
    }

    fun toggleTask(taskId: String) {
        val task = taskEntitiesById()[taskId] ?: return
        toggleTask(task)
    }

    fun updateTaskTitle(task: TaskEntity, title: String) {
        if (isTodayClosed()) return
        scope.launch { updateTaskTitleUseCase(task, title) }
    }

    fun updateTaskTitle(taskId: String, title: String) {
        val task = taskEntitiesById()[taskId] ?: return
        updateTaskTitle(task, title)
    }

    @Suppress("LongParameterList")
    fun updateTask(
        task: TaskEntity,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) {
        if (isTodayClosed()) return
        scope.launch {
            updateTaskUseCase(
                task,
                title.trim(),
                note?.trim()?.takeIf { it.isNotBlank() },
                date,
                startTime?.trim()?.takeIf { it.isNotBlank() },
                category?.trim()?.takeIf { it.isNotBlank() },
                priority,
                reminderTime?.trim()?.takeIf { it.isNotBlank() },
            )
        }
    }

    @Suppress("LongParameterList")
    fun updateTask(
        taskId: String,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) {
        val task = taskEntitiesById()[taskId] ?: return
        updateTask(task, title, note, date, startTime, category, priority, reminderTime)
    }

    fun moveTaskToTomorrow(task: TaskEntity) {
        if (isTodayClosed()) return
        scope.launch {
            moveTaskToDateUseCase(task, dateTimeProvider.today().plusDays(1))
            emitEffect(TodayUiEffect.TaskMovedTomorrow("task_moved_tomorrow"))
        }
    }

    fun moveTaskToTomorrow(taskId: String) {
        val task = taskEntitiesById()[taskId] ?: return
        moveTaskToTomorrow(task)
    }

    fun moveTaskToDate(task: TaskEntity, date: LocalDate) {
        if (isTodayClosed()) return
        scope.launch { moveTaskToDateUseCase(task, date) }
    }

    fun moveTaskToDate(taskId: String, date: LocalDate) {
        val task = taskEntitiesById()[taskId] ?: return
        moveTaskToDate(task, date)
    }

    fun deleteTask(task: TaskEntity) {
        if (isTodayClosed()) return
        scope.launch {
            runCatching { deleteTaskUseCase(task) }
                .onSuccess { subTasks ->
                    deletedTasksById[task.id] = DeletedTaskSnapshot(task, subTasks)
                    emitEffect(TodayUiEffect.TaskDeleted(task.id))
                }
                .onFailure {
                    emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_delete_task))
                }
        }
    }

    fun deleteTask(taskId: String) {
        val task = taskEntitiesById()[taskId] ?: return
        deleteTask(task)
    }

    fun restoreTask(task: TaskEntity, subTasks: List<SubTaskEntity> = emptyList()) {
        if (isTodayClosed()) return
        scope.launch { restoreTaskUseCase(task, subTasks) }
    }

    fun restoreDeletedTask(taskId: String) {
        val deleted = deletedTasksById.remove(taskId) ?: return
        restoreTask(deleted.task, deleted.subTasks)
    }

    fun undoTaskToggle(taskId: String) {
        if (isTodayClosed()) return
        scope.launch { setTaskPendingUseCase(taskId) }
    }

    fun moveAllOverdueTo(date: LocalDate) {
        if (isTodayClosed()) return
        scope.launch {
            val ids = uiStateValue().overdueTasks.map { it.id }
            var moved = 0
            ids.forEach { id ->
                val entity = taskEntitiesById()[id] ?: return@forEach
                moveTaskToDateUseCase(entity, date)
                moved++
            }
            if (moved > 0) {
                emitEffect(TodayUiEffect.OverdueTasksMoved(moved))
            }
        }
    }

    // ── SubTask actions ─────────────────────────────────────────────────────

    fun subTasksFlow(taskId: String) = observeSubTasksUseCase(taskId)

    fun addSubTask(taskId: String, title: String) {
        if (isTodayClosed()) return
        val t = title.trim()
        if (t.isBlank()) return
        scope.launch { addSubTaskUseCase(taskId, t) }
    }

    fun toggleSubTask(subTask: SubTaskEntity) {
        if (isTodayClosed()) return
        scope.launch { toggleSubTaskUseCase(subTask) }
    }

    fun deleteSubTask(subTask: SubTaskEntity) {
        if (isTodayClosed()) return
        scope.launch { deleteSubTaskUseCase(subTask) }
    }
}
