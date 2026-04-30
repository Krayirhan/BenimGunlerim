package com.benimgunlerim.domain.validation

/**
 * Validates optional HH:mm time strings used for task start/reminder fields.
 */
object TimeInputValidator {

    fun sanitize(raw: String): String =
        raw.filter { it.isDigit() || it == ':' }.take(5)

    /** Blank is valid (no time set). Otherwise must be exactly HH:mm with valid clock values. */
    fun isValid(hhmm: String): Boolean {
        if (hhmm.isBlank()) return true
        if (!HH_MM_REGEX.matches(hhmm)) return false
        val parts = hhmm.split(':')
        val h = parts[0].toIntOrNull() ?: return false
        val m = parts[1].toIntOrNull() ?: return false
        return h in 0..23 && m in 0..59
    }

    fun validationMessageKey(hhmm: String): TimeValidation {
        when {
            hhmm.isBlank() -> return TimeValidation.Ok
            hhmm.length < 5 -> return TimeValidation.Incomplete
            !HH_MM_REGEX.matches(hhmm) -> return TimeValidation.InvalidFormat
        }
        val parts = hhmm.split(':')
        val h = parts[0].toIntOrNull() ?: return TimeValidation.InvalidClock
        val m = parts[1].toIntOrNull() ?: return TimeValidation.InvalidClock
        return if (h in 0..23 && m in 0..59) TimeValidation.Ok else TimeValidation.InvalidClock
    }

    private val HH_MM_REGEX = Regex("^\\d{2}:\\d{2}$")

    enum class TimeValidation {
        Ok,
        Incomplete,
        InvalidFormat,
        InvalidClock,
    }
}
