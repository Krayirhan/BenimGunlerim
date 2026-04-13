package com.benimgunlerim.notifications

object NotificationConstants {
    const val ROUTINE_CHANNEL_ID = "routine_reminders"
    const val ACTION_ROUTINE_REMINDER = "com.benimgunlerim.ACTION_ROUTINE_REMINDER"
    const val EXTRA_ROUTINE_ID = "routine_id"
    const val EXTRA_ROUTINE_NAME = "routine_name"
    const val EXTRA_TARGET_DAYS = "target_days"
    const val EXTRA_START_ROUTE = "start_route"
    const val ACTION_DAILY_SUMMARY = "com.benimgunlerim.ACTION_DAILY_SUMMARY"

    // Sprint 8: Task Reminders
    const val TASK_CHANNEL_ID = "task_reminders"
    const val ACTION_TASK_REMINDER = "com.benimgunlerim.ACTION_TASK_REMINDER"
    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_TITLE = "task_title"

    // Sprint 8: Morning Planner
    const val MORNING_CHANNEL_ID = "morning_planner"
    const val ACTION_MORNING_PLANNER = "com.benimgunlerim.ACTION_MORNING_PLANNER"
    const val MORNING_PLANNER_REQUEST_CODE = 8_000

    // Sprint 8: Snooze
    const val ACTION_SNOOZE = "com.benimgunlerim.ACTION_SNOOZE"
    const val EXTRA_SNOOZE_TYPE = "snooze_type"
    const val EXTRA_SNOOZE_ID = "snooze_id"
    const val EXTRA_SNOOZE_TITLE = "snooze_title"
    const val SNOOZE_DELAY_MINUTES = 10L
    const val SNOOZE_TYPE_ROUTINE = "routine"
    const val SNOOZE_TYPE_TASK = "task"
    const val SNOOZE_TYPE_DAILY = "daily"
    const val SNOOZE_TYPE_MORNING = "morning"
}
