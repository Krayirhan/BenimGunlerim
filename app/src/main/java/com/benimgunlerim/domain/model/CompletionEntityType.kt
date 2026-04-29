package com.benimgunlerim.domain.model

/**
 * The type of entity recorded in a completion log row.
 */
enum class CompletionEntityType(val value: String) {
    TASK("task"),
    ROUTINE("routine"),
    ;

    companion object {
        fun fromString(value: String): CompletionEntityType =
            entries.firstOrNull { it.value == value } ?: TASK
    }
}
