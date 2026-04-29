@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(viewModel: PlanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val today = viewModel.today()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val taskAdded = stringResource(R.string.plan_task_added)
    val taskDeleted = stringResource(R.string.plan_task_deleted)

    var showAddSheet by remember { mutableStateOf(false) }
    var addText by remember { mutableStateOf("") }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    stringResource(R.string.plan_add_task_sheet_title, state.selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("tr", "TR")))),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                OutlinedTextField(
                    value = addText,
                    onValueChange = { addText = it },
                    label = { Text(stringResource(R.string.plan_task_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { showAddSheet = false }) { Text(stringResource(R.string.action_cancel)) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            viewModel.addTask(addText, state.selectedDate)
                            addText = ""
                            showAddSheet = false
                            scope.launch { snackbarHost.showSnackbar(taskAdded) }
                        },
                    ) { Text(stringResource(R.string.action_save)) }
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = CandyPrimary,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.plan_add_task_cd))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF7F6FF), Color(0xFFFFFFFF))))
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 106.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { PlanHeroCard(state.selectedDate, today) }
            item {
                WeekDatePicker(
                    weekStart = state.weekStart,
                    selectedDate = state.selectedDate,
                    today = today,
                    onSelectDate = { viewModel.selectDate(it) },
                )
            }
            item {
                DayTasksCard(
                    date = state.selectedDate,
                    tasks = state.tasksForDay,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleTask(it)
                    },
                    onDelete = {
                        viewModel.deleteTask(it)
                        scope.launch { snackbarHost.showSnackbar(taskDeleted) }
                    },
                    onAdd = { showAddSheet = true },
                )
            }
            if (state.overdueTasks.isNotEmpty()) {
                item {
                    PlanOverdueCard(
                        tasks = state.overdueTasks,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleTask(it)
                        },
                        onMoveToSelected = { viewModel.moveTaskToDate(it, state.selectedDate) },
                        onDelete = {
                            viewModel.deleteTask(it)
                            scope.launch { snackbarHost.showSnackbar(taskDeleted) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanHeroCard(selectedDate: LocalDate, today: LocalDate) {
    val todayLabel = stringResource(R.string.label_today)
    val tomorrowLabel = stringResource(R.string.label_tomorrow)
    val yesterdayLabel = stringResource(R.string.label_yesterday)
    val label = when (selectedDate) {
        today -> todayLabel
        today.plusDays(1) -> tomorrowLabel
        today.minusDays(1) -> yesterdayLabel
        else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale("tr", "TR")))
            .replaceFirstChar { it.titlecase(Locale("tr", "TR")) }
    }
    val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr", "TR")))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.plan_title), style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
            Text(dateStr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CandyPrimary.copy(.10f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CandyPrimary)
            }
        }
    }
}

private val turkishDays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

@Composable
private fun WeekDatePicker(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items((0..6).map { weekStart.plusDays(it.toLong()) }) { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            val dayLabel = turkishDays[date.dayOfWeek.value - 1]
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            isSelected -> CandyPrimary
                            isToday -> CandyPrimary.copy(.10f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                    )
                    .border(
                        width = if (isToday && !isSelected) 1.dp else 0.dp,
                        color = if (isToday && !isSelected) CandyPrimary.copy(.30f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clickable { onSelectDate(date) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DayTasksCard(
    date: LocalDate,
    tasks: List<PlanTaskUi>,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val dateLabel = date.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR")))
    val daySectionTitle = stringResource(R.string.plan_day_section_title, dateLabel)
    val taskCount = stringResource(R.string.plan_task_count, tasks.size)
    val addAction = stringResource(R.string.plan_add_action)
    val emptyState = stringResource(R.string.plan_empty_state)
    PlanSectionShell(
        title = daySectionTitle,
        badge = taskCount,
        color = LevelSky,
        actionLabel = addAction,
        onAction = onAdd,
    ) {
        if (tasks.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LevelSky.copy(.08f))
                    .border(1.dp, LevelSky.copy(.14f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text(
                    emptyState,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            tasks.forEach { task ->
                PlanTaskRow(task = task, accentColor = LevelSky, onToggle = onToggle, onDelete = onDelete, secondaryAction = null)
            }
        }
    }
}

@Composable
private fun PlanOverdueCard(
    tasks: List<PlanTaskUi>,
    onToggle: (String) -> Unit,
    onMoveToSelected: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val overdueTitle = stringResource(R.string.plan_overdue_title)
    val overdueCount = stringResource(R.string.plan_overdue_count, tasks.size)
    PlanSectionShell(
        title = overdueTitle,
        badge = overdueCount,
        color = StreakCoral,
        actionLabel = null,
        onAction = null,
    ) {
        tasks.forEach { task ->
            PlanTaskRow(
                task = task,
                accentColor = StreakCoral,
                onToggle = onToggle,
                onDelete = onDelete,
                secondaryAction = { onMoveToSelected(task.id) },
            )
        }
    }
}

@Composable
private fun PlanTaskRow(
    task: PlanTaskUi,
    accentColor: Color,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    secondaryAction: (() -> Unit)?,
) {
    val done = task.isCompleted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (done) accentColor else accentColor.copy(.10f))
                .border(1.5.dp, accentColor.copy(if (done) 0f else .40f), CircleShape)
                .clickable { onToggle(task.id) },
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                ),
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!task.plannedDate.isNullOrBlank()) {
                Text(
                    task.plannedDate!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.8f),
                )
            }
        }
        if (secondaryAction != null) {
            IconButton(onClick = secondaryAction, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = stringResource(R.string.plan_move_to_selected_day_cd), tint = accentColor, modifier = Modifier.size(18.dp))
            }
        }
        IconButton(onClick = { onDelete(task.id) }, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PlanSectionShell(
    title: String,
    badge: String,
    color: Color,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(.12f)).padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(badge, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
                    }
                }
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(actionLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
                    }
                }
            }
            content()
        }
    }
}
