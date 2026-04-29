package com.benimgunlerim.domain.model

/**
 * How a routine's completion is measured.
 */
enum class RoutineTargetType(val value: String) {
    /** Simple checkbox — done or not done. */
    CHECK("check"),

    /** Numeric goal — e.g. "drink 8 glasses of water". */
    GOAL("goal"),
    ;

    companion object {
        fun fromString(value: String): RoutineTargetType =
            entries.firstOrNull { it.value == value } ?: CHECK
    }
}
