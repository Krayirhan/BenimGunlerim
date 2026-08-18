package com.benimgunlerim.ui

object TestTags {
    // ── Bottom Navigation ─────────────────────────────────────────────────
    const val BottomNavToday     = "bottom_nav.today"
    const val BottomNavPlan      = "bottom_nav.plan"
    const val BottomNavRoutines  = "bottom_nav.routines"
    const val BottomNavProgress  = "bottom_nav.progress"
    const val BottomNavSettings  = "bottom_nav.settings"

    // ── Today Screen ──────────────────────────────────────────────────────
    const val TodayRoot  = "screen.today.root"
    const val TodayFab   = "screen.today.add_task.button"
    const val AddTaskTitleField = "screen.add_task.title.field"
    const val TodaySnapshotErrorBanner = "screen.today.snapshot_error.banner"
    const val TodaySwipeHint = "screen.today.swipe_hint.text"
    const val TodayTasksSection = "screen.today.section.tasks"
    const val TodayRoutinesSection = "screen.today.section.routines"
    const val TodayCompletedSection = "screen.today.section.completed"
    const val TodayCloseDayCard = "screen.today.close_day.card"
    const val TodayMissedDayBanner = "screen.today.missed_day.banner"
    fun todayTaskRow(id: String) = "screen.today.task.$id"
    fun todayRoutineRow(id: String) = "screen.today.routine.$id"
    fun todayOverdueRow(id: String) = "screen.today.overdue.$id"

    // Plan Screen
    const val PlanRoot = "screen.plan.root"
    const val PlanFab = "screen.plan.add_task.button"
    const val PlanSnapshotErrorBanner = "screen.plan.snapshot_error.banner"
    const val PlanWeekPicker = "screen.plan.week_picker"
    fun planTaskRow(id: String) = "screen.plan.task.$id"
    fun planOverdueRow(id: String) = "screen.plan.overdue.$id"

    // ── Routines Screen ───────────────────────────────────────────────────
    const val RoutinesRoot = "screen.routines.root"
    const val RoutinesFab  = "screen.routines.add_routine.button"

    // ── Settings Screen ───────────────────────────────────────────────────
    const val SettingsRoot = "screen.settings.root"
    const val SettingsExportButton = "screen.settings.export.button"
    const val SettingsImportButton = "screen.settings.import.button"
}
