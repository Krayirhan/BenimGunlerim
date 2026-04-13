@file:Suppress("SpellCheckingInspection")

package com.benimgunlerim.ui.today

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var addText by remember { mutableStateOf("") }
    var addNote by remember { mutableStateOf("") }
    var addTime by remember { mutableStateOf("") }
    var addCategory by remember { mutableStateOf("") }
    var addPriority by remember { mutableStateOf(2) }
    var addDateOffset by remember { mutableStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showCloseSheet by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }

    var lastDeletedTask by remember { mutableStateOf<TaskEntity?>(null) }

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

    LaunchedEffect(Unit) {
        viewModel.gameEvents.collect { event ->
            when (event) {
                is GameEvent.RewardEarned -> {
                    rewardText = "+${event.xp} XP"
                    showReward = true
                    showConfetti = true
                    confettiIntensity = ConfettiIntensity.Mini
                }
                is GameEvent.LevelUp -> {
                    levelUpLevel = event.level
                    levelUpTitle = event.title
                    showLevelUp = true
                    showConfetti = true
                    confettiIntensity = ConfettiIntensity.LevelUp
                }
                is GameEvent.AchievementUnlocked -> {
                    achievementEmoji = event.emoji
                    achievementTitle = event.title
                    showAchievement = true
                    showConfetti = true
                    confettiIntensity = ConfettiIntensity.Medium
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            AddTaskSheet(
                text = addText,
                note = addNote,
                time = addTime,
                category = addCategory,
                priority = addPriority,
                dateOffset = addDateOffset,
                onTextChange = { addText = it },
                onNoteChange = { addNote = it },
                onTimeChange = { addTime = it.sanitizedTimeInput() },
                onCategoryChange = { addCategory = it },
                onPriorityChange = { addPriority = it },
                onDateOffsetChange = { addDateOffset = it },
                onSave = {
                    if (addText.isNotBlank()) {
                        viewModel.addTask(
                            title = addText,
                            note = addNote,
                            date = LocalDate.now().plusDays(addDateOffset.toLong()),
                            startTime = addTime.takeIf { it.isNotBlank() },
                            category = addCategory.takeIf { it.isNotBlank() },
                            priority = addPriority,
                            reminderTime = addTime.takeIf { it.isNotBlank() },
                        )
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        addText = ""
                        addNote = ""
                        addTime = ""
                        addCategory = ""
                        addPriority = 2
                        addDateOffset = 0
                        showAddSheet = false
                    }
                },
            )
        }
    }

    selectedTask?.let { task ->
        val subtasks by viewModel.subTasksFlow(task.id).collectAsState(initial = emptyList())
        ModalBottomSheet(onDismissRequest = { selectedTask = null }) {
            TaskDetailSheet(
                task = task,
                subtasks = subtasks,
                onSave = { title, note, date, time, category, priority ->
                    viewModel.updateTask(task, title, note, date, time, category, priority, time)
                    selectedTask = null
                },
                onMoveTomorrow = {
                    viewModel.moveTaskToTomorrow(task)
                    selectedTask = null
                    scope.launch { snackbarHost.showSnackbar("Görev yarına taşındı") }
                },
                onDelete = {
                    lastDeletedTask = task
                    viewModel.deleteTask(task)
                    selectedTask = null
                    scope.launch {
                        val result = snackbarHost.showSnackbar(
                            message = "Görev silindi",
                            actionLabel = "Geri Al",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            lastDeletedTask?.let { viewModel.restoreTask(it) }
                        }
                    }
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
                completedCount = state.tasks.count { it.completionState == "completed" } + state.routines.count { it.id in state.completedRoutineIds },
                totalCount = state.tasks.size + state.routines.size,
                progress = state.progress,
                overdueCount = state.overdueTasks.size,
                onSave = { mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks ->
                    viewModel.saveDailySummary(note, mood, energy, bestMoment, challenge, tomorrowIntention)
                    if (carryTasks) viewModel.carryTasksToTomorrow()
                    showCloseSheet = false
                    scope.launch { snackbarHost.showSnackbar("Gün kaydedildi") }
                },
            )
        }
    }

    val completedTasks = state.tasks.count { it.completionState == "completed" }
    val completedRoutines = state.routines.count { it.id in state.completedRoutineIds }
    val total = state.tasks.size + state.routines.size
    val completed = completedTasks + completedRoutines
    Box(Modifier.fillMaxSize().testTag(com.benimgunlerim.ui.TestTags.TodayRoot)) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHost) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = CandyPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag(com.benimgunlerim.ui.TestTags.TodayFab),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Ekle")
                }
            },
        ) { padding ->
            if (state.isLoading) {
                Box(
                    Modifier.fillMaxSize().background(HomeBackground()).padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = CandyPrimary)
                }
            } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBackground())
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 106.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "hero") { HeroCard(state.currentStreak, state.gameState.happiness) }
                item(key = "dayScore") { DayScoreCard(completed, total, state.progress, completedRoutines, state.routines.size, completedTasks, state.tasks.size) }
                item(key = "routines") {
                    RoutinesCard(
                        routines = state.routines,
                        completedRoutineIds = state.completedRoutineIds,
                        completionLogs = state.completionLogs,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleRoutine(it, completedToday = it.id in state.completedRoutineIds)
                        },
                        onProgressChange = { routine, value, wasCompleted ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.updateRoutineProgress(routine, value, wasCompleted)
                        },
                    )
                }
                item(key = "tasks") {
                    TasksCard(
                        tasks = state.tasks,
                        onToggle = { task ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val wasPending = task.completionState != "completed"
                            viewModel.toggleTask(task)
                            if (wasPending) {
                                scope.launch {
                                    val result = snackbarHost.showSnackbar(
                                        message = "Görev tamamlandı",
                                        actionLabel = "Geri Al",
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoTaskToggle(task.id)
                                    }
                                }
                            }
                        },
                        onAdd = { showAddSheet = true },
                        onOpen = { selectedTask = it },
                        onSwipeDelete = { task ->
                            lastDeletedTask = task
                            viewModel.deleteTask(task)
                            scope.launch {
                                val result = snackbarHost.showSnackbar(
                                    message = "Görev silindi",
                                    actionLabel = "Geri Al",
                                    duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    lastDeletedTask?.let { viewModel.restoreTask(it) }
                                }
                            }
                        },
                    )
                }
                if (state.overdueTasks.isNotEmpty()) {
                    item(key = "overdue") {
                        OverdueTasksCard(
                            tasks = state.overdueTasks,
                            onToggle = { viewModel.toggleTask(it) },
                            onOpen = { selectedTask = it },
                            onMoveToday = { viewModel.moveTaskToDate(it, LocalDate.now()) },
                        )
                    }
                }
                item(key = "rewards") { RewardsCard(state.currentStreak, completed, state.progress) }
                item(key = "closeDay") {
                    CloseDayCard(
                        completed = completed,
                        total = total,
                        progress = state.progress,
                        isClosed = dayIsClosed,
                        mood = state.todayState?.mood,
                    ) { showCloseSheet = true }
                }
            }
            } // end if (!isLoading)
        }

        ConfettiOverlay(showConfetti, confettiIntensity) { showConfetti = false }
        FloatingRewardText(rewardText, showReward, Modifier.align(Alignment.Center)) { showReward = false }
        LevelUpOverlay(showLevelUp, levelUpLevel, levelUpTitle) { showLevelUp = false }
        AchievementUnlockOverlay(showAchievement, achievementEmoji, achievementTitle) { showAchievement = false }
    }
}

@Composable
private fun HomeBackground(): Brush = Brush.verticalGradient(
    listOf(
        Color(0xFFEAF8F2),
        Color(0xFFF0EEFF),
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.background,
    ),
)

@Composable
private fun HeroCard(streak: Int, happiness: Int) {
    val date = remember {
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("tr", "TR")))
            .replaceFirstChar { it.titlecase(Locale("tr", "TR")) }
    }
    SurfaceCard(radius = 28.dp, padding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Bugün", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text(date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Rutinlerini koru, bugünün görevlerini net bitir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Pill(if (streak > 0) "$streak günlük seri aktif" else "Yeni seri başlatmaya hazır", CandyPrimary)
            }
            Box(
                Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFF3EEFF), MaterialTheme.colorScheme.surface)))
                    .border(1.dp, CandySecondary.copy(.18f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Enerji", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%$happiness", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = CandySecondary)
                }
            }
        }
    }
}

@Composable
private fun DayScoreCard(
    completed: Int,
    total: Int,
    progress: Float,
    routineDone: Int,
    routineTotal: Int,
    taskDone: Int,
    taskTotal: Int,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(800, easing = FastOutSlowInEasing), label = "dayProgress")
    val percent = (animated * 100).toInt()
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF19B97A), Color(0xFF0FA46A), Color(0xFF0E8C5C))))
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Bugünün durumu", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(.72f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("$completed / $total", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                    Text("toplam adım tamamlandı", color = Color.White.copy(.86f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScorePill("Rutin $routineDone/$routineTotal")
                        ScorePill("Görev $taskDone/$taskTotal")
                    }
                }
                ProgressRing(animated, percent)
            }
            Box(Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(99.dp)).background(Color.White.copy(.20f))) {
                Box(Modifier.fillMaxWidth(animated).height(9.dp).clip(RoundedCornerShape(99.dp)).background(Color.White.copy(.88f)))
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, percent: Int) {
    Box(Modifier.size(94.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            drawArc(Color.White.copy(.22f), -90f, 360f, false, style = stroke)
            drawArc(Color.White, -90f, progress * 360f, false, style = stroke)
        }
        Text("%$percent", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
    }
}

@Composable
private fun ScorePill(text: String) {
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(.16f)).padding(horizontal = 9.dp, vertical = 5.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
    }
}

@Composable
private fun RoutinesCard(
    routines: List<RoutineEntity>,
    completedRoutineIds: Set<String>,
    completionLogs: List<CompletionLogEntity>,
    onToggle: (RoutineEntity) -> Unit,
    onProgressChange: (RoutineEntity, Float, Boolean) -> Unit,
) {
    SectionShell(
        title = "Günlük rutinler",
        subtitle = "Her gün olması gereken alışkanlıklar. Su, yürüyüş, ilaç, okuma gibi.",
        badge = "Tekrar eder",
        color = CandyPrimary,
        actionLabel = null,
        onAction = null,
    ) {
        if (routines.isEmpty()) {
            EmptyBox("Henüz rutin yok. Her gün yapmak istediğin bir alışkanlık ekle.", CandyPrimary)
        } else {
            routines.forEach { routine ->
                val log = completionLogs.firstOrNull { it.entityType == "routine" && it.entityId == routine.id }
                RoutineRow(routine, log, routine.id in completedRoutineIds, onToggle, onProgressChange)
            }
        }
    }
}

@Composable
private fun TasksCard(
    tasks: List<TaskEntity>,
    onToggle: (TaskEntity) -> Unit,
    onAdd: () -> Unit,
    onOpen: (TaskEntity) -> Unit,
    onSwipeDelete: (TaskEntity) -> Unit,
) {
    SectionShell(
        title = "Bugünün görevleri",
        subtitle = "Sadece bugün yapmak istediğin tek seferlik işler.",
        badge = "Bugüne özel",
        color = LevelSky,
        actionLabel = "+ Görev",
        onAction = onAdd,
    ) {
        if (tasks.isEmpty()) {
            EmptyBox("Bugüne özel görev yok. Bir iş, arama veya yapılacak adım ekle.", LevelSky)
        } else {
            tasks.forEach { task ->
                SwipeableTaskRow(task = task, onToggle = onToggle, onOpen = onOpen, onDelete = onSwipeDelete)
            }
        }
    }
}

@Composable
private fun SectionShell(
    title: String,
    subtitle: String,
    badge: String,
    color: Color,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(.13f)), contentAlignment = Alignment.Center) {
                Text(title.first().toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = color)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(badge, color)
                if (actionLabel != null && onAction != null) {
                    Text(actionLabel, modifier = Modifier.clickable(onClick = onAction), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold), color = color)
                }
            }
        }
        content()
    }
}

@Composable
private fun TaskRow(task: TaskEntity, onToggle: (TaskEntity) -> Unit, onOpen: (TaskEntity) -> Unit) {
    val done = task.completionState == "completed"
    val color = categoryColor(task.category ?: task.title)
    ItemRow(done = done, color = color) {
        CheckCircle(done, color) { onToggle(task) }
        Column(Modifier.weight(1f).clickable { onOpen(task) }, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, textDecoration = if (done) TextDecoration.LineThrough else null),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (done) .52f else 1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("${task.startTime ?: "bugün"} · tek seferlik görev", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Icon(Icons.Rounded.MoreHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp).clickable { onOpen(task) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskRow(
    task: TaskEntity,
    onToggle: (TaskEntity) -> Unit,
    onOpen: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(task)
                true
            } else false
        },
        positionalThreshold = { it * 0.4f },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(StreakCoral.copy(.15f)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Row(
                    Modifier.padding(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Sil", tint = StreakCoral, modifier = Modifier.size(20.dp))
                    Text("Sil", style = MaterialTheme.typography.labelMedium, color = StreakCoral)
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        TaskRow(task = task, onToggle = onToggle, onOpen = onOpen)
    }
}

@Composable
private fun RoutineRow(
    routine: RoutineEntity,
    log: CompletionLogEntity?,
    done: Boolean,
    onToggle: (RoutineEntity) -> Unit,
    onProgressChange: (RoutineEntity, Float, Boolean) -> Unit,
) {
    val color = routine.color?.let(::parseColorOrNull) ?: CandyPrimary
    val isCheckType = routine.targetType == "check"
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ItemRow(done = done, color = color) {
            CheckCircle(done, color) { onToggle(routine) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    routine.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, textDecoration = if (done) TextDecoration.LineThrough else null),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (done) .52f else 1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCheckType && routine.targetValue != null) {
                    val current = log?.value?.toInt() ?: 0
                    Text("$current / ${routine.targetValue} ${routine.targetUnit ?: ""}".trim(), style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
                } else {
                    Text(routine.preferredTime?.let { "$it · günlük rutin" } ?: "her gün tekrar eder", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Pill(if (done) "Tamam" else "Seri", if (done) CompletedGreen else color)
        }
        if (!isCheckType && routine.targetValue != null && !done) {
            val current = log?.value ?: 0f
            val target = routine.targetValue.toFloat()
            val step = if (routine.targetType == "count") 1f else (target / 4f).coerceAtLeast(1f)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { onProgressChange(routine, (current - step).coerceAtLeast(0f), done) },
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp),
                ) { Text("-", style = MaterialTheme.typography.titleMedium, color = color) }
                Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(99.dp)).background(color.copy(.12f))) {
                    Box(Modifier.fillMaxWidth((current / target).coerceIn(0f, 1f)).height(6.dp).clip(RoundedCornerShape(99.dp)).background(color))
                }
                TextButton(
                    onClick = { onProgressChange(routine, (current + step).coerceAtMost(target), done) },
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp),
                ) { Text("+", style = MaterialTheme.typography.titleMedium, color = color) }
            }
        }
    }
}

@Composable
private fun ItemRow(done: Boolean, color: Color, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (done) .48f else 1f)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(5.dp).height(72.dp).background(color.copy(alpha = if (done) .35f else 1f)))
        Row(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun CheckCircle(done: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(32.dp).clip(CircleShape).background(if (done) color else color.copy(.10f)).border(2.dp, color.copy(if (done) 1f else .45f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(Icons.Rounded.Check, contentDescription = "Tamamlandı", tint = Color.White, modifier = Modifier.size(19.dp))
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
            Icon(Icons.Rounded.Add, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OverdueTasksCard(
    tasks: List<TaskEntity>,
    onToggle: (TaskEntity) -> Unit,
    onOpen: (TaskEntity) -> Unit,
    onMoveToday: (TaskEntity) -> Unit,
) {
    SectionShell(
        title = "Geciken görevler",
        subtitle = "${tasks.size} tamamlanmamış görev bulundu.",
        badge = "Gecikiyor",
        color = StreakCoral,
        actionLabel = null,
        onAction = null,
    ) {
        tasks.forEach { task ->
            ItemRow(done = task.completionState == "completed", color = StreakCoral) {
                CheckCircle(done = task.completionState == "completed", color = StreakCoral) { onToggle(task) }
                Column(
                    modifier = Modifier.weight(1f).clickable { onOpen(task) },
                ) {
                    Text(task.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    if (!task.plannedDate.isNullOrBlank()) {
                        Text(task.plannedDate!!, style = MaterialTheme.typography.labelSmall, color = StreakCoral.copy(alpha = 0.8f))
                    }
                }
                IconButton(onClick = { onMoveToday(task) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "Bugüne taşı", tint = StreakCoral, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun RewardsCard(streak: Int, completed: Int, progress: Float) {
    SurfaceCard(radius = 24.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Küçük kazanımlar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                RewardTile("$streak gün", "Seri", CandyPrimary, Modifier.weight(1f))
                RewardTile("$completed", "Adım", CompletedGreen, Modifier.weight(1f))
                RewardTile("%${(progress * 100).toInt()}", "Bugün", CandySecondary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RewardTile(value: String, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier.height(82.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(.10f)).border(1.dp, color.copy(.16f), RoundedCornerShape(20.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CloseDayCard(completed: Int, total: Int, progress: Float, isClosed: Boolean, mood: String?, onClick: () -> Unit) {
    if (isClosed) {
        // Day is closed — show summary state
        val moodColor = when (mood) {
            "harika" -> CompletedGreen; "iyi" -> CandyPrimary; "kotu", "cok_kotu" -> StreakCoral; else -> CandySecondary
        }
        val moodLabel = when (mood) {
            "harika" -> "Harika"; "iyi" -> "İyi"; "normal" -> "Normal"; "kotu" -> "Kötü"; "cok_kotu" -> "Çok kötü"; else -> "Kapatıldı"
        }
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(CompletedGreen.copy(.08f), CompletedGreen.copy(.03f))))
                .border(1.dp, CompletedGreen.copy(.22f), RoundedCornerShape(28.dp)).padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill("Gün kapatıldı", CompletedGreen)
                    Pill(moodLabel, moodColor)
                }
                Text("Bugün tamamlandı", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text("$completed / $total adım tamamlandı. XP zaten kazanıldı.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(40.dp)) {
                    Text("Özeti güncelle", color = CompletedGreen, maxLines = 1)
                }
            }
        }
    } else {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(CandySecondary.copy(.08f), CandySecondary.copy(.03f))))
                .border(1.dp, CandySecondary.copy(.18f), RoundedCornerShape(28.dp)).padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Pill("Gün sonu", CandySecondary)
                Text("Günü kapat", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text("Bugün $completed / $total adım tamamlandı. Günü değerlendir.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary.copy(.10f))) {
                    Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary))
                }
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(CandySecondary, Color.White)) {
                    Text("Günü değerlendir", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun AddTaskSheet(
    text: String,
    note: String,
    time: String,
    category: String,
    priority: Int,
    dateOffset: Int,
    onTextChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onDateOffsetChange: (Int) -> Unit,
    onSave: () -> Unit,
) {
    val priorityLabels = listOf("Yüksek", "Normal", "Düşük")
    val dateLabels = listOf("Bugün", "Yarın", "+2 gün")
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Yeni görev", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        OutlinedTextField(value = text, onValueChange = onTextChange, label = { Text("Görev adı") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp))
        OutlinedTextField(value = note, onValueChange = onNoteChange, label = { Text("Not (isteğe bağlı)") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = time, onValueChange = onTimeChange, label = { Text("Saat") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
            OutlinedTextField(value = category, onValueChange = onCategoryChange, label = { Text("Kategori") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Öncelik", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEachIndexed { i, p ->
                    val sel = priority == p
                    val col = if (i == 0) StreakCoral else if (i == 1) CandyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (sel) col.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (sel) col else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { onPriorityChange(p) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(priorityLabels[i], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) col else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Tarih", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2).forEach { offset ->
                    val sel = dateOffset == offset
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (sel) CandySecondary.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (sel) CandySecondary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { onDateOffsetChange(offset) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(dateLabels[offset], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) CandySecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Button(onClick = onSave, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White)) {
            Text("Kaydet")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TaskDetailSheet(
    task: TaskEntity,
    subtasks: List<SubTaskEntity>,
    onSave: (String, String?, LocalDate, String?, String?, Int) -> Unit,
    onMoveTomorrow: () -> Unit,
    onDelete: () -> Unit,
    onAddSubTask: (String) -> Unit,
    onToggleSubTask: (SubTaskEntity) -> Unit,
    onDeleteSubTask: (SubTaskEntity) -> Unit,
) {
    var title by remember { mutableStateOf(task.title) }
    var note by remember { mutableStateOf(task.note ?: "") }
    var time by remember { mutableStateOf(task.startTime ?: "") }
    var category by remember { mutableStateOf(task.category ?: "") }
    var priority by remember { mutableStateOf(task.priority) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newSubTaskText by remember { mutableStateOf("") }
    val priorityLabels = listOf("Yüksek", "Normal", "Düşük")

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Görevi sil?") },
            text = { Text("\"${task.title}\" görevi kalıcı olarak silinecek. Kısa süre içinde geri alabilirsin.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Sil", color = StreakCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Vazgeç")
                }
            },
        )
    }
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Görevi düzenle", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Görev adı") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp))
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = time, onValueChange = { time = it.sanitizedTimeInput() }, label = { Text("Saat") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Öncelik", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEachIndexed { i, p ->
                    val sel = priority == p
                    val col = if (i == 0) StreakCoral else if (i == 1) CandyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (sel) col.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (sel) col else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { priority = p }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(priorityLabels[i], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) col else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        // ── Alt Görevler ─────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Alt görevler", style = MaterialTheme.typography.labelLarge)
            if (subtasks.isEmpty()) {
                Text("Henüz alt görev yok.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                subtasks.forEach { st ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            Modifier.size(22.dp).clip(CircleShape)
                                .background(if (st.isCompleted) CandyPrimary else CandyPrimary.copy(.12f))
                                .border(1.5.dp, CandyPrimary.copy(if (st.isCompleted) 1f else .4f), CircleShape)
                                .clickable { onToggleSubTask(st) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (st.isCompleted) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            st.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = if (st.isCompleted) TextDecoration.LineThrough else null,
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (st.isCompleted) .5f else 1f),
                        )
                        IconButton(onClick = { onDeleteSubTask(st) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Sil", tint = StreakCoral, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = newSubTaskText,
                    onValueChange = { newSubTaskText = it },
                    label = { Text("Alt görev ekle") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                )
                IconButton(
                    onClick = {
                        if (newSubTaskText.isNotBlank()) {
                            onAddSubTask(newSubTaskText)
                            newSubTaskText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(CandyPrimary),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Ekle", tint = Color.White)
                }
            }
        }
        Button(
            onClick = { onSave(title, note.takeIf { it.isNotBlank() }, LocalDate.parse(task.plannedDate), time.takeIf { it.isNotBlank() }, category.takeIf { it.isNotBlank() }, priority) },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White),
        ) { Text("Kaydet") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedSmallButton("Yarına taşı", Modifier.weight(1f), onMoveTomorrow)
            OutlinedSmallButton("Sil", Modifier.weight(1f)) { showDeleteConfirm = true }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun CloseDaySheet(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    overdueCount: Int,
    onSave: (mood: Int, energy: Int, note: String, bestMoment: String, challenge: String, tomorrowIntention: String, carryTasks: Boolean) -> Unit,
) {
    var step by remember { mutableStateOf(0) }
    var mood by remember { mutableStateOf(3) }
    var energy by remember { mutableStateOf(3) }
    var note by remember { mutableStateOf("") }
    var bestMoment by remember { mutableStateOf("") }
    var challenge by remember { mutableStateOf("") }
    var tomorrowIntention by remember { mutableStateOf("") }
    var carryTasks by remember { mutableStateOf(overdueCount > 0) }

    val moodLabels = listOf("Çok kötü", "Kötü", "Normal", "İyi", "Harika")
    val moodColors = listOf(StreakCoral, StreakCoral.copy(.65f), CandySecondary, CandyPrimary, CompletedGreen)

    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Step indicator
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (i <= step) CandySecondary else MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }

        when (step) {
            0 -> {
                // Step 1: Summary
                Text("Bugünün özeti", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryTile("$completedCount / $totalCount", "Tamamlandı", CandyPrimary, Modifier.weight(1f))
                    SummaryTile("%${(progress * 100).toInt()}", "Başarı", CompletedGreen, Modifier.weight(1f))
                    if (overdueCount > 0) SummaryTile("$overdueCount", "Geciken", StreakCoral, Modifier.weight(1f))
                }
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary.copy(.10f))) {
                    Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary))
                }
            }
            1 -> {
                // Step 2: Mood + Energy
                Text("Bugün nasıl hissettin?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ruh hali", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        moodColors.forEachIndexed { i, col ->
                            Box(
                                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (mood == i) col else col.copy(.14f))
                                    .border(1.dp, if (mood == i) col else col.copy(.20f), RoundedCornerShape(10.dp))
                                    .clickable { mood = i },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(moodLabels[i], style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (mood == i) FontWeight.ExtraBold else FontWeight.Normal), color = if (mood == i) Color.White else col, maxLines = 1)
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enerji seviyesi (1-5)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3, 4, 5).forEach { e ->
                            Box(
                                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (energy == e) LevelSky else LevelSky.copy(.12f))
                                    .border(1.dp, if (energy == e) LevelSky else LevelSky.copy(.20f), RoundedCornerShape(10.dp))
                                    .clickable { energy = e },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("$e", style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (energy == e) FontWeight.ExtraBold else FontWeight.Normal), color = if (energy == e) Color.White else LevelSky)
                            }
                        }
                    }
                }
            }
            2 -> {
                // Step 3: Reflection
                Text("Bugünü yansıt", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                OutlinedTextField(value = bestMoment, onValueChange = { bestMoment = it }, label = { Text("Bugünün en iyi şeyi") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = challenge, onValueChange = { challenge = it }, label = { Text("Zorlayan bir şey vardı mı?") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Bugünden kısa bir not") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
            }
            3 -> {
                // Step 4: Tomorrow
                Text("Yarına hazırlan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                OutlinedTextField(value = tomorrowIntention, onValueChange = { tomorrowIntention = it }, label = { Text("Yarın için tek niyet") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
                if (overdueCount > 0) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(if (carryTasks) StreakCoral.copy(.10f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (carryTasks) StreakCoral.copy(.25f) else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { carryTasks = !carryTasks }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(if (carryTasks) StreakCoral else MaterialTheme.colorScheme.outline.copy(.5f)), contentAlignment = Alignment.Center) {
                            if (carryTasks) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("$overdueCount geciken görevi yarına taşı", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Tamamlanmamış görevler yarın listene eklenir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Navigation buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) {
                OutlinedSmallButton("Geri", Modifier.weight(1f)) { step-- }
            }
            if (step < 3) {
                Button(onClick = { step++ }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(CandySecondary, Color.White)) {
                    Text("Devam")
                }
            } else {
                Button(
                    onClick = { onSave(mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(CandySecondary, Color.White),
                ) { Text("Günü kaydet") }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SummaryTile(value: String, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier.height(72.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(.10f)).border(1.dp, color.copy(.16f), RoundedCornerShape(16.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SurfaceCard(radius: androidx.compose.ui.unit.Dp, padding: androidx.compose.ui.unit.Dp, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.fillMaxWidth().padding(padding), content = content)
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

@Composable
private fun OutlinedSmallButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(42.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(text, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun categoryColor(seed: String): Color {
    val value = seed.lowercase(Locale.ROOT)
    return when {
        value.contains("su") || value.contains("spor") || value.contains("sağ") -> CandyPrimary
        value.contains("oku") || value.contains("öğren") || value.contains("geli") -> CandySecondary
        value.contains("iş") || value.contains("sunum") || value.contains("odak") -> LevelSky
        value.contains("kişisel") || value.contains("not") || value.contains("akşam") -> StreakCoral
        else -> listOf(CandyPrimary, CandySecondary, XpGold, LevelSky, StreakCoral)[abs(seed.hashCode()) % 5]
    }
}

private fun parseColorOrNull(raw: String): Color? = runCatching {
    val hex = raw.removePrefix("#")
    Color(("FF$hex").takeLast(8).toLong(16))
}.getOrNull()

private fun String.sanitizedTimeInput(): String = filter { it.isDigit() || it == ':' }.take(5)
