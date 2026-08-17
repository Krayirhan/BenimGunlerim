package com.benimgunlerim.domain

import java.time.LocalDate

/**
 * Hafif Gün Modu is date-scoped: [UserPreferencesRepository.setLightDayMode] stores the
 * ISO date it was enabled for, and it's considered active only while "today" matches
 * that date — so it lapses automatically at midnight without any scheduled job.
 */
object LightDayMode {
    fun isActiveOn(lightDayModeDate: String, today: LocalDate): Boolean =
        lightDayModeDate == today.toString()
}
