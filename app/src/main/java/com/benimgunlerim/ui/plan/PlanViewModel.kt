@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.DeleteTaskUseCase
import com.benimgunlerim.domain.usecase.MoveTaskToDateUseCase
import com.benimgunlerim.domain.usecase.ObservePlanSnapshotUseCase
import com.benimgunlerim.domain.usecase.RestoreTaskUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskUseCase
import com.benimgunlerim.domain.validation.TimeInputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlanUiState(
    val selectedDate: LocalDate,
    val weekStart: LocalDate,
    val tasksForDay: List<PlanTaskUi> = emptyList(),
    val overdueTasks: List<PlanTaskUi> = emptyList(),
    val weeklyTaskCounts: Map<LocalDate, Int> = emptyMap(),
    val snapshotLoadError: Boolean = false,
)

sealed class PlanUiEffect {
    data object TaskAdded : PlanUiEffect()
    data class TaskDeleted(val taskId: String) : PlanUiEffect()
    data class OverdueTasksMoved(val count: Int) : PlanUiEffect()
    data object ActionFailed : PlanUiEffect()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val dateTimeProvider: DateTimeProvider,
    private val observePlanSnapshotUseCase: ObservePlanSnapshotUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val moveTaskToDateUseCase: MoveTaskToDateUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase,
) : ViewModel() {
    private val today = dateTimeProvider.today()
    private val _selectedDate = MutableStateFlow(today)
    private var latestTasksById: Map<String, TaskEntity> = emptyMap()
    private var latestOverdueTasks: List<TaskEntity> = emptyList()
    private val deletedTasksById = mutableMapOf<String, TaskEntity>()
    private val _uiEffects = MutableSharedFlow<PlanUiEffect>(extraBufferCapacity = 16)
    private val retrySnapshotTrigger = MutableStateFlow(0)
    val uiEffects = _uiEffects.asSharedFlow()

    val uiState: StateFlow<PlanUiState> = combine(_selectedDate, retrySnapshotTrigger) { date, _ -> date }
        .flatMapLatest { date ->
            observePlanSnapshotUseCase(date, dateTimeProvider.today())
                .map { snapshot -> PlanSnapshotResult(date, snapshot, snapshotLoadError = false) }
                .catch {
                    emit(
                        PlanSnapshotResult(
                            selectedDate = date,
                            snapshot = com.benimgunlerim.domain.usecase.PlanSnapshot(
                                tasksForDay = emptyList(),
                                overdueTasks = emptyList(),
                            ),
                            snapshotLoadError = true,
                        ),
                    )
                }
        }
        .map { result ->
            val date = result.selectedDate
            val snapshot = result.snapshot
        latestTasksById = (snapshot.tasksForDay + snapshot.overdueTasks).associateBy { it.id }
        latestOverdueTasks = snapshot.overdueTasks
        val weekStart = date.minusDays(date.dayOfWeek.value.toLong() - 1)
        PlanUiState(
            selectedDate = date,
            weekStart = weekStart,
            tasksForDay = snapshot.tasksForDay.map { it.toUiModel() },
            overdueTasks = snapshot.overdueTasks.map { it.toUiModel() },
            weeklyTaskCounts = snapshot.weekTasks.toTaskCountsByDate(),
                snapshotLoadError = result.snapshotLoadError,
        )
        }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PlanUiState(
            selectedDate = today,
            weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1),
        ),
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectPreviousWeek() {
        _selectedDate.value = _selectedDate.value.minusWeeks(1)
    }

    fun selectNextWeek() {
        _selectedDate.value = _selectedDate.value.plusWeeks(1)
    }

    fun selectToday() {
        _selectedDate.value = dateTimeProvider.today()
    }

    fun today(): LocalDate = dateTimeProvider.today()

    fun retrySnapshotLoad() {
        retrySnapshotTrigger.value++
    }

    fun addTask(
        title: String,
        date: LocalDate,
        note: String? = null,
        startTime: String? = null,
        category: String? = null,
        priority: Int = 2,
        reminderTime: String? = null,
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        if (!TimeInputValidator.isValid(startTime.orEmpty()) || !TimeInputValidator.isValid(reminderTime.orEmpty())) {
            _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            return
        }
        viewModelScope.launch {
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
                _uiEffects.tryEmit(PlanUiEffect.TaskAdded)
            }.onFailure {
                _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            }
        }
    }

    fun toggleTask(taskId: String) {
        val task = latestTasksById[taskId] ?: return
        viewModelScope.launch { toggleTaskUseCase(task) }
    }

    fun moveTaskToDate(taskId: String, date: LocalDate) {
        val task = latestTasksById[taskId] ?: return
        viewModelScope.launch { moveTaskToDateUseCase(task, date) }
    }

    fun moveOverdueTasksToDate(date: LocalDate) {
        val tasks = latestOverdueTasks
        if (tasks.isEmpty()) return
        viewModelScope.launch {
            var movedCount = 0
            tasks.forEach { task ->
                runCatching {
                    moveTaskToDateUseCase(task, date)
                }.onSuccess {
                    movedCount++
                }
            }
            if (movedCount > 0) {
                _uiEffects.tryEmit(PlanUiEffect.OverdueTasksMoved(movedCount))
            } else {
                _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            }
        }
    }

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
        val task = latestTasksById[taskId] ?: return
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        if (!TimeInputValidator.isValid(startTime.orEmpty()) || !TimeInputValidator.isValid(reminderTime.orEmpty())) {
            _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            return
        }
        viewModelScope.launch {
            runCatching {
                updateTaskUseCase(
                    task,
                    cleanTitle,
                    note?.trim()?.takeIf { it.isNotBlank() },
                    date,
                    startTime?.trim()?.takeIf { it.isNotBlank() },
                    category?.trim()?.takeIf { it.isNotBlank() },
                    priority,
                    reminderTime?.trim()?.takeIf { it.isNotBlank() },
                )
            }.onFailure {
                _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            }
        }
    }

    fun deleteTask(taskId: String) {
        val task = latestTasksById[taskId] ?: return
        viewModelScope.launch {
            runCatching {
                deleteTaskUseCase(task)
            }.onSuccess {
                deletedTasksById[task.id] = task
                _uiEffects.tryEmit(PlanUiEffect.TaskDeleted(task.id))
            }.onFailure {
                _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            }
        }
    }

    fun restoreDeletedTask(taskId: String) {
        val task = deletedTasksById.remove(taskId) ?: return
        viewModelScope.launch {
            runCatching {
                restoreTaskUseCase(task)
            }.onFailure {
                _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            }
        }
    }

    private fun TaskEntity.toUiModel(): PlanTaskUi = PlanTaskUi(
        id = id,
        title = title,
        note = note,
        plannedDate = plannedDate,
        startTime = startTime,
        category = category,
        priority = priority,
        reminderTime = reminderTime,
        isCompleted = completionState == com.benimgunlerim.domain.model.TaskCompletionState.COMPLETED.value,
    )

    private fun List<TaskEntity>.toTaskCountsByDate(): Map<LocalDate, Int> = mapNotNull { task ->
        runCatching { LocalDate.parse(task.plannedDate) }.getOrNull()
    }.groupingBy { it }.eachCount()
}

private data class PlanSnapshotResult(
    val selectedDate: LocalDate,
    val snapshot: com.benimgunlerim.domain.usecase.PlanSnapshot,
    val snapshotLoadError: Boolean,
)
