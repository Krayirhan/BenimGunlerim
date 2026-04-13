@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.local.entity.TaskEntity
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
    val selectedDate: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1),
    val tasksForDay: List<TaskEntity> = emptyList(),
    val overdueTasks: List<TaskEntity> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: BenimGunlerimRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<PlanUiState> = combine(
        _selectedDate,
        _selectedDate.flatMapLatest { date ->
            repository.observeTasksForRange(date, date)
        },
        repository.observeOverdueTasks(),
    ) { date, dayTasks, overdue ->
        val weekStart = date.minusDays(date.dayOfWeek.value.toLong() - 1)
        PlanUiState(
            selectedDate = date,
            weekStart = weekStart,
            tasksForDay = dayTasks,
            overdueTasks = overdue,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanUiState())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addTask(title: String, date: LocalDate) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTask(
                title = title,
                note = null,
                date = date,
                startTime = null,
                category = null,
                priority = 2,
                reminderTime = null,
            )
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch { repository.toggleTask(task) }
    }

    fun moveTaskToDate(task: TaskEntity, date: LocalDate) {
        viewModelScope.launch { repository.moveTaskToDate(task, date) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
}
