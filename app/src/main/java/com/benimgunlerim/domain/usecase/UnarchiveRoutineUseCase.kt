package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.notifications.RoutineReminderSchedulerContract
import javax.inject.Inject

class UnarchiveRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val routineReminderScheduler: RoutineReminderSchedulerContract,
) {
    suspend operator fun invoke(routine: RoutineEntity) {
        val restored = routineRepository.unarchive(routine)
        routineReminderScheduler.schedule(restored)
    }
}
