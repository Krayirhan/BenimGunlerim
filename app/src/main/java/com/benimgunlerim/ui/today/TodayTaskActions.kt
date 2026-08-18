package com.benimgunlerim.ui.today

import com.benimgunlerim.R
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.model.TaskCompletionState
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Snapshot of a deleted task and its subtasks, kept in memory to power Undo. */
private data class DeletedTaskSnapshot(
    val task: TaskEntity,
    val subTasks: List<SubTaskEntity>,
)

internal data class TodayTaskDependencies(
    val dateTimeProvider: DateTimeProvider,
    val analyticsTracker: AnalyticsTracker,
    val feedbackManager: FeedbackManager,
    val achievementTracker: AchievementTracker,
    val rewardDisplayService: RewardDisplayService,
    val addTaskUseCase: AddTaskUseCase,
    val addTasksBatchUseCase: AddTasksBatchUseCase,
    val updateTaskTitleUseCase: UpdateTaskTitleUseCase,
    val moveTaskToDateUseCase: MoveTaskToDateUseCase,
    val updateTaskUseCase: UpdateTaskUseCase,
    val deleteTaskUseCase: DeleteTaskUseCase,
    val restoreTaskUseCase: RestoreTaskUseCase,
    val setTaskPendingUseCase: SetTaskPendingUseCase,
    val observeSubTasksUseCase: ObserveSubTasksUseCase,
    val addSubTaskUseCase: AddSubTaskUseCase,
    val toggleSubTaskUseCase: ToggleSubTaskUseCase,
    val deleteSubTaskUseCase: DeleteSubTaskUseCase,
    val toggleTaskUseCase: ToggleTaskUseCase,
)

/**
 * Task + subtask actions for [TodayViewModel], extracted so the ViewModel itself
 * stays focused on state assembly. Holds no StateFlow of its own — reads current
 * state via [uiStateValue] and [taskEntitiesById], emits one-shot effects via [emitEffect].
 */
internal class TodayTaskActions(
    private val scope: CoroutineScope,
    private val deps: TodayTaskDependencies,
    private val taskEntitiesById: () -> Map<String, TaskEntity>,
    private val uiStateValue: () -> TodayUiState,
    private val isTodayClosed: () -> Boolean,
    private val emitEffect: (TodayUiEffect) -> Unit,
) {
    private val deletedTasksById = mutableMapOf<String, DeletedTaskSnapshot>()
    private val inFlightTaskIds = mutableSetOf<String>()

    fun addTasksFromBrainDump(taskTitles: List<String>) {
        scope.launch {
            runCatching {
                deps.addTasksBatchUseCase(titles = taskTitles, date = deps.dateTimeProvider.today(), priority = 1)
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
                deps.addTaskUseCase(
                    title = cleanTitle,
                    date = date,
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                    startTime = startTime?.trim()?.takeIf { it.isNotBlank() },
                    category = category?.trim()?.takeIf { it.isNotBlank() },
                    priority = priority,
                    reminderTime = reminderTime?.trim()?.takeIf { it.isNotBlank() },
                )
            }.onSuccess {
                deps.analyticsTracker.track(AnalyticsEvent("task_created"))
            }.onFailure {
                emitEffect(TodayUiEffect.ActionFailed(R.string.today_error_generic))
            }
        }
    }

    fun addTask(draft: TaskEditDraft) {
        addTask(
            title = draft.title,
            note = draft.note,
            date = draft.date ?: deps.dateTimeProvider.today(),
            startTime = draft.startTime?.toString(),
            category = draft.category,
            priority = draft.priority,
            reminderTime = draft.reminderTime?.toString(),
        )
    }

    fun toggleTask(task: TaskEntity) {
        if (isTodayClosed()) return
        if (!claim(inFlightTaskIds, task.id)) return
        scope.launch {
            try {
                val wasPending = task.completionState != TaskCompletionState.COMPLETED.value
                val currentTasks = uiStateValue().tasks
                val completedCountBefore = currentTasks.count { it.isCompleted }
                val totalTasks = currentTasks.size

                deps.analyticsTracker.track(AnalyticsEvent("task_completed"))
                val result = deps.toggleTaskUseCase(task) ?: return@launch
                deps.rewardDisplayService.onTaskCompleted(
                    taskId = task.id,
                    taskReward = result.taskReward,
                    allTasksBonus = result.allTasksBonus,
                )
                if (wasPending) {
                    emitEffect(TodayUiEffect.TaskCompletedUndo(task.id))
                    deps.achievementTracker.checkFirstTask()

                    if (completedCountBefore == 0) {
                        deps.rewardDisplayService.emitMiniBanner(R.string.today_mini_banner_first_task, "🌱")
                    } else if (completedCountBefore + 1 == totalTasks && totalTasks > 1) {
                        deps.rewardDisplayService.emitAllTasksCompleted(totalTasks)
                        deps.achievementTracker.checkListCleared()
                    }
                }
            } finally {
                release(inFlightTaskIds, task.id)
            }
        }
    }

    fun toggleTask(taskId: String) {
        val task = taskEntitiesById()[taskId] ?: return
        toggleTask(task)
    }

    fun updateTaskTitle(task: TaskEntity, title: String) {
        if (isTodayClosed()) return
        scope.launch { deps.updateTaskTitleUseCase(task, title) }
    }

    fun updateTaskTitle(taskId: String, title: String) {
        val task = taskEntitiesById()[taskId] ?: return
        updateTaskTitle(task, title)
    }

    fun updateTask(
        task: TaskEntity,
        draft: TaskEditDraft,
    ) {
        updateTask(
            task = task,
            title = draft.title,
            note = draft.note,
            date = draft.date ?: deps.dateTimeProvider.today(),
            startTime = draft.startTime?.toString(),
            category = draft.category,
            priority = draft.priority,
            reminderTime = draft.reminderTime?.toString(),
        )
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
            deps.updateTaskUseCase(
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
            deps.moveTaskToDateUseCase(task, deps.dateTimeProvider.today().plusDays(1))
            emitEffect(TodayUiEffect.TaskMovedTomorrow("task_moved_tomorrow"))
        }
    }

    fun moveTaskToTomorrow(taskId: String) {
        val task = taskEntitiesById()[taskId] ?: return
        moveTaskToTomorrow(task)
    }

    fun moveTaskToDate(task: TaskEntity, date: LocalDate) {
        if (isTodayClosed()) return
        scope.launch { deps.moveTaskToDateUseCase(task, date) }
    }

    fun moveTaskToDate(taskId: String, date: LocalDate) {
        val task = taskEntitiesById()[taskId] ?: return
        moveTaskToDate(task, date)
    }

    fun deleteTask(task: TaskEntity) {
        if (isTodayClosed()) return
        scope.launch {
            runCatching { deps.deleteTaskUseCase(task) }
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
        scope.launch { deps.restoreTaskUseCase(task, subTasks) }
    }

    fun restoreDeletedTask(taskId: String) {
        val deleted = deletedTasksById.remove(taskId) ?: return
        restoreTask(deleted.task, deleted.subTasks)
    }

    fun undoTaskToggle(taskId: String) {
        if (isTodayClosed()) return
        scope.launch { deps.setTaskPendingUseCase(taskId) }
    }

    fun moveAllOverdueTo(date: LocalDate) {
        if (isTodayClosed()) return
        scope.launch {
            val ids = uiStateValue().overdueTasks.map { it.id }
            var moved = 0
            ids.forEach { id ->
                val entity = taskEntitiesById()[id] ?: return@forEach
                deps.moveTaskToDateUseCase(entity, date)
                moved++
            }
            if (moved > 0) {
                emitEffect(TodayUiEffect.OverdueTasksMoved(moved))
            }
        }
    }

    // ── SubTask actions ─────────────────────────────────────────────────────

    fun subTasksFlow(taskId: String): Flow<List<SubTaskEntity>> = deps.observeSubTasksUseCase(taskId)

    fun addSubTask(taskId: String, title: String) {
        if (isTodayClosed()) return
        val t = title.trim()
        if (t.isBlank()) return
        scope.launch { deps.addSubTaskUseCase(taskId, t) }
    }

    fun toggleSubTask(subTask: SubTaskEntity) {
        if (isTodayClosed()) return
        scope.launch { deps.toggleSubTaskUseCase(subTask) }
    }

    fun deleteSubTask(subTask: SubTaskEntity) {
        if (isTodayClosed()) return
        scope.launch { deps.deleteSubTaskUseCase(subTask) }
    }

    private fun claim(ids: MutableSet<String>, id: String): Boolean = synchronized(ids) { ids.add(id) }

    private fun release(ids: MutableSet<String>, id: String) {
        synchronized(ids) { ids.remove(id) }
    }
}
