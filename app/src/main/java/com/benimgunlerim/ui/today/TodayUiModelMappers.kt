package com.benimgunlerim.ui.today

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.data.targetDaySet
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus

internal fun TaskEntity.toTodayUiModel(): TodayTaskUi = TodayTaskUi(
    id = id,
    title = title,
    note = note,
    plannedDate = plannedDate,
    startTime = startTime,
    category = category,
    color = color,
    priority = priority,
    completionState = completionState,
    reminderTime = reminderTime,
)

internal fun RoutineEntity.toTodayUiModel(
    currentStreak: Int,
    todayLogs: List<CompletionLogEntity> = emptyList(),
): TodayRoutineUi {
    val todayLog = todayLogs.firstOrNull {
        it.entityType == CompletionEntityType.ROUTINE.value && it.entityId == id
    }
    val isCompleted = todayLog?.status == CompletionStatus.COMPLETED.value
    val currentValue = todayLog?.value ?: if (isCompleted) (targetValue?.toFloat() ?: 1f) else 0f

    return TodayRoutineUi(
        id = id,
        name = name,
        preferredTime = preferredTime,
        color = color,
        targetType = targetType,
        targetValue = targetValue,
        targetUnit = targetUnit,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        targetDays = targetDaySet(),
        currentValue = currentValue,
    )
}
