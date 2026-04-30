package com.benimgunlerim.ui.today

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import java.time.LocalDate

@Preview(showBackground = true, name = "Today Header")
@Composable
private fun TodayHeaderPreview() {
    BenimGunlerimTheme {
        TodayHeaderCard(
            today = LocalDate.of(2026, 4, 30),
            streak = 4,
            happiness = 78,
            completed = 3,
            total = 8,
            progress = 0.37f,
        )
    }
}

@Preview(showBackground = true, name = "Today List Mixed")
@Composable
private fun TodayListMixedPreview() {
    val today = LocalDate.of(2026, 4, 30)
    BenimGunlerimTheme {
        TodayList(
            today = today,
            routines = listOf(
                TodayRoutineUi(
                    id = "routine_1",
                    name = "Su iç",
                    preferredTime = "09:00",
                    color = null,
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
                    plannedDate = "2026-04-30",
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
                    plannedDate = "2026-04-30",
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
                    plannedDate = "2026-04-28",
                    startTime = null,
                    category = "Finans",
                    color = null,
                    priority = 1,
                    completionState = "pending",
                    reminderTime = null,
                ),
            ),
            completedRoutineIds = emptySet(),
            completionLogs = emptyList(),
            snapshotLoadError = false,
            dayLocked = false,
            actions = TodayListActions(
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
            ),
        )
    }
}

@Preview(showBackground = true, name = "Today List Empty")
@Composable
private fun TodayListEmptyPreview() {
    BenimGunlerimTheme {
        TodayList(
            today = LocalDate.of(2026, 4, 30),
            routines = emptyList(),
            tasks = emptyList(),
            overdueTasks = emptyList(),
            completedRoutineIds = emptySet(),
            completionLogs = emptyList(),
            snapshotLoadError = false,
            dayLocked = false,
            actions = TodayListActions(
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
            ),
        )
    }
}

