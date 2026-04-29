package com.benimgunlerim.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.domain.usecase.AddRoutineUseCase
import com.benimgunlerim.domain.usecase.ArchiveRoutineUseCase
import com.benimgunlerim.domain.usecase.SkipRoutineUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineUseCase
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
@Suppress("LongParameterList")
class RoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val dateTimeProvider: DateTimeProvider,
    private val addRoutineUseCase: AddRoutineUseCase,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val archiveRoutineUseCase: ArchiveRoutineUseCase,
    private val skipRoutineUseCase: SkipRoutineUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    val routines: StateFlow<List<RoutineListItem>> = combine(
        routineRepository.observeActive(),
        completionLogRepository.observeAll(),
    ) { routines, logs ->
        val today = dateTimeProvider.today()
        val weekStart = today.minusDays(6)
        routines.map { routine ->
            val routineLogs = logs.filter {
                it.entityType == CompletionEntityType.ROUTINE.value &&
                    it.entityId == routine.id &&
                    it.status == CompletionStatus.COMPLETED.value
            }
            val last7 = (0..6).map { offset ->
                val date = weekStart.plusDays(offset.toLong()).toString()
                routineLogs.any { it.date == date }
            }
            RoutineListItem(
                routine = routine,
                currentStreak = currentStreakForEntity(logs, CompletionEntityType.ROUTINE.value, routine.id, today),
                last7Days = last7,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRoutine(
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
        targetType: String = RoutineTargetType.CHECK.value,
        targetValue: Int? = null,
        targetUnit: String? = null,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            addRoutineUseCase(name, targetDays, preferredTime, targetType, targetValue, targetUnit)
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
        viewModelScope.launch { updateRoutineUseCase(routine, name, targetDays, preferredTime, targetType, targetValue, targetUnit) }
    }

    fun archiveRoutine(routine: RoutineEntity) {
        viewModelScope.launch { archiveRoutineUseCase(routine) }
    }

    fun skipRoutine(routine: RoutineEntity) {
        viewModelScope.launch { skipRoutineUseCase(routine, dateTimeProvider.today()) }
    }

    private fun currentStreakForEntity(
        logs: List<CompletionLogEntity>,
        entityType: String,
        entityId: String,
        today: LocalDate,
    ): Int {
        val completedDates = logs
            .asSequence()
            .filter { it.entityType == entityType && it.entityId == entityId }
            .filter { it.status == CompletionStatus.COMPLETED.value }
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .toSet()
        var streak = 0
        var cursor = today
        while (cursor in completedDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
