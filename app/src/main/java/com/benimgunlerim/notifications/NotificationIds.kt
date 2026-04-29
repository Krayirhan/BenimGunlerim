package com.benimgunlerim.notifications

/**
 * Deterministic, always-positive PendingIntent request codes and notification IDs.
 *
 * String.hashCode() can return Int.MIN_VALUE..Int.MAX_VALUE including negatives.
 * Negative request codes can cause unexpected PendingIntent mismatches on some
 * Android versions. Negative notification IDs silently map to 0 on older SDKs,
 * which means concurrent notifications would overwrite each other.
 *
 * ID partitions (each 90_000 slots — enough for any realistic user-data volume):
 *   10_000 ..  99_999  — task alarms & notification display IDs
 *  300_000 .. 389_999  — routine alarms & notification display IDs
 *  500_000 .. 589_999  — snooze-action PendingIntents (keyed by "action:type:id")
 *  600_000 .. 689_999  — snooze re-show alarms (keyed by "reshow:type:id")
 *
 *  Fixed/known values (outside the dynamic ranges above):
 *    8_000  — MORNING_PLANNER_REQUEST_CODE (content intent + notification ID)
 *   21_000  — daily-summary content intent + notification ID
 */
internal object NotificationIds {

    /** Alarm PendingIntent code AND notification display ID for a task reminder. */
    fun forTask(taskId: String): Int = stable(taskId, 10_000)

    /** Alarm PendingIntent code AND notification display ID for a routine reminder. */
    fun forRoutine(routineId: String): Int = stable(routineId, 300_000)

    /**
     * PendingIntent code for a snooze-action button.
     * Key is "action:$type:$id" so it is always distinct from alarm and re-show codes.
     */
    fun forSnoozeAction(type: String, id: String): Int = stable("action:$type:$id", 500_000)

    /**
     * Alarm PendingIntent code for the snooze re-show alarm.
     * Key is "reshow:$type:$id" so task and routine IDs can never collide even with the
     * same UUID backing string.
     */
    fun forSnoozeReshow(type: String, id: String): Int = stable("reshow:$type:$id", 600_000)

    // ── impl ─────────────────────────────────────────────────────────────────

    private fun stable(key: String, base: Int): Int =
        (key.hashCode() and Int.MAX_VALUE) % 90_000 + base
}
