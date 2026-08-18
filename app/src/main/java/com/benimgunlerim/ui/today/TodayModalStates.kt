package com.benimgunlerim.ui.today

import androidx.compose.runtime.MutableState
import com.benimgunlerim.domain.model.GameEvent

@Suppress("LongParameterList")
internal class TodayModalStates(
    val showFabMenu: MutableState<Boolean>,
    val showResetDialog: MutableState<Boolean>,
    val showBrainDumpDialog: MutableState<Boolean>,
    val showAddTaskSheet: MutableState<Boolean>,
    val showCloseSheet: MutableState<Boolean>,
    val showMissedDaySheet: MutableState<Boolean>,
    val menuRoutineId: MutableState<String?>,
    val editingRoutine: MutableState<TodayRoutineUi?>,
    val levelUpEvent: MutableState<GameEvent.LevelUp?>,
    val achievementEvent: MutableState<GameEvent.AchievementUnlocked?>,
    val blockCompletionData: MutableState<Triple<String, String, String>?>,
    val archiveRoutineId: MutableState<String?>,
    val deleteTaskId: MutableState<String?>,
)
