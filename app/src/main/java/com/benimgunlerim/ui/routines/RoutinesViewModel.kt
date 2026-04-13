package com.benimgunlerim.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.currentStreakForEntity
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoutineListItem(
    val routine: RoutineEntity,
    val currentStreak: Int,
    val last7Days: List<Boolean> = emptyList(), // Mon..Sun: true if completed
)

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val repository: BenimGunlerimRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    val routines: StateFlow<List<RoutineListItem>> = combine(
        repository.observeActiveRoutines(),
        repository.observeAllCompletionLogs(),
    ) { routines, logs ->
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        routines.map { routine ->
            val routineLogs = logs.filter { it.entityType == "routine" && it.entityId == routine.id && it.status == "completed" }
            val last7 = (0..6).map { offset ->
                val date = weekStart.plusDays(offset.toLong()).toString()
                routineLogs.any { it.date == date }
            }
            RoutineListItem(
                routine = routine,
                currentStreak = logs.currentStreakForEntity("routine", routine.id),
                last7Days = last7,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRoutine(
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
        targetType: String = "check",
        targetValue: Int? = null,
        targetUnit: String? = null,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addRoutine(name, targetDays, preferredTime, targetType, targetValue, targetUnit)
            analyticsTracker.track(
                AnalyticsEvent(
                    name = "routine_created",
                    properties = mapOf("has_reminder" to (preferredTime != null).toString()),
                ),
            )
        }
    }

    fun updateRoutine(
        routine: RoutineEntity,
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
        targetType: String = routine.targetType,
        targetValue: Int? = routine.targetValue,
        targetUnit: String? = routine.targetUnit,
    ) {
        viewModelScope.launch { repository.updateRoutine(routine, name, targetDays, preferredTime, targetType, targetValue, targetUnit) }
    }

    fun archiveRoutine(routine: RoutineEntity) {
        viewModelScope.launch { repository.archiveRoutine(routine) }
    }

    fun skipRoutine(routine: RoutineEntity) {
        viewModelScope.launch { repository.skipRoutine(routine) }
    }
}
