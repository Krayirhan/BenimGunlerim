package com.benimgunlerim.ui.today

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import java.time.LocalDate

private val previewDate: LocalDate = LocalDate.of(2026, 4, 30)

@Preview(showBackground = true, name = "Today Header")
@Composable
private fun TodayHeaderPreview() {
    BenimGunlerimTheme {
        TodayHeaderCard(
            today = previewDate,
            streak = 4,
            happiness = 78,
            completed = 3,
            total = 8,
            progress = 0.37f,
        )
    }
}

@Preview(showBackground = true, name = "Today Empty Day")
@Composable
private fun TodayListEmptyPreview() {
    BenimGunlerimTheme {
        TodayList(
            today = previewDate,
            routines = emptyList(),
            tasks = emptyList(),
            overdueTasks = emptyList(),
            completedRoutineIds = emptySet(),
            completionLogs = emptyList(),
            snapshotLoadError = false,
            dayLocked = false,
            actions = previewActions(),
        )
    }
}

@Preview(showBackground = true, name = "Today Two Routines")
@Composable
private fun TodayTwoRoutinesPreview() {
    BenimGunlerimTheme {
        TodayList(
            today = previewDate,
            routines = listOf(
                previewRoutine(id = "routine_1", name = "Su iç", preferredTime = "09:00"),
                previewRoutine(id = "routine_2", name = "Kitap", preferredTime = "20:00"),
            ),
            tasks = emptyList(),
            overdueTasks = emptyList(),
            completedRoutineIds = emptySet(),
            completionLogs = emptyList(),
            snapshotLoadError = false,
            dayLocked = false,
            actions = previewActions(),
        )
    }
}

@Preview(showBackground = true, name = "Today Target Routine")
@Composable
private fun TodayTargetRoutinePreview() {
    BenimGunlerimTheme {
        TodayList(
            today = previewDate,
            routines = listOf(
                previewRoutine(
                    id = "routine_target",
                    name = "Su iç",
                    preferredTime = "09:00",
                    targetType = "count",
                    targetValue = 8,
                    targetUnit = "bardak",
                    currentStreak = 3,
                    bestStreak = 7,
                ),
            ),
            tasks = emptyList(),
            overdueTasks = emptyList(),
            completedRoutineIds = emptySet(),
            completionLogs = listOf(
                CompletionLogEntity(
                    entityType = CompletionEntityType.ROUTINE.value,
                    entityId = "routine_target",
                    id = "log_routine_target",
                    date = previewDate.toString(),
                    completedAt = 0L,
                    status = "completed",
                    note = null,
                    value = 3f,
                ),
            ),
            snapshotLoadError = false,
            dayLocked = false,
            actions = previewActions(),
        )
    }
}

@Preview(showBackground = true, name = "Today Mixed")
@Composable
private fun TodayListMixedPreview() {
    BenimGunlerimTheme {
        TodayList(
            today = previewDate,
            routines = listOf(
                previewRoutine(
                    id = "routine_1",
                    name = "Su iç",
                    preferredTime = "09:00",
                    targetType = "count",
                    targetValue = 8,
                    targetUnit = "bardak",
                    currentStreak = 3,
                    bestStreak = 7,
                ),
            ),
            tasks = listOf(
                TodayTaskUi(
                    id = "task_1",
                    title = "Toplantı notlarını yaz",
                    note = "3 ana karar maddesi",
                    plannedDate = previewDate.toString(),
                    startTime = "10:30",
                    category = "İş",
                    color = null,
                    priority = 1,
                    completionState = "pending",
                    reminderTime = "10:30",
                ),
                TodayTaskUi(
                    id = "task_2",
                    title = "Akşam yürüyüşü",
                    note = null,
                    plannedDate = previewDate.toString(),
                    startTime = null,
                    category = "Sağlık",
                    color = null,
                    priority = 2,
                    completionState = "completed",
                    reminderTime = null,
                ),
            ),
            overdueTasks = listOf(
                TodayTaskUi(
                    id = "task_3",
                    title = "Fatura öde",
                    note = null,
                    plannedDate = previewDate.minusDays(2).toString(),
                    startTime = null,
                    category = "Finans",
                    color = null,
                    priority = 1,
                    completionState = "pending",
                    reminderTime = null,
                ),
            ),
            completedRoutineIds = emptySet(),
            completionLogs = listOf(
                CompletionLogEntity(
                    entityType = CompletionEntityType.ROUTINE.value,
                    entityId = "routine_1",
                    id = "log_routine_1",
                    date = previewDate.toString(),
                    completedAt = 0L,
                    status = "completed",
                    note = null,
                    value = 2f,
                ),
            ),
            snapshotLoadError = false,
            dayLocked = false,
            actions = previewActions(),
        )
    }
}

@Preview(showBackground = true, name = "Today Completed State")
@Composable
private fun TodayCompletedPreview() {
    BenimGunlerimTheme {
        TodayList(
            today = previewDate,
            routines = listOf(previewRoutine(id = "routine_done", name = "Yürüyüş")),
            tasks = listOf(
                TodayTaskUi(
                    id = "task_done",
                    title = "Günü planla",
                    note = null,
                    plannedDate = previewDate.toString(),
                    startTime = "08:30",
                    category = "Plan",
                    color = null,
                    priority = 2,
                    completionState = "completed",
                    reminderTime = "08:30",
                ),
            ),
            overdueTasks = emptyList(),
            completedRoutineIds = setOf("routine_done"),
            completionLogs = emptyList(),
            snapshotLoadError = false,
            dayLocked = false,
            actions = previewActions(),
        )
    }
}

@Preview(showBackground = true, name = "Today Day Close Disabled")
@Composable
private fun TodayDayCloseDisabledPreview() {
    BenimGunlerimTheme {
        CloseDayCard(
            completed = 1,
            total = 3,
            progress = 0.33f,
            isClosed = false,
            mood = null,
            closedNote = null,
            closedEnergyLevel = null,
            canCloseDay = false,
            dailySummaryTime = "21:00",
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Today Missed Day")
@Composable
private fun TodayMissedDayPreview() {
    BenimGunlerimTheme {
        MissedDayBanner(
            date = previewDate.minusDays(1),
            onReview = {},
            onDismiss = {},
        )
    }
}

private fun previewActions() = TodayListActions(
    onToggleRoutine = {},
    onRoutineProgressChange = { _, _, _ -> },
    onOpenRoutineDetail = {},
    onToggleTask = {},
    onOpenTask = {},
    onDeleteTask = {},
    onMoveOverdueToday = {},
    onMoveAllOverdueToday = {},
    onMoveAllOverdueTomorrow = {},
    onAddTask = {},
    onNavigateToRoutines = {},
    onNavigateToPlan = {},
)

private fun previewRoutine(
    id: String,
    name: String,
    preferredTime: String? = null,
    targetType: String = "check",
    targetValue: Int? = null,
    targetUnit: String? = null,
    currentStreak: Int = 0,
    bestStreak: Int = 0,
) = TodayRoutineUi(
    id = id,
    name = name,
    preferredTime = preferredTime,
    color = null,
    targetType = targetType,
    targetValue = targetValue,
    targetUnit = targetUnit,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
)
