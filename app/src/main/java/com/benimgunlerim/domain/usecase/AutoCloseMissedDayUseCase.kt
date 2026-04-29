package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DailyStateRepository
import java.time.LocalDate
import javax.inject.Inject

class AutoCloseMissedDayUseCase @Inject constructor(
    private val dailyStateRepository: DailyStateRepository,
) {
    suspend operator fun invoke(date: LocalDate) {
        dailyStateRepository.autoCloseIfMissed(date)
    }
}
