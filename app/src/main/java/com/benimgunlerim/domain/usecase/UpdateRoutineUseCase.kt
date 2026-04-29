package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.notifications.RoutineReminderSchedulerContract
import java.time.DayOfWeek
import javax.inject.Inject

class UpdateRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val routineReminderScheduler: RoutineReminderSchedulerContract,
) {
    suspend operator fun invoke(
        routine: RoutineEntity,
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
        targetType: String = routine.targetType,
        targetValue: Int? = routine.targetValue,
        targetUnit: String? = routine.targetUnit,
    ) {
        val updated = routineRepository.update(routine, name, targetDays, preferredTime, targetType, targetValue, targetUnit)
        routineReminderScheduler.cancel(routine)
        routineReminderScheduler.schedule(updated)
    }
}
