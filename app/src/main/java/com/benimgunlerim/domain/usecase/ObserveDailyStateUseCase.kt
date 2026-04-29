package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DailyStateRepository
import java.time.LocalDate
import javax.inject.Inject

class ObserveDailyStateUseCase @Inject constructor(
    private val dailyStateRepository: DailyStateRepository,
) {
    operator fun invoke(date: LocalDate) = dailyStateRepository.observeByDate(date)
}
