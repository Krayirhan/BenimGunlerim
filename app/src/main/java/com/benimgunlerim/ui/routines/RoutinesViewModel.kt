package com.benimgunlerim.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.currentStreakForEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.targetDaySet
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
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
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
    fun todayDayOfWeek(): DayOfWeek = dateTimeProvider.today().dayOfWeek

    private val routineEntitiesById = MutableStateFlow<Map<String, RoutineEntity>>(emptyMap())

    val routines: StateFlow<List<RoutineCardUi>> = combine(
        routineRepository.observeActive(),
        completionLogRepository.observeAll(),
    ) { routines, logs ->
        routineEntitiesById.value = routines.associateBy { it.id }
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
            RoutineCardUi(
                id = routine.id,
                name = routine.name,
                targetDays = routine.targetDaySet(),
                preferredTime = routine.preferredTime,
                targetType = routine.targetType,
                targetValue = routine.targetValue,
                targetUnit = routine.targetUnit,
                currentStreak = logs.currentStreakForEntity(
                    entityType = CompletionEntityType.ROUTINE.value,
                    entityId = routine.id,
                    today = today,
                ),
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
        routineId: String,
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
    ) {
        val routine = routineEntitiesById.value[routineId] ?: return
        viewModelScope.launch {
            updateRoutineUseCase(
                routine = routine,
                name = name,
                targetDays = targetDays,
                preferredTime = preferredTime,
                targetType = routine.targetType,
                targetValue = routine.targetValue,
                targetUnit = routine.targetUnit,
            )
        }
    }

    fun archiveRoutine(routineId: String) {
        val routine = routineEntitiesById.value[routineId] ?: return
        viewModelScope.launch { archiveRoutineUseCase(routine) }
    }

    fun skipRoutine(routineId: String) {
        val routine = routineEntitiesById.value[routineId] ?: return
        viewModelScope.launch { skipRoutineUseCase(routine, dateTimeProvider.today()) }
    }
}
