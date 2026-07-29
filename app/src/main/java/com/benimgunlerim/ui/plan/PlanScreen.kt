@file:Suppress(
    "SpellCheckingInspection",
    "LongParameterList",
    "LongMethod",
    "CyclomaticComplexMethod",
    "MagicNumber",
)
package com.benimgunlerim.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.benimgunlerim.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.domain.validation.TimeInputValidator
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(viewModel: PlanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val today = viewModel.today()
    val snackbarHost = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val taskAdded = stringResource(R.string.plan_task_added)
    val taskDeleted = stringResource(R.string.plan_task_deleted)
    val overdueMoved = stringResource(R.string.plan_overdue_moved_count)
    val genericError = stringResource(R.string.today_error_generic)
    val actionUndo = stringResource(R.string.action_undo)

    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var addText by rememberSaveable { mutableStateOf("") }
    var addNote by rememberSaveable { mutableStateOf("") }
    var addTime by rememberSaveable { mutableStateOf("") }
    var addCategory by rememberSaveable { mutableStateOf("") }
    var addPriority by rememberSaveable { mutableStateOf(2) }
    var addReminderEnabled by rememberSaveable { mutableStateOf(false) }
    var addDateText by rememberSaveable { mutableStateOf(state.selectedDate.toString()) }
    val addDate = remember(addDateText, state.selectedDate) {
        runCatching { LocalDate.parse(addDateText) }.getOrElse { state.selectedDate }
    }
    var addAttempted by rememberSaveable { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    val editingTask = remember(editingTaskId, state.tasksForDay, state.overdueTasks) {
        (state.tasksForDay + state.overdueTasks).firstOrNull { it.id == editingTaskId }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                PlanUiEffect.TaskAdded -> snackbarHost.showSnackbar(taskAdded)
                is PlanUiEffect.TaskDeleted -> {
                    val result = snackbarHost.showSnackbar(
                        message = taskDeleted,
                        actionLabel = actionUndo,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask(effect.taskId)
                    }
                }
                is PlanUiEffect.OverdueTasksMoved -> {
                    snackbarHost.showSnackbar(overdueMoved.format(effect.count))
                }
                PlanUiEffect.ActionFailed -> snackbarHost.showSnackbar(genericError)
            }
        }
    }

    LaunchedEffect(showAddSheet, state.selectedDate) {
        if (showAddSheet) {
            addDateText = state.selectedDate.toString()
            addAttempted = false
        }
    }

    LaunchedEffect(editingTaskId, editingTask) {
        if (editingTaskId != null && editingTask == null) {
            editingTaskId = null
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            PlanAddTaskSheet(
                today = today,
                selectedDate = state.selectedDate,
                addDate = addDate,
                addText = addText,
                addNote = addNote,
                addTime = addTime,
                addCategory = addCategory,
                addPriority = addPriority,
                addReminderEnabled = addReminderEnabled,
                addAttempted = addAttempted,
                onDateChange = { addDateText = it.toString() },
                onTextChange = { addText = it },
                onNoteChange = { addNote = it },
                onTimeChange = {
                    addTime = TimeInputValidator.sanitize(it)
                    if (addTime.isBlank()) addReminderEnabled = false
                },
                onCategoryChange = { addCategory = it },
                onPriorityChange = { addPriority = it },
                onReminderEnabledChange = { addReminderEnabled = it },
                onCancel = { showAddSheet = false },
                onSave = {
                    addAttempted = true
                    val startTime = addTime.takeIf { it.isNotBlank() }
                    val reminderTime = if (addReminderEnabled && startTime != null) startTime else null
                    if (addText.trim().isEmpty() || addText.trim().length > 80 || !TimeInputValidator.isValid(addTime)) {
                        return@PlanAddTaskSheet
                    }
                    viewModel.addTask(
                        title = addText.take(80),
                        date = addDate,
                        note = addNote,
                        startTime = startTime,
                        category = addCategory,
                        priority = addPriority,
                        reminderTime = reminderTime,
                    )
                    addText = ""
                    addNote = ""
                    addTime = ""
                    addCategory = ""
                    addPriority = 2
                    addReminderEnabled = false
                    addDateText = state.selectedDate.toString()
                    addAttempted = false
                    showAddSheet = false
                },
            )
        }
    }

    if (editingTask != null) {
        ModalBottomSheet(onDismissRequest = { editingTaskId = null }) {
            PlanEditTaskSheet(
                task = editingTask,
                today = today,
                onCancel = { editingTaskId = null },
                onSave = { title, note, date, startTime, category, priority, reminderTime ->
                    viewModel.updateTask(
                        taskId = editingTask.id,
                        title = title,
                        note = note,
                        date = date,
                        startTime = startTime,
                        category = category,
                        priority = priority,
                        reminderTime = reminderTime,
                    )
                    editingTaskId = null
                },
                onDelete = {
                    viewModel.deleteTask(editingTask.id)
                    editingTaskId = null
                },
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.toPickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.selectDate(millis.toLocalDate())
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        stringResource(R.string.plan_pick_date_title),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    )
                },
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier.testTag(TestTags.PlanFab),
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
                .testTag(TestTags.PlanRoot)
                .background(com.benimgunlerim.ui.components.ScreenBackground())
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 106.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (state.snapshotLoadError) {
                    PlanSnapshotErrorBanner(onRetry = viewModel::retrySnapshotLoad)
                }
            }
            item {
                PlanHeroCard(
                    selectedDate = state.selectedDate,
                    today = today,
                    taskCount = state.tasksForDay.size,
                    completedCount = state.tasksForDay.count { it.isCompleted },
                    overdueCount = state.overdueTasks.size,
                )
            }
            item {
                PlanDateNavigationRow(
                    weekStart = state.weekStart,
                    selectedDate = state.selectedDate,
                    today = today,
                    onPreviousWeek = viewModel::selectPreviousWeek,
                    onNextWeek = viewModel::selectNextWeek,
                    onToday = viewModel::selectToday,
                    onPickDate = { showDatePicker = true },
                )
            }
            item {
                WeekDatePicker(
                    weekStart = state.weekStart,
                    selectedDate = state.selectedDate,
                    today = today,
                    taskCounts = state.weeklyTaskCounts,
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
                    },
                    onOpen = { editingTaskId = it },
                    onAdd = { showAddSheet = true },
                )
            }
            if (state.overdueTasks.isNotEmpty()) {
                item {
                    PlanOverdueCard(
                        tasks = state.overdueTasks,
                        today = today,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleTask(it)
                        },
                        onMoveToSelected = { viewModel.moveTaskToDate(it, state.selectedDate) },
                        onMoveAllToday = { viewModel.moveOverdueTasksToDate(today) },
                        onMoveAllTomorrow = { viewModel.moveOverdueTasksToDate(today.plusDays(1)) },
                        onMoveAllSelected = { viewModel.moveOverdueTasksToDate(state.selectedDate) },
                        onDelete = {
                            viewModel.deleteTask(it)
                        },
                        onOpen = { editingTaskId = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanSnapshotErrorBanner(onRetry: () -> Unit) {
    com.benimgunlerim.ui.components.AlertCard(
        modifier = Modifier.testTag(TestTags.PlanSnapshotErrorBanner),
        title = stringResource(R.string.plan_snapshot_load_error),
        body = "",
        primaryCtaLabel = stringResource(R.string.today_retry),
        onPrimaryCtaClick = onRetry,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanAddTaskSheet(
    today: LocalDate,
    selectedDate: LocalDate,
    addDate: LocalDate,
    addText: String,
    addNote: String,
    addTime: String,
    addCategory: String,
    addPriority: Int,
    addReminderEnabled: Boolean,
    addAttempted: Boolean,
    onDateChange: (LocalDate) -> Unit,
    onTextChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val dateFmt = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr", "TR")) }
    val sheetTitle = stringResource(
        R.string.plan_add_task_sheet_title,
        addDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("tr", "TR"))),
    )
    val timeErrorText = when (TimeInputValidator.validationMessageKey(addTime)) {
        TimeInputValidator.TimeValidation.Ok -> null
        TimeInputValidator.TimeValidation.Incomplete -> stringResource(R.string.today_time_incomplete)
        TimeInputValidator.TimeValidation.InvalidFormat,
        TimeInputValidator.TimeValidation.InvalidClock,
        -> stringResource(R.string.today_time_invalid)
    }
    val reminderCanBeEnabled = TimeInputValidator.isValid(addTime) && addTime.isNotBlank()
    val effectiveReminderEnabled = addReminderEnabled && reminderCanBeEnabled
    val timePickerState = rememberTimePickerState(
        initialHour = addTime.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }
            ?.substring(0, 2)
            ?.toIntOrNull() ?: 9,
        initialMinute = addTime.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }
            ?.substring(3, 5)
            ?.toIntOrNull() ?: 0,
        is24Hour = true,
    )
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = addDate.toPickerMillis())
    val canSave = addText.trim().isNotEmpty() &&
        addText.trim().length <= 80 &&
        TimeInputValidator.isValid(addTime)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            sheetTitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = addDate == today,
                onClick = { onDateChange(today) },
                label = { Text(stringResource(R.string.label_today)) },
            )
            FilterChip(
                selected = addDate == today.plusDays(1),
                onClick = { onDateChange(today.plusDays(1)) },
                label = { Text(stringResource(R.string.label_tomorrow)) },
            )
            FilterChip(
                selected = addDate == selectedDate,
                onClick = { onDateChange(selectedDate) },
                label = { Text(selectedDate.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR")))) },
            )
        }
        OutlinedTextField(
            value = addText,
            onValueChange = onTextChange,
            label = { Text(stringResource(R.string.plan_task_title_label)) },
            supportingText = {
                Text("${addText.trim().length}/80")
            },
            isError = addAttempted && addText.trim().isEmpty(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = addTime,
            onValueChange = onTimeChange,
            label = { Text(stringResource(R.string.today_task_plan_time_label)) },
            supportingText = {
                timeErrorText?.let { Text(it, color = StreakCoral) }
            },
            isError = timeErrorText != null,
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = stringResource(R.string.today_pick_time_cd),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        PlanPrioritySelector(priority = addPriority, onPriorityChange = onPriorityChange)
        OutlinedTextField(
            value = addNote,
            onValueChange = onNoteChange,
            label = { Text(stringResource(R.string.today_add_task_note_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
        )
        OutlinedTextField(
            value = addCategory,
            onValueChange = onCategoryChange,
            label = { Text(stringResource(R.string.today_add_task_category_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.today_reminder_switch_label), style = MaterialTheme.typography.bodyMedium)
                Text(
                    when {
                        !reminderCanBeEnabled -> stringResource(R.string.today_reminder_switch_desc_needs_time)
                        effectiveReminderEnabled -> stringResource(R.string.today_reminder_switch_desc_on)
                        else -> stringResource(R.string.today_reminder_switch_desc_off)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = effectiveReminderEnabled,
                enabled = reminderCanBeEnabled,
                onCheckedChange = onReminderEnabledChange,
            )
        }
        TextButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.today_pick_custom_date, addDate.format(dateFmt)))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                enabled = canSave,
                onClick = onSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute))
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateChange(millis.toLocalDate())
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanEditTaskSheet(
    task: PlanTaskUi,
    today: LocalDate,
    onCancel: () -> Unit,
    onSave: (String, String?, LocalDate, String?, String?, Int, String?) -> Unit,
    onDelete: () -> Unit,
) {
    var title by rememberSaveable(task.id) { mutableStateOf(task.title) }
    var note by rememberSaveable(task.id) { mutableStateOf(task.note.orEmpty()) }
    var dateText by rememberSaveable(task.id) { mutableStateOf(task.plannedDate ?: today.toString()) }
    val date = remember(dateText, today) {
        runCatching { LocalDate.parse(dateText) }.getOrElse { today }
    }
    var time by rememberSaveable(task.id) { mutableStateOf(task.startTime.orEmpty()) }
    var category by rememberSaveable(task.id) { mutableStateOf(task.category.orEmpty()) }
    var priority by rememberSaveable(task.id) { mutableStateOf(task.priority) }
    var reminderEnabled by rememberSaveable(task.id) { mutableStateOf(task.reminderTime != null) }
    var showTimePicker by rememberSaveable(task.id) { mutableStateOf(false) }
    var showDatePicker by rememberSaveable(task.id) { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val dateFmt = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr", "TR")) }
    val timeErrorText = when (TimeInputValidator.validationMessageKey(time)) {
        TimeInputValidator.TimeValidation.Ok -> null
        TimeInputValidator.TimeValidation.Incomplete -> stringResource(R.string.today_time_incomplete)
        TimeInputValidator.TimeValidation.InvalidFormat,
        TimeInputValidator.TimeValidation.InvalidClock,
        -> stringResource(R.string.today_time_invalid)
    }
    val reminderCanBeEnabled = TimeInputValidator.isValid(time) && time.isNotBlank()
    val effectiveReminderEnabled = reminderEnabled && reminderCanBeEnabled
    val timePickerState = rememberTimePickerState(
        initialHour = time.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }
            ?.substring(0, 2)
            ?.toIntOrNull() ?: 9,
        initialMinute = time.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }
            ?.substring(3, 5)
            ?.toIntOrNull() ?: 0,
        is24Hour = true,
    )
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toPickerMillis())
    val canSave = title.trim().isNotEmpty() && title.trim().length <= 80 && TimeInputValidator.isValid(time)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            stringResource(R.string.today_edit_task_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.plan_task_title_label)) },
            supportingText = { Text("${title.trim().length}/80") },
            isError = title.trim().isEmpty() || title.trim().length > 80,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        TextButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.today_pick_custom_date, date.format(dateFmt)))
        }
        OutlinedTextField(
            value = time,
            onValueChange = {
                time = TimeInputValidator.sanitize(it)
                if (time.isBlank()) reminderEnabled = false
            },
            label = { Text(stringResource(R.string.today_task_plan_time_label)) },
            supportingText = {
                timeErrorText?.let { Text(it, color = StreakCoral) }
            },
            isError = timeErrorText != null,
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Outlined.Schedule, contentDescription = stringResource(R.string.today_pick_time_cd))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        PlanPrioritySelector(priority = priority, onPriorityChange = { priority = it })
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.today_edit_task_note_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
        )
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text(stringResource(R.string.today_add_task_category_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.today_reminder_switch_label), style = MaterialTheme.typography.bodyMedium)
                Text(
                    when {
                        !reminderCanBeEnabled -> stringResource(R.string.today_reminder_switch_desc_needs_time)
                        effectiveReminderEnabled -> stringResource(R.string.today_reminder_switch_desc_on)
                        else -> stringResource(R.string.today_reminder_switch_desc_off)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = effectiveReminderEnabled,
                enabled = reminderCanBeEnabled,
                onCheckedChange = { reminderEnabled = it },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.today_delete_label), color = StreakCoral)
            }
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                enabled = canSave,
                onClick = {
                    val startTime = time.takeIf { it.isNotBlank() }
                    val reminderTime = if (effectiveReminderEnabled && startTime != null) startTime else null
                    onSave(
                        title,
                        note.takeIf { it.isNotBlank() },
                        date,
                        startTime,
                        category.takeIf { it.isNotBlank() },
                        priority,
                        reminderTime,
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        time = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dateText = millis.toLocalDate().toString()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun PlanPrioritySelector(priority: Int, onPriorityChange: (Int) -> Unit) {
    val priorityLabels = listOf(
        stringResource(R.string.today_priority_high),
        stringResource(R.string.today_priority_normal),
        stringResource(R.string.today_priority_low),
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.today_add_task_priority_label), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3).forEachIndexed { index, value ->
                val selected = priority == value
                val color = when (value) {
                    1 -> StreakCoral
                    2 -> CandyPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) color.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            if (selected) color else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { onPriorityChange(value) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        priorityLabels[index],
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                        ),
                        color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanDateNavigationRow(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
    onPickDate: () -> Unit,
) {
    val weekEnd = weekStart.plusDays(6)
    val weekLabel = stringResource(
        R.string.plan_week_range,
        weekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR"))),
        weekEnd.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR"))),
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onPreviousWeek, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Rounded.ChevronLeft,
                    contentDescription = stringResource(R.string.plan_previous_week_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                weekLabel,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (selectedDate != today) {
                TextButton(onClick = onToday, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        stringResource(R.string.plan_today_action),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
            IconButton(onClick = onPickDate, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = stringResource(R.string.plan_pick_date_cd),
                    tint = CandyPrimary,
                )
            }
            IconButton(onClick = onNextWeek, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = stringResource(R.string.plan_next_week_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlanHeroCard(
    selectedDate: LocalDate,
    today: LocalDate,
    taskCount: Int,
    completedCount: Int,
    overdueCount: Int,
) {
    val todayLabel = stringResource(R.string.label_today)
    val tomorrowLabel = stringResource(R.string.label_tomorrow)
    val yesterdayLabel = stringResource(R.string.label_yesterday)
    val dayTag = when (selectedDate) {
        today -> todayLabel
        today.plusDays(1) -> tomorrowLabel
        today.minusDays(1) -> yesterdayLabel
        else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale("tr", "TR")))
            .replaceFirstChar { it.titlecase(Locale("tr", "TR")) }
    }
    val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr", "TR")))

    com.benimgunlerim.ui.components.ScreenHeroCard(
        title = stringResource(R.string.plan_title),
        subtitle = "$dateStr • $dayTag",
        metricRow = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanSummaryTile(
                    value = taskCount.toString(),
                    label = stringResource(R.string.plan_summary_total),
                    color = LevelSky,
                    modifier = Modifier.weight(1f),
                )
                PlanSummaryTile(
                    value = completedCount.toString(),
                    label = stringResource(R.string.plan_summary_completed),
                    color = CandyPrimary,
                    modifier = Modifier.weight(1f),
                )
                PlanSummaryTile(
                    value = overdueCount.toString(),
                    label = stringResource(R.string.plan_summary_overdue),
                    color = StreakCoral,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    )
}

@Composable
private fun PlanSummaryTile(value: String, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = color,
            maxLines = 1,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


private val turkishDays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

@Composable
private fun WeekDatePicker(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    taskCounts: Map<LocalDate, Int>,
    onSelectDate: (LocalDate) -> Unit,
) {
    LazyRow(
        modifier = Modifier.testTag(TestTags.PlanWeekPicker),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items((0..6).map { weekStart.plusDays(it.toLong()) }) { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            val dayLabel = turkishDays[date.dayOfWeek.value - 1]
            val taskCount = taskCounts[date].orZero()
            val dayCd = "$dayLabel ${date.dayOfMonth}, $taskCount görev"
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when {
                            isSelected -> CandyPrimary
                            isToday -> CandyPrimary.copy(.14f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                    )
                    .border(
                        width = if (isToday && !isSelected) 1.dp else 0.dp,
                        color = if (isToday && !isSelected) CandyPrimary.copy(.40f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clickable { onSelectDate(date) }
                    .semantics { contentDescription = dayCd }
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
                if (taskCount > 0) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color.White.copy(.22f) else CandyPrimary.copy(.14f))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(
                            taskCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else CandyPrimary,
                        )
                    }
                } else {
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

private fun Int?.orZero(): Int = this ?: 0

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
