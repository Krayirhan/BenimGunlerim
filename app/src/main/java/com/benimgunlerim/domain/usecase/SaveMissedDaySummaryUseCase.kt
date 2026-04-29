package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DailyStateRepository
import java.time.LocalDate
import javax.inject.Inject

class SaveMissedDaySummaryUseCase @Inject constructor(
    private val dailyStateRepository: DailyStateRepository,
) {
    private companion object {
        const val MIN_ENERGY = 1
        const val MAX_ENERGY = 5
    }

    suspend operator fun invoke(
        date: LocalDate,
        note: String,
        mood: Int,
        energy: Int,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
    ) {
        val moodLabels = listOf("cok_kotu", "kotu", "normal", "iyi", "harika")
        dailyStateRepository.saveSummary(
            date = date,
            mood = moodLabels.getOrElse(mood) { "normal" },
            note = note,
            completionRate = 0f,
            energyLevel = energy.coerceIn(MIN_ENERGY, MAX_ENERGY),
            bestMoment = bestMoment.ifBlank { null },
            challenge = challenge.ifBlank { null },
            tomorrowIntention = tomorrowIntention.ifBlank { null },
        )
    }
}
