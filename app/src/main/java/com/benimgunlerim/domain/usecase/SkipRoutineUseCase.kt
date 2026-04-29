package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.DateTimeProvider
import java.time.LocalDate
import javax.inject.Inject

class SkipRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val dateTimeProvider: DateTimeProvider,
) {
    suspend operator fun invoke(
        routine: RoutineEntity,
        date: LocalDate = dateTimeProvider.today(),
    ) {
        routineRepository.skip(routine, date)
    }
}
