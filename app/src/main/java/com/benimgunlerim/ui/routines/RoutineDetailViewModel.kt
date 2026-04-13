package com.benimgunlerim.ui.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.currentStreakForEntity
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoutineDetailUiState(
    val routine: RoutineEntity? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val successRate: Int = 0,
    val last7Days: List<Boolean> = emptyList(),
    val last30Logs: List<CompletionLogEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    private val repository: BenimGunlerimRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])

    val uiState: StateFlow<RoutineDetailUiState> = combine(
        repository.observeActiveRoutines(),
        repository.observeAllCompletionLogs(),
    ) { routines, allLogs ->
        val routine = routines.firstOrNull { it.id == routineId }
        if (routine == null) return@combine RoutineDetailUiState(isLoading = false)

        val routineLogs = allLogs.filter {
            it.entityType == "routine" && it.entityId == routineId
        }
        val completedLogs = routineLogs.filter { it.status == "completed" }

        // Last 7 days
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val last7 = (0..6).map { offset ->
            val date = weekStart.plusDays(offset.toLong()).toString()
            completedLogs.any { it.date == date }
        }

        // Last 30 days success rate (only on scheduled days)
        val last30 = (0..29).mapNotNull { offset ->
            val date = today.minusDays(offset.toLong())
            val dayLog = routineLogs.firstOrNull { it.date == date.toString() }
            dayLog
        }
        val scheduledIn30 = (0..29).count { offset ->
            val date = today.minusDays(offset.toLong())
            val dow = date.dayOfWeek.name
            routine.targetDays.split(",").contains(dow)
        }.coerceAtLeast(1)
        val completedIn30 = (0..29).count { offset ->
            val date = today.minusDays(offset.toLong()).toString()
            completedLogs.any { it.date == date }
        }
        val successRate = ((completedIn30.toFloat() / scheduledIn30) * 100).toInt().coerceIn(0, 100)

        // Best streak from all logs
        val bestStreak = calculateBestStreak(completedLogs)

        RoutineDetailUiState(
            routine = routine,
            currentStreak = allLogs.currentStreakForEntity("routine", routineId),
            bestStreak = bestStreak,
            successRate = successRate,
            last7Days = last7,
            last30Logs = last30,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoutineDetailUiState())

    fun archiveRoutine() {
        val routine = uiState.value.routine ?: return
        viewModelScope.launch { repository.archiveRoutine(routine) }
    }

    fun skipToday() {
        val routine = uiState.value.routine ?: return
        viewModelScope.launch { repository.skipRoutine(routine) }
    }

    private fun calculateBestStreak(logs: List<CompletionLogEntity>): Int {
        val dates = logs
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .toSortedSet()
        if (dates.isEmpty()) return 0
        var best = 1
        var current = 1
        var prev = dates.first()
        for (date in dates.drop(1)) {
            if (date == prev.plusDays(1)) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
            prev = date
        }
        return best
    }
}
