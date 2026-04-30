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
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlanUiState(
    val selectedDate: LocalDate,
    val weekStart: LocalDate,
    val tasksForDay: List<PlanTaskUi> = emptyList(),
    val overdueTasks: List<PlanTaskUi> = emptyList(),
)

sealed class PlanUiEffect {
    data object TaskAdded : PlanUiEffect()
    data object TaskDeleted : PlanUiEffect()
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
) : ViewModel() {
    private val today = dateTimeProvider.today()
    private val _selectedDate = MutableStateFlow(today)
    private var latestTasksById: Map<String, TaskEntity> = emptyMap()
    private val _uiEffects = MutableSharedFlow<PlanUiEffect>(extraBufferCapacity = 16)
    val uiEffects = _uiEffects.asSharedFlow()

    val uiState: StateFlow<PlanUiState> = combine(
        _selectedDate,
        _selectedDate.flatMapLatest { date ->
            observePlanSnapshotUseCase(date, dateTimeProvider.today())
        },
    ) { date, snapshot ->
        latestTasksById = (snapshot.tasksForDay + snapshot.overdueTasks).associateBy { it.id }
        val weekStart = date.minusDays(date.dayOfWeek.value.toLong() - 1)
        PlanUiState(
            selectedDate = date,
            weekStart = weekStart,
            tasksForDay = snapshot.tasksForDay.map { it.toUiModel() },
            overdueTasks = snapshot.overdueTasks.map { it.toUiModel() },
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

    fun today(): LocalDate = dateTimeProvider.today()

    fun addTask(title: String, date: LocalDate) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        viewModelScope.launch {
            runCatching {
                addTaskUseCase(title = cleanTitle, date = date)
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

    fun deleteTask(taskId: String) {
        val task = latestTasksById[taskId] ?: return
        viewModelScope.launch {
            runCatching {
                deleteTaskUseCase(task)
            }.onSuccess {
                _uiEffects.tryEmit(PlanUiEffect.TaskDeleted)
            }.onFailure {
                _uiEffects.tryEmit(PlanUiEffect.ActionFailed)
            }
        }
    }

    private fun TaskEntity.toUiModel(): PlanTaskUi = PlanTaskUi(
        id = id,
        title = title,
        plannedDate = plannedDate,
        isCompleted = completionState == com.benimgunlerim.domain.model.TaskCompletionState.COMPLETED.value,
    )
}
