@file:Suppress("SpellCheckingInspection")

package com.benimgunlerim.ui.today

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.GameEvent
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.domain.model.TaskCompletionState
import com.benimgunlerim.ui.components.AchievementUnlockOverlay
import com.benimgunlerim.ui.components.ConfettiIntensity
import com.benimgunlerim.ui.components.ConfettiOverlay
import com.benimgunlerim.ui.components.FloatingRewardText
import com.benimgunlerim.ui.components.LevelUpOverlay
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import com.benimgunlerim.ui.theme.XpGold
import com.benimgunlerim.ui.today.theme.todayColorTokens
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.domain.validation.TimeInputValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Developer option “Animator duration scale” = off → skip decorative motion for header/check UI. */
@Composable
private fun rememberSystemAnimationsDisabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) < 0.01f
        }.getOrDefault(false)
    }
}

@Composable
internal fun SnapshotErrorBanner(onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(com.benimgunlerim.ui.TestTags.TodaySnapshotErrorBanner),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.today_snapshot_load_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.today_retry))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onOpenRoutineDetail: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val today = viewModel.today()
    val snackbarHost = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val msgTaskMovedTomorrow = stringResource(R.string.today_task_moved_tomorrow)
    val msgOverdueMoved = stringResource(R.string.today_overdue_moved_count)
    val msgTaskDeleted = stringResource(R.string.today_task_deleted)
    val msgTaskCompleted = stringResource(R.string.today_task_completed)
    val msgDaySaved = stringResource(R.string.today_day_saved)
    val msgUndoLabel = stringResource(R.string.action_undo)
    val msgErrDelete = stringResource(R.string.today_error_delete_task)
    val msgErrSave = stringResource(R.string.today_error_save_day)
    val msgErrGeneric = stringResource(R.string.today_error_generic)

    var addText by remember { mutableStateOf("") }
    var addNote by remember { mutableStateOf("") }
    var addTime by remember { mutableStateOf("") }
    var addCategory by remember { mutableStateOf("") }
    var addPriority by remember { mutableStateOf(2) }
    var plannedTaskDate by remember(today) { mutableStateOf(today) }
    var addReminderEnabled by remember { mutableStateOf(true) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showCloseSheet by remember { mutableStateOf(false) }
    var showMissedDaySheet by remember { mutableStateOf(false) }
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteTask by remember { mutableStateOf<TodayTaskUi?>(null) }

    var showConfetti by remember { mutableStateOf(false) }
    var confettiIntensity by remember { mutableStateOf(ConfettiIntensity.Mini) }
    var showReward by remember { mutableStateOf(false) }
    var rewardText by remember { mutableStateOf("") }
    var showLevelUp by remember { mutableStateOf(false) }
    var levelUpLevel by remember { mutableStateOf(0) }
    var levelUpTitle by remember { mutableStateOf("") }
    var showAchievement by remember { mutableStateOf(false) }
    var achievementEmoji by remember { mutableStateOf("") }
    var achievementTitle by remember { mutableStateOf("") }

    val dayIsClosed = state.todayState?.closedAt != null
    val emptyForFab =
        !state.isLoading &&
            state.tasks.isEmpty() &&
            state.routines.isEmpty() &&
            state.overdueTasks.isEmpty()

    LaunchedEffect(showAddSheet, today) {
        if (showAddSheet) {
            plannedTaskDate = today
            addReminderEnabled = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.gameEvents.collect { event ->
            val confettiOn = viewModel.uiState.value.gameState.celebrationEffectsEnabled
            when (event) {
                is GameEvent.RewardEarned -> {
                    rewardText = "+${event.xp} XP"
                    showReward = true
                    if (confettiOn) {
                        showConfetti = true
                        confettiIntensity = ConfettiIntensity.Mini
                    }
                }
                is GameEvent.LevelUp -> {
                    levelUpLevel = event.level
                    levelUpTitle = event.title
                    showLevelUp = true
                    if (confettiOn) {
                        showConfetti = true
                        confettiIntensity = ConfettiIntensity.LevelUp
                    }
                }
                is GameEvent.AchievementUnlocked -> {
                    achievementEmoji = event.emoji
                    achievementTitle = event.title
                    showAchievement = true
                    if (confettiOn) {
                        showConfetti = true
                        confettiIntensity = ConfettiIntensity.Medium
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is TodayUiEffect.TaskMovedTomorrow -> {
                    snackbarHost.showSnackbar(msgTaskMovedTomorrow)
                }
                is TodayUiEffect.OverdueTasksMoved -> {
                    snackbarHost.showSnackbar(msgOverdueMoved.format(effect.count))
                }
                is TodayUiEffect.TaskDeleted -> {
                    val result = snackbarHost.showSnackbar(
                        message = msgTaskDeleted,
                        actionLabel = msgUndoLabel,
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask(effect.taskId)
                    }
                }
                is TodayUiEffect.TaskCompletedUndo -> {
                    val result = snackbarHost.showSnackbar(
                        message = msgTaskCompleted,
                        actionLabel = msgUndoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoTaskToggle(effect.taskId)
                    }
                }
                is TodayUiEffect.DaySaved -> {
                    snackbarHost.showSnackbar(msgDaySaved)
                }
                is TodayUiEffect.ActionFailed -> {
                    val msg = when (effect.messageRes) {
                        R.string.today_error_delete_task -> msgErrDelete
                        R.string.today_error_save_day -> msgErrSave
                        else -> msgErrGeneric
                    }
                    snackbarHost.showSnackbar(msg)
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            AddTaskSheet(
                today = today,
                plannedDate = plannedTaskDate,
                onPlannedDateChange = { plannedTaskDate = it },
                text = addText,
                note = addNote,
                time = addTime,
                category = addCategory,
                priority = addPriority,
                onTextChange = { addText = it },
                onNoteChange = { addNote = it },
                onTimeChange = { addTime = TimeInputValidator.sanitize(it) },
                onCategoryChange = { addCategory = it },
                onPriorityChange = { addPriority = it },
                reminderEnabled = addReminderEnabled,
                onReminderEnabledChange = { addReminderEnabled = it },
                onSave = {
                    if (addText.isNotBlank() && TimeInputValidator.isValid(addTime)) {
                        val timeOrNull = addTime.takeIf { it.isNotBlank() }
                        viewModel.addTask(
                            title = addText,
                            note = addNote,
                            date = plannedTaskDate,
                            startTime = timeOrNull,
                            category = addCategory.takeIf { it.isNotBlank() },
                            priority = addPriority,
                            reminderTime = if (addReminderEnabled) timeOrNull else null,
                        )
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        addText = ""
                        addNote = ""
                        addTime = ""
                        addCategory = ""
                        addPriority = 2
                        plannedTaskDate = today
                        showAddSheet = false
                    }
                },
            )
        }
    }

    selectedTaskId?.let { taskId ->
        val task = (state.tasks + state.overdueTasks).firstOrNull { it.id == taskId } ?: return@let
        val subtasks by viewModel.subTasksFlow(task.id).collectAsState(initial = emptyList())
        ModalBottomSheet(onDismissRequest = { selectedTaskId = null }) {
            TaskDetailSheet(
                task = task,
                today = today,
                subtasks = subtasks,
                interactionLocked = dayIsClosed,
                onSave = { title, note, date, startTime, category, priority, reminderTime ->
                    viewModel.updateTask(
                        taskId = task.id,
                        title = title,
                        note = note,
                        date = date,
                        startTime = startTime,
                        category = category,
                        priority = priority,
                        reminderTime = reminderTime,
                    )
                    selectedTaskId = null
                },
                onMoveTomorrow = {
                    viewModel.moveTaskToTomorrow(task.id)
                    selectedTaskId = null
                },
                onDelete = {
                    if (task.completionState == TaskCompletionState.COMPLETED.value) {
                        pendingDeleteTask = task
                    } else {
                        viewModel.deleteTask(task.id)
                    }
                    selectedTaskId = null
                },
                onAddSubTask = { title -> viewModel.addSubTask(task.id, title) },
                onToggleSubTask = { st -> viewModel.toggleSubTask(st) },
                onDeleteSubTask = { st -> viewModel.deleteSubTask(st) },
            )
        }
    }

    if (showCloseSheet) {
        ModalBottomSheet(onDismissRequest = { showCloseSheet = false }) {
            CloseDaySheet(
                completedCount = state.tasks.count {
                    it.completionState == TaskCompletionState.COMPLETED.value
                } + state.routines.count { it.id in state.completedRoutineIds },
                totalCount = state.tasks.size + state.routines.size,
                progress = state.progress,
                overdueCount = state.overdueTasks.size,
                onSave = { mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks ->
                    viewModel.saveDailySummaryWithOptionalCarry(
                        note = note,
                        mood = mood,
                        energy = energy,
                        bestMoment = bestMoment,
                        challenge = challenge,
                        tomorrowIntention = tomorrowIntention,
                        carryOverdueToTomorrow = carryTasks,
                    )
                    showCloseSheet = false
                },
            )
        }
    }

    if (showMissedDaySheet && state.missedDay != null) {
        val missedDate = state.missedDay!!
        ModalBottomSheet(onDismissRequest = { showMissedDaySheet = false }) {
            CloseDaySheet(
                completedCount = 0,
                totalCount = 0,
                progress = 0f,
                overdueCount = 0,
                onSave = { mood, energy, note, bestMoment, challenge, tomorrowIntention, _ ->
                    viewModel.saveMissedDaySummary(missedDate, note, mood, energy, bestMoment, challenge, tomorrowIntention)
                    showMissedDaySheet = false
                },
            )
        }
    }

    val completedTasks = state.tasks.count { it.completionState == TaskCompletionState.COMPLETED.value }
    val completedRoutines = state.routines.count { it.id in state.completedRoutineIds }
    val total = state.tasks.size + state.routines.size
    val completed = completedTasks + completedRoutines
    Box(Modifier.fillMaxSize().testTag(com.benimgunlerim.ui.TestTags.TodayRoot)) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHost) },
            floatingActionButton = {
                if (emptyForFab) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddSheet = true },
                        containerColor = CandyPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag(com.benimgunlerim.ui.TestTags.TodayFab),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.today_add_cd))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.today_empty_fab_extended), style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    FloatingActionButton(
                        onClick = { showAddSheet = true },
                        containerColor = CandyPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag(com.benimgunlerim.ui.TestTags.TodayFab),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.today_add_cd))
                    }
                }
            },
        ) { padding ->
            TodayContent(
                state = state,
                today = today,
                completed = completed,
                total = total,
                dayIsClosed = dayIsClosed,
                padding = padding,
                haptic = haptic,
                viewModel = viewModel,
                onNavigateToRoutines = onNavigateToRoutines,
                onNavigateToPlan = onNavigateToPlan,
                onOpenRoutineDetail = onOpenRoutineDetail,
                onOpenTask = { selectedTaskId = it },
                onShowAddTaskSheet = { showAddSheet = true },
                onShowMissedDaySheet = { showMissedDaySheet = true },
                onShowCloseDaySheet = { showCloseSheet = true },
                onConfirmDeleteCompletedTask = { pendingDeleteTask = it },
            )
        }

        ConfettiOverlay(showConfetti, confettiIntensity) { showConfetti = false }
        FloatingRewardText(rewardText, showReward, Modifier.align(Alignment.Center)) { showReward = false }
        LevelUpOverlay(showLevelUp, levelUpLevel, levelUpTitle) { showLevelUp = false }
        AchievementUnlockOverlay(showAchievement, achievementEmoji, achievementTitle) { showAchievement = false }

        pendingDeleteTask?.let { task ->
            AlertDialog(
                onDismissRequest = { pendingDeleteTask = null },
                title = { Text(stringResource(R.string.today_delete_completed_title)) },
                text = { Text(stringResource(R.string.today_delete_completed_body, task.title)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTask(task.id)
                            pendingDeleteTask = null
                        },
                    ) {
                        Text(stringResource(R.string.today_delete_label), color = StreakCoral)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteTask = null }) {
                        Text(stringResource(R.string.action_dismiss))
                    }
                },
            )
        }
    }
}

@Composable
internal fun HomeBackground(): Brush = Brush.verticalGradient(
    listOf(
        MaterialTheme.todayColorTokens.backgroundTop,
        MaterialTheme.todayColorTokens.backgroundBottom,
    ),
)

@Composable
private fun mutedAccent(color: Color): Color = lerp(color, MaterialTheme.colorScheme.surface, 0.40f)

private data class TodayLayoutMetrics(
    val isCompact: Boolean,
    val headerCorner: Dp,
    val headerPadding: Dp,
    val headerSpacing: Dp,
    val headerMetaSpacing: Dp,
    val headerProgressHeight: Dp,
    val sectionCorner: Dp,
    val sectionPaddingHorizontal: Dp,
    val sectionPaddingVertical: Dp,
    val sectionItemSpacing: Dp,
    val listBlockSpacing: Dp,
    val summaryBlockSpacing: Dp,
    val dividerTopPadding: Dp,
    val dividerBottomPadding: Dp,
    val itemCorner: Dp,
    val itemMinHeight: Dp,
    val itemContentHorizontal: Dp,
    val itemContentVertical: Dp,
    val accentRailWidth: Dp,
    val accentRailHeight: Dp,
    val iconButtonSize: Dp,
    val metaChipVertical: Dp,
    val routineControlCorner: Dp,
    val routineProgressHeight: Dp,
)

@Composable
private fun rememberTodayLayoutMetrics(): TodayLayoutMetrics {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isCompact = screenWidthDp < 380
    val scale = (screenWidthDp / 411f).coerceIn(if (isCompact) 0.9f else 0.94f, if (isCompact) 1.0f else 1.1f)
    fun s(value: Float): Dp = (value * scale).dp
    return remember(screenWidthDp, isCompact) {
        TodayLayoutMetrics(
            isCompact = isCompact,
            headerCorner = s(if (isCompact) 24f else 28f),
            headerPadding = s(if (isCompact) 14f else 16f),
            headerSpacing = s(if (isCompact) 8f else 10f),
            headerMetaSpacing = s(if (isCompact) 2f else 3f),
            headerProgressHeight = s(if (isCompact) 7f else 8f),
            sectionCorner = s(if (isCompact) 14f else 16f),
            sectionPaddingHorizontal = s(if (isCompact) 10f else 12f),
            sectionPaddingVertical = s(if (isCompact) 9f else 10f),
            sectionItemSpacing = s(if (isCompact) 7f else 8f),
            listBlockSpacing = s(if (isCompact) 9f else 10f),
            summaryBlockSpacing = s(2f),
            dividerTopPadding = s(4f),
            dividerBottomPadding = s(2f),
            itemCorner = s(20f),
            itemMinHeight = s(if (isCompact) 78f else 82f),
            itemContentHorizontal = s(12f),
            itemContentVertical = s(if (isCompact) 10f else 11f),
            accentRailWidth = s(2f),
            accentRailHeight = s(if (isCompact) 64f else 68f),
            iconButtonSize = s(40f),
            metaChipVertical = s(2f),
            routineControlCorner = s(12f),
            routineProgressHeight = s(5f),
        )
    }
}

@Composable
internal fun TodayHeaderCard(
    today: LocalDate,
    streak: Int,
    happiness: Int,
    completed: Int,
    total: Int,
    progress: Float,
) {
    val metrics = rememberTodayLayoutMetrics()
    val date = remember(today) {
        today
            .format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("tr", "TR")))
            .replaceFirstChar { it.titlecase(Locale("tr", "TR")) }
    }
    val instantTransition = rememberSystemAnimationsDisabled()
    val targetProgress = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetProgress,
        if (instantTransition) snap() else tween(800, easing = FastOutSlowInEasing),
        label = "headerProgress",
    )
    val percent = (animated * 100).toInt()
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(metrics.headerCorner))
            .background(MaterialTheme.todayColorTokens.headerSurface)
            .border(1.dp, MaterialTheme.todayColorTokens.headerBorder, RoundedCornerShape(metrics.headerCorner))
            .padding(metrics.headerPadding),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(metrics.headerSpacing)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(metrics.headerMetaSpacing)) {
                    Text(
                        date,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(.85f),
                    )
                    Text(
                        "$completed / $total",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = Color.White,
                    )
                    Text(
                        stringResource(R.string.today_header_subtitle, streak, happiness),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(.90f),
                    )
                }
                Text(
                    "%$percent",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(metrics.headerProgressHeight)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(.20f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animated)
                        .height(metrics.headerProgressHeight)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(.88f)),
                )
            }
        }
    }
}

@Composable
internal fun TodayList(
    today: LocalDate,
    routines: List<TodayRoutineUi>,
    tasks: List<TodayTaskUi>,
    overdueTasks: List<TodayTaskUi>,
    completedRoutineIds: Set<String>,
    completionLogs: List<CompletionLogEntity>,
    snapshotLoadError: Boolean,
    dayLocked: Boolean,
    actions: TodayListActions,
) {
    var completedExpanded by remember { mutableStateOf(false) }
    val metrics = rememberTodayLayoutMetrics()
    val openTasks = tasks.filterNot { it.completionState == TaskCompletionState.COMPLETED.value }
    val completedTasks = tasks.filter { it.completionState == TaskCompletionState.COMPLETED.value }
    val openRoutines = routines.filterNot { it.id in completedRoutineIds }
    val completedRoutines = routines.filter { it.id in completedRoutineIds }
    val sortedOpenTasks = remember(openTasks) { openTasks.sortedWith(todayTaskComparator()) }
    val sortedOverdueTasks = remember(overdueTasks, today) { overdueTasks.sortedWith(overdueTaskComparator(today)) }
    val sortedOpenRoutines = remember(openRoutines) {
        openRoutines.sortedWith(
            compareBy<TodayRoutineUi> { it.preferredTime.isNullOrBlank() }
                .thenBy { it.preferredTime ?: "99:99" }
                .thenBy { it.name.lowercase(Locale("tr", "TR")) },
        )
    }
    val completedTotal = completedTasks.size + completedRoutines.size
    val completedHeavy = completedTotal > 3
    val visibleCompletedTasks = if (completedExpanded) completedTasks else completedTasks.take(2)
    val visibleCompletedRoutines = if (completedExpanded) completedRoutines else completedRoutines.take(1)
    val routineLogsById = remember(completionLogs) {
        completionLogs
            .filter { it.entityType == CompletionEntityType.ROUTINE.value }
            .associateBy { it.entityId }
    }

    val summary = listOfNotNull(
        if (overdueTasks.isNotEmpty()) {
            stringResource(R.string.today_summary_overdue, overdueTasks.size)
        } else null,
        if (openTasks.isNotEmpty()) {
            stringResource(R.string.today_summary_tasks, openTasks.size)
        } else null,
        if (openRoutines.isNotEmpty()) {
            stringResource(R.string.today_summary_routines, openRoutines.size)
        } else null,
    ).joinToString(" · ").ifBlank { stringResource(R.string.today_summary_all_done) }

    val readableSummary = normalizeMiddleDot(summary)
    val guidance = when {
        overdueTasks.isNotEmpty() -> stringResource(R.string.today_guidance_overdue)
        openTasks.isNotEmpty() || openRoutines.isNotEmpty() -> stringResource(R.string.today_guidance_focus)
        else -> stringResource(R.string.today_guidance_all_done)
    }

    Column(verticalArrangement = Arrangement.spacedBy(metrics.listBlockSpacing)) {
        Column(verticalArrangement = Arrangement.spacedBy(metrics.summaryBlockSpacing)) {
            Text(
                text = readableSummary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
            )
            Text(
                text = guidance,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(.66f),
            )
        }

        if (tasks.isEmpty() && routines.isEmpty() && overdueTasks.isEmpty()) {
            if (snapshotLoadError) {
                EmptyBox(stringResource(R.string.today_snapshot_load_error), StreakCoral)
                return@Column
            }
            EmptyTodayOnboarding(
                onAddTask = actions.onAddTask,
                onNavigateToPlan = actions.onNavigateToPlan,
                onNavigateToRoutines = actions.onNavigateToRoutines,
            )
            return@Column
        }

        if (overdueTasks.isNotEmpty()) {
            QuietDivider(stringResource(R.string.today_overdue_title))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = actions.onMoveAllOverdueToday, enabled = !dayLocked) {
                    Text(stringResource(R.string.today_bulk_all_today))
                }
                TextButton(onClick = actions.onMoveAllOverdueTomorrow, enabled = !dayLocked) {
                    Text(stringResource(R.string.today_bulk_all_tomorrow))
                }
            }
            sortedOverdueTasks.forEach { task ->
                Box(Modifier.testTag(com.benimgunlerim.ui.TestTags.todayOverdueRow(task.id))) {
                    OverdueTaskRow(
                        task = task,
                        today = today,
                        interactionLocked = dayLocked,
                        onToggle = actions.onToggleTask,
                        onOpen = actions.onOpenTask,
                        onMoveToday = actions.onMoveOverdueToday,
                    )
                }
            }
        }

        if (openTasks.isNotEmpty() || openRoutines.isNotEmpty()) {
            QuietDivider(stringResource(R.string.today_focus_now_label))
            if (!dayLocked && sortedOpenTasks.size >= 2) {
                Text(
                    stringResource(R.string.today_swipe_delete_hint),
                    modifier = Modifier.testTag(com.benimgunlerim.ui.TestTags.TodaySwipeHint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.62f),
                )
            }
            if (sortedOpenTasks.isNotEmpty()) {
                SectionCard(
                    title = stringResource(R.string.today_tasks_subsection),
                    tint = mutedAccent(CandyPrimary),
                    containerColor = MaterialTheme.todayColorTokens.tasksSectionSurface,
                    borderColor = MaterialTheme.todayColorTokens.tasksSectionBorder,
                    metrics = metrics,
                    modifier = Modifier.testTag(com.benimgunlerim.ui.TestTags.TodayTasksSection),
                ) {
                    sortedOpenTasks.forEach { task ->
                        Box(Modifier.testTag(com.benimgunlerim.ui.TestTags.todayTaskRow(task.id))) {
                            SwipeableTaskRow(
                                task = task,
                                interactionLocked = dayLocked,
                                onToggle = actions.onToggleTask,
                                onOpen = actions.onOpenTask,
                                onDelete = actions.onDeleteTask,
                            )
                        }
                    }
                }
            }
            if (sortedOpenRoutines.isNotEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                )
                SectionCard(
                    title = stringResource(R.string.today_routines_subsection),
                    tint = mutedAccent(LevelSky),
                    containerColor = MaterialTheme.todayColorTokens.routinesSectionSurface,
                    borderColor = MaterialTheme.todayColorTokens.routinesSectionBorder,
                    metrics = metrics,
                    modifier = Modifier.testTag(com.benimgunlerim.ui.TestTags.TodayRoutinesSection),
                ) {
                    sortedOpenRoutines.forEach { routine ->
                        Box(Modifier.testTag(com.benimgunlerim.ui.TestTags.todayRoutineRow(routine.id))) {
                            RoutineRow(
                                routine = routine,
                                log = routineLogsById[routine.id],
                                done = false,
                                interactionLocked = dayLocked,
                                onToggle = actions.onToggleRoutine,
                                onProgressChange = actions.onRoutineProgressChange,
                                onOpenDetail = actions.onOpenRoutineDetail,
                            )
                        }
                    }
                }
            }
        }

        if (completedTasks.isNotEmpty() || completedRoutines.isNotEmpty()) {
            QuietDivider(stringResource(R.string.today_completed_label), Modifier.testTag(com.benimgunlerim.ui.TestTags.TodayCompletedSection))
            if (!completedExpanded) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.today_completed_summary, completedTotal),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                        )
                        TextButton(onClick = { completedExpanded = true }) {
                            Text(stringResource(R.string.today_completed_show_more, completedTotal))
                        }
                    }
                }
            } else {
                visibleCompletedTasks.forEach { task ->
                    Box(Modifier.testTag(com.benimgunlerim.ui.TestTags.todayTaskRow(task.id))) {
                        SwipeableTaskRow(
                            task = task,
                            interactionLocked = dayLocked,
                            onToggle = actions.onToggleTask,
                            onOpen = actions.onOpenTask,
                            onDelete = actions.onDeleteTask,
                        )
                    }
                }
                visibleCompletedRoutines.forEach { routine ->
                    Box(Modifier.testTag(com.benimgunlerim.ui.TestTags.todayRoutineRow(routine.id))) {
                        RoutineRow(
                            routine = routine,
                            log = routineLogsById[routine.id],
                            done = true,
                            interactionLocked = dayLocked,
                            onToggle = actions.onToggleRoutine,
                            onProgressChange = actions.onRoutineProgressChange,
                            onOpenDetail = actions.onOpenRoutineDetail,
                        )
                    }
                }
                TextButton(onClick = { completedExpanded = false }) {
                    Text(stringResource(R.string.today_completed_show_less))
                }
            }
        }
    }
}


@Composable
private fun EmptyTodayOnboarding(
    onAddTask: () -> Unit,
    onNavigateToPlan: () -> Unit,
    onNavigateToRoutines: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(LevelSky.copy(.08f))
            .border(1.dp, LevelSky.copy(.14f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.today_tasks_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddTask, colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White)) {
                Text(stringResource(R.string.today_empty_cta_task))
            }
            OutlinedButton(onClick = onNavigateToPlan) {
                Text(stringResource(R.string.today_empty_cta_plan))
            }
        }
        OutlinedButton(onClick = onNavigateToRoutines) {
            Text(stringResource(R.string.today_empty_cta_routine))
        }
    }
}

@Composable
private fun QuietDivider(label: String, modifier: Modifier = Modifier) {
    val metrics = rememberTodayLayoutMetrics()
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(.78f),
        modifier = modifier
            .padding(top = metrics.dividerTopPadding, bottom = metrics.dividerBottomPadding)
            .semantics { heading() },
    )
}

@Composable
private fun QuietSubLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(.62f),
        modifier = Modifier.padding(top = 2.dp),
    )
}

@Composable
private fun SectionCard(
    title: String,
    tint: Color,
    containerColor: Color,
    borderColor: Color,
    metrics: TodayLayoutMetrics,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(metrics.sectionCorner))
            .background(containerColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(metrics.sectionCorner))
            .padding(horizontal = metrics.sectionPaddingHorizontal, vertical = metrics.sectionPaddingVertical),
        verticalArrangement = Arrangement.spacedBy(metrics.sectionItemSpacing),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = tint.copy(alpha = 0.92f),
        )
        content()
    }
}

@Composable
private fun MetaTag(text: String) {
    val metrics = rememberTodayLayoutMetrics()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.90f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 8.dp, vertical = metrics.metaChipVertical),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            maxLines = 1,
        )
    }
}

@Composable
private fun OverdueTaskRow(
    task: TodayTaskUi,
    today: LocalDate,
    interactionLocked: Boolean,
    onToggle: (TodayTaskUi) -> Unit,
    onOpen: (TodayTaskUi) -> Unit,
    onMoveToday: (TodayTaskUi) -> Unit,
) {
    val done = task.completionState == TaskCompletionState.COMPLETED.value
    val openTaskLabel = stringResource(R.string.today_task_open_cd)
    val plannedLabel = runCatching { LocalDate.parse(task.plannedDate) }.getOrNull()?.let { date ->
        val days = ChronoUnit.DAYS.between(date, today).toInt()
        when {
            days <= 0 -> task.plannedDate
            days == 1 -> stringResource(R.string.today_relative_yesterday)
            else -> stringResource(R.string.today_relative_days_ago, days)
        }
    } ?: task.plannedDate
    ItemRow(done = done, color = StreakCoral) {
        CheckCircle(done, StreakCoral, enabled = !interactionLocked) { onToggle(task) }
        Column(
            modifier = Modifier.weight(1f).clickable(onClickLabel = openTaskLabel) { onOpen(task) },
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                plannedLabel,
                style = MaterialTheme.typography.labelSmall,
                color = StreakCoral.copy(.85f),
                maxLines = 1,
            )
        }
        IconButton(onClick = { onMoveToday(task) }, enabled = !interactionLocked) {
            Icon(
                Icons.Outlined.CalendarToday,
                contentDescription = stringResource(R.string.today_move_to_today_cd),
                tint = StreakCoral,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TaskRow(
    task: TodayTaskUi,
    interactionLocked: Boolean,
    onToggle: (TodayTaskUi) -> Unit,
    onOpen: (TodayTaskUi) -> Unit,
    onDelete: (TodayTaskUi) -> Unit,
) {
    val metrics = rememberTodayLayoutMetrics()
    var menuOpen by remember(task.id) { mutableStateOf(false) }
    val done = task.completionState == TaskCompletionState.COMPLETED.value
    val color = mutedAccent(task.color?.let(::parseColorOrNull) ?: CategoryPalette.colorFor(task.category ?: task.title))
    val overflowCd = stringResource(R.string.today_task_overflow_cd)
    val openTaskLabel = stringResource(R.string.today_task_open_cd)
    val timeNone = stringResource(R.string.today_task_time_none_short)
    val priorityLabel = when (task.priority) {
        1 -> stringResource(R.string.today_priority_short_high)
        2 -> stringResource(R.string.today_priority_short_normal)
        else -> stringResource(R.string.today_priority_short_low)
    }
    val categoryText = task.category?.trim()?.takeIf { it.isNotEmpty() }
    val timeText = task.startTime ?: timeNone
    val notePreview = task.note?.trim()?.takeIf { it.isNotEmpty() }
    ItemRow(done = done, color = color) {
        CheckCircle(done, color, enabled = !interactionLocked) { onToggle(task) }
        Column(Modifier.weight(1f).clickable(onClickLabel = openTaskLabel) { onOpen(task) }, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, textDecoration = if (done) TextDecoration.LineThrough else null),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (done) .58f else 1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetaTag(text = priorityLabel)
                categoryText?.let { MetaTag(text = it) }
                MetaTag(text = timeText)
            }
            notePreview?.let {
                Text(
                    stringResource(R.string.today_note_preview, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(metrics.iconButtonSize),
            ) {
                Icon(
                    Icons.Rounded.MoreHoriz,
                    contentDescription = overflowCd,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.today_task_menu_open_detail)) },
                    onClick = {
                        menuOpen = false
                        onOpen(task)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.today_delete_label)) },
                    leadingIcon = {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = StreakCoral)
                    },
                    onClick = {
                        menuOpen = false
                        if (!interactionLocked) onDelete(task)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskRow(
    task: TodayTaskUi,
    interactionLocked: Boolean,
    onToggle: (TodayTaskUi) -> Unit,
    onOpen: (TodayTaskUi) -> Unit,
    onDelete: (TodayTaskUi) -> Unit,
) {
    val metrics = rememberTodayLayoutMetrics()
    val swipeDeleteCd = stringResource(R.string.today_swipe_to_delete_cd)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (interactionLocked) return@rememberSwipeToDismissBoxState false
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(task)
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.4f },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(metrics.itemCorner))
                    .background(StreakCoral.copy(.15f))
                    .semantics { contentDescription = swipeDeleteCd },
                contentAlignment = Alignment.CenterEnd,
            ) {
                Row(
                    Modifier.padding(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.today_delete_label), tint = StreakCoral, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.today_delete_label), style = MaterialTheme.typography.labelMedium, color = StreakCoral)
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = !interactionLocked,
    ) {
        TaskRow(
            task = task,
            interactionLocked = interactionLocked,
            onToggle = onToggle,
            onOpen = onOpen,
            onDelete = onDelete,
        )
    }
}

@Composable
@Suppress("CyclomaticComplexMethod")
private fun RoutineRow(
    routine: TodayRoutineUi,
    log: CompletionLogEntity?,
    done: Boolean,
    interactionLocked: Boolean,
    onToggle: (TodayRoutineUi) -> Unit,
    onProgressChange: (TodayRoutineUi, Float, Boolean) -> Unit,
    onOpenDetail: (TodayRoutineUi) -> Unit,
) {
    val metrics = rememberTodayLayoutMetrics()
    val color = mutedAccent(routine.color?.let(::parseColorOrNull) ?: CandyPrimary)
    val isCheckType = routine.targetType == RoutineTargetType.CHECK.value
    val streakLabel = if (routine.currentStreak > 0) {
        stringResource(R.string.today_routine_streak_count, routine.currentStreak)
    } else if (routine.bestStreak > 0) {
        stringResource(R.string.today_routine_best_streak_count, routine.bestStreak)
    } else {
        stringResource(R.string.today_routine_streak_pill)
    }
    val currentValue = log?.value ?: 0f
    val targetValue = routine.targetValue?.toFloat()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(metrics.sectionCorner))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, color.copy(alpha = 0.52f), RoundedCornerShape(metrics.sectionCorner))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(metrics.sectionItemSpacing),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CheckCircle(done, color, enabled = !interactionLocked) { onToggle(routine) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    routine.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, textDecoration = if (done) TextDecoration.LineThrough else null),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (done) .58f else 1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCheckType && routine.targetValue != null) {
                    val unit = routine.targetUnit ?: ""
                    Text(
                        if (done) {
                            stringResource(R.string.today_routine_done_target_result, currentValue.toInt(), routine.targetValue, unit)
                        } else {
                            stringResource(R.string.today_routine_progress_value, currentValue.toInt(), routine.targetValue, unit).trim()
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        maxLines = 1,
                    )
                } else {
                    Text(
                        routine.preferredTime?.let { stringResource(R.string.today_routine_time_format, it) }
                            ?: stringResource(R.string.today_routine_daily_no_time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Text(
                    if (done) stringResource(R.string.today_routine_done_pill) else streakLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (done) CompletedGreen else color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { onOpenDetail(routine) }, modifier = Modifier.size(metrics.iconButtonSize)) {
                Icon(
                    Icons.Rounded.MoreHoriz,
                    contentDescription = stringResource(R.string.today_routine_open_detail_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!isCheckType && targetValue != null && !done) {
            val target = targetValue
            val step = if (routine.targetType == "count") 1f else (target / 4f).coerceAtLeast(1f)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(metrics.routineControlCorner))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                    .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(metrics.routineControlCorner))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { onProgressChange(routine, (currentValue - step).coerceAtLeast(0f), done) },
                        enabled = !interactionLocked,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.today_routine_decrease),
                            style = MaterialTheme.typography.labelMedium,
                            color = color,
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.fillMaxWidth().height(metrics.routineProgressHeight).clip(RoundedCornerShape(99.dp)).background(color.copy(.16f))) {
                            Box(Modifier.fillMaxWidth((currentValue / target).coerceIn(0f, 1f)).height(metrics.routineProgressHeight).clip(RoundedCornerShape(99.dp)).background(color.copy(alpha = 0.85f)))
                        }
                        Text(
                            stringResource(R.string.today_routine_target_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    TextButton(
                        onClick = { onProgressChange(routine, (currentValue + step).coerceAtMost(target), done) },
                        enabled = !interactionLocked,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.today_routine_increase),
                            style = MaterialTheme.typography.labelMedium,
                            color = color,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRow(
    done: Boolean,
    color: Color,
    tone: Color? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val metrics = rememberTodayLayoutMetrics()
    val reduceMotion = rememberSystemAnimationsDisabled()
    val animatedBg by animateColorAsState(
        targetValue = (tone ?: MaterialTheme.colorScheme.surface).copy(alpha = if (done) .96f else 1f),
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 220),
        label = "itemRowBg",
    )
    val animatedBorder by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.outline.copy(alpha = 0.62f),
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 180),
        label = "itemRowBorder",
    )
    val rowModifier = if (reduceMotion) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(metrics.itemCorner))
            .background(animatedBg)
            .border(1.dp, animatedBorder, RoundedCornerShape(metrics.itemCorner))
    } else {
        Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(metrics.itemCorner))
            .background(animatedBg)
            .border(1.dp, animatedBorder, RoundedCornerShape(metrics.itemCorner))
    }
    Row(
        rowModifier.heightIn(min = metrics.itemMinHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(metrics.accentRailWidth).height(metrics.accentRailHeight).background(color.copy(alpha = if (done) .30f else .55f)))
        Row(
            Modifier.weight(1f).padding(horizontal = metrics.itemContentHorizontal, vertical = metrics.itemContentVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun CheckCircle(done: Boolean, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    val markDone = stringResource(R.string.today_mark_done_label)
    val unmarkDone = stringResource(R.string.today_unmark_done_label)
    val instantTransition = rememberSystemAnimationsDisabled()
    val animatedFill by animateColorAsState(
        targetValue = if (done) color else color.copy(.10f),
        animationSpec = if (instantTransition) snap() else tween(durationMillis = 140),
        label = "checkFill",
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (done) 1.06f else 1f,
        animationSpec = if (instantTransition) snap() else tween(durationMillis = 170, easing = FastOutSlowInEasing),
        label = "checkScale",
    )
    Box(
        Modifier
            .minimumInteractiveComponentSize()
            .alpha(if (enabled) 1f else 0.42f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clip(CircleShape)
                .background(animatedFill)
                .border(2.dp, color.copy(if (done) 1f else .45f), CircleShape)
                .semantics {
                    role = Role.Checkbox
                    contentDescription = if (done) unmarkDone else markDone
                }
                .clickable(
                    enabled = enabled,
                    onClickLabel = if (done) unmarkDone else markDone,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.today_done_cd), tint = Color.White, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun EmptyBox(text: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(color.copy(.08f)).border(1.dp, color.copy(.14f), RoundedCornerShape(18.dp)).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(color.copy(.12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.today_empty_hint_icon_cd), tint = color, modifier = Modifier.size(16.dp))
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun CloseDayCard(
    completed: Int,
    total: Int,
    progress: Float,
    isClosed: Boolean,
    mood: String?,
    closedNote: String?,
    closedEnergyLevel: Int?,
    canCloseDay: Boolean,
    dailySummaryTime: String,
    onClick: () -> Unit,
) {
    val metrics = rememberTodayLayoutMetrics()
    if (isClosed) {
        // Day is closed — show summary state
        val moodColor = when (mood) {
            "harika" -> CompletedGreen; "iyi" -> CandyPrimary; "kotu", "cok_kotu" -> StreakCoral; else -> CandySecondary
        }
        val moodLabel = when (mood) {
            "harika" -> stringResource(R.string.today_mood_great)
            "iyi" -> stringResource(R.string.today_mood_good)
            "normal" -> stringResource(R.string.today_mood_normal)
            "kotu" -> stringResource(R.string.today_mood_bad)
            "cok_kotu" -> stringResource(R.string.today_mood_very_bad)
            else -> stringResource(R.string.today_mood_closed)
        }
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(metrics.headerCorner))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, mutedAccent(CompletedGreen).copy(.30f), RoundedCornerShape(metrics.headerCorner))
                .padding(if (metrics.isCompact) 16.dp else 20.dp)
                .testTag(com.benimgunlerim.ui.TestTags.TodayCloseDayCard),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(stringResource(R.string.today_closed_pill), CompletedGreen)
                    Pill(moodLabel, moodColor)
                }
                Text(stringResource(R.string.today_closed_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.today_closed_desc, completed, total), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                closedEnergyLevel?.takeIf { it in 1..5 }?.let { e ->
                    Text(
                        stringResource(R.string.today_closed_energy_preview, e),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                closedNote?.trim()?.takeIf { it.isNotEmpty() }?.let { n ->
                    Text(
                        stringResource(R.string.today_closed_note_preview, n.take(160)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(40.dp)) {
                    Text(stringResource(R.string.today_closed_update_btn), color = CompletedGreen, maxLines = 1)
                }
            }
        }
    } else if (!canCloseDay) {
        // Before summary time — show locked state
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(metrics.sectionCorner))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(.6f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(.12f), RoundedCornerShape(metrics.sectionCorner))
                .padding(horizontal = if (metrics.isCompact) 12.dp else 14.dp, vertical = if (metrics.isCompact) 10.dp else 12.dp)
                .testTag(com.benimgunlerim.ui.TestTags.TodayCloseDayCard),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Pill(stringResource(R.string.today_locked_pill), MaterialTheme.colorScheme.onSurfaceVariant.copy(.5f))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.today_locked_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface.copy(.68f))
                    Text(stringResource(R.string.today_locked_desc, dailySummaryTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(.72f), maxLines = 1)
                }
            }
        }
    } else {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(metrics.headerCorner))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, mutedAccent(CandySecondary).copy(.30f), RoundedCornerShape(metrics.headerCorner))
                .padding(if (metrics.isCompact) 16.dp else 20.dp)
                .testTag(com.benimgunlerim.ui.TestTags.TodayCloseDayCard),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Pill(stringResource(R.string.today_locked_pill), CandySecondary)
                Text(stringResource(R.string.today_active_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.today_active_desc, completed, total), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.fillMaxWidth().height(metrics.headerProgressHeight).clip(RoundedCornerShape(99.dp)).background(CandySecondary.copy(.10f))) {
                    Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(metrics.headerProgressHeight).clip(RoundedCornerShape(99.dp)).background(CandySecondary))
                }
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(if (metrics.isCompact) 42.dp else 44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(CandySecondary, Color.White),
                ) {
                    Text(stringResource(R.string.today_active_btn), maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun MissedDayBanner(
    date: LocalDate,
    onReview: () -> Unit,
    onDismiss: () -> Unit,
) {
    val metrics = rememberTodayLayoutMetrics()
    val label = remember(date) {
        date.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("tr", "TR")))
    }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(metrics.itemCorner))
            .background(StreakCoral.copy(.08f))
            .border(1.dp, StreakCoral.copy(.22f), RoundedCornerShape(metrics.itemCorner))
            .padding(horizontal = if (metrics.isCompact) 14.dp else 16.dp, vertical = if (metrics.isCompact) 10.dp else 12.dp)
            .testTag(com.benimgunlerim.ui.TestTags.TodayMissedDayBanner),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.today_missed_label), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = StreakCoral)
                Text(
                    stringResource(R.string.today_missed_emphasis, label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            TextButton(onClick = onReview) { Text(stringResource(R.string.today_missed_review_btn), color = StreakCoral, style = MaterialTheme.typography.labelLarge) }
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.today_missed_skip_btn), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge) }
        }
    }
}


@Composable
private fun Pill(text: String, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(.12f)).border(1.dp, color.copy(.20f), RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 5.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun parseColorOrNull(raw: String): Color? = runCatching {
    val hex = raw.removePrefix("#")
    Color(("FF$hex").takeLast(8).toLong(16))
}.getOrNull()

private fun normalizeMiddleDot(raw: String): String =
    raw
        .replace("Â·", "·")
        .replace("\u00C2\u00B7", "\u00B7")

private fun todayTaskComparator(): Comparator<TodayTaskUi> =
    compareBy<TodayTaskUi> { it.startTime.isNullOrBlank() }
        .thenBy { it.startTime ?: "99:99" }
        .thenBy { it.priority }
        .thenBy { it.title.lowercase(Locale("tr", "TR")) }

private fun overdueTaskComparator(today: LocalDate): Comparator<TodayTaskUi> =
    compareBy<TodayTaskUi> { task ->
        runCatching { LocalDate.parse(task.plannedDate) }.getOrElse { today }
    }.thenBy { it.priority }
        .thenBy { it.title.lowercase(Locale("tr", "TR")) }
