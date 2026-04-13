package com.benimgunlerim.analytics

object ErrorReporterContextPolicy {
    const val MAX_VALUE_LENGTH = 1024

    private val allowedKeys = setOf(
        "action",
        "app_version",
        "build_type",
        "db_version",
        "notification_permission",
        "screen",
        "theme",
        "thread",
        "type",
    )

    fun sanitize(context: Map<String, String>): Map<String, String> =
        context.asSequence()
            .filter { (key, _) -> key in allowedKeys }
            .associate { (key, value) -> key to value.take(MAX_VALUE_LENGTH) }

    fun isAllowedKey(key: String): Boolean = key in allowedKeys
}