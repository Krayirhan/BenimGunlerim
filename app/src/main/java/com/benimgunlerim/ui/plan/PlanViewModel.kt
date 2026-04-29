@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.DeleteTaskUseCase
import com.benimgunlerim.domain.usecase.MoveTaskToDateUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlanUiState(
    val selectedDate: LocalDate,
    val weekStart: LocalDate,
    val tasksForDay: List<TaskEntity> = emptyList(),
    val overdueTasks: List<TaskEntity> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val dateTimeProvider: DateTimeProvider,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val moveTaskToDateUseCase: MoveTaskToDateUseCase,
) : ViewModel() {
    private val today = dateTimeProvider.today()
    private val _selectedDate = MutableStateFlow(today)

    val uiState: StateFlow<PlanUiState> = combine(
        _selectedDate,
        _selectedDate.flatMapLatest { date ->
            taskRepository.observeRange(date, date)
        },
        taskRepository.observeOverdue(today),
    ) { date, dayTasks, overdue ->
        val weekStart = date.minusDays(date.dayOfWeek.value.toLong() - 1)
        PlanUiState(
            selectedDate = date,
            weekStart = weekStart,
            tasksForDay = dayTasks,
            overdueTasks = overdue,
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

    fun addTask(title: String, date: LocalDate) {
        if (title.isBlank()) return
        viewModelScope.launch {
            addTaskUseCase(title = title, date = date)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch { toggleTaskUseCase(task) }
    }

    fun moveTaskToDate(task: TaskEntity, date: LocalDate) {
        viewModelScope.launch { moveTaskToDateUseCase(task, date) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { deleteTaskUseCase(task) }
    }
}
