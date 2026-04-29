package com.benimgunlerim.domain.model

/**
 * Outcome recorded when a task or routine interaction is logged.
 */
enum class CompletionStatus(val value: String) {
    COMPLETED("completed"),
    PARTIAL("partial"),
    SKIPPED("skipped"),
    ;

    companion object {
        fun fromString(value: String): CompletionStatus =
            entries.firstOrNull { it.value == value } ?: COMPLETED
    }
}
