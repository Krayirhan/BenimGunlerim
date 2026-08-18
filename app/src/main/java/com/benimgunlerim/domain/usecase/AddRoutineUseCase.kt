package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.notifications.RoutineReminderSchedulerContract
import java.time.DayOfWeek
import javax.inject.Inject

class AddRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val routineReminderScheduler: RoutineReminderSchedulerContract,
) {
    suspend operator fun invoke(
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
        targetType: String = RoutineTargetType.CHECK.value,
        targetValue: Int? = null,
        targetUnit: String? = null,
        category: String? = null,
    ) {
        val routine = routineRepository.add(name, targetDays, preferredTime, targetType, targetValue, targetUnit, category)
        routineReminderScheduler.schedule(routine)
    }
}
