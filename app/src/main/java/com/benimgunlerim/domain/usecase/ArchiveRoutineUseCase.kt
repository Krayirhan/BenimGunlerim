package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.notifications.RoutineReminderSchedulerContract
import javax.inject.Inject

class ArchiveRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val routineReminderScheduler: RoutineReminderSchedulerContract,
) {
    suspend operator fun invoke(routine: RoutineEntity) {
        val archived = routineRepository.archive(routine)
        routineReminderScheduler.cancel(archived)
    }
}
