package com.benimgunlerim.domain.model

/**
 * Compile-time-safe representation of a task's completion state.
 * Stored in the database as the string value via [TaskCompletionState.value].
 */
enum class TaskCompletionState(val value: String) {
    PENDING("pending"),
    COMPLETED("completed"),
    SKIPPED("skipped"),
    ;

    companion object {
        fun fromString(value: String): TaskCompletionState =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}
