package com.benimgunlerim.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.domain.validation.TimeInputValidator
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ActionButtonHeight = AppTokens.Calm.resetButtonHeight
private val ActionButtonCornerRadius = AppTokens.Radius.sm
private const val DEFAULT_TIME_HOUR = 9
private const val TIME_STRING_LENGTH = 5
private const val TIME_HOUR_START = 0
private const val TIME_HOUR_END = 2
private const val TIME_MINUTE_START = 3
private const val TIME_MINUTE_END = 5

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
internal fun TaskDetailSheet(
    task: TodayTaskUi,
    today: LocalDate,
    subtasks: List<SubTaskEntity>,
    interactionLocked: Boolean,
    callbacks: TaskDetailCallbacks,
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var note by remember(task.id) { mutableStateOf(task.note ?: "") }
    var time by remember(task.id) { mutableStateOf(task.startTime ?: "") }
    var category by remember(task.id) { mutableStateOf(task.category ?: "") }
    var priority by remember(task.id) { mutableIntStateOf(task.priority) }
    var plannedDate by remember(task.id) {
        mutableStateOf(runCatching { LocalDate.parse(task.plannedDate) }.getOrElse { today })
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var reminderEnabled by remember(task.id) { mutableStateOf(task.reminderTime != null) }
    val scroll = rememberScrollState()
    val priorityLabels = listOf(
        stringResource(R.string.today_priority_high),
        stringResource(R.string.today_priority_normal),
        stringResource(R.string.today_priority_low),
    )
    val timeErrIncomplete = stringResource(R.string.today_time_incomplete)
    val timeErrInvalid = stringResource(R.string.today_time_invalid)
    val timeValidation = TimeInputValidator.validationMessageKey(time)
    val timeErrorText = when (timeValidation) {
        TimeInputValidator.TimeValidation.Ok -> null
        TimeInputValidator.TimeValidation.Incomplete -> timeErrIncomplete
        TimeInputValidator.TimeValidation.InvalidFormat, TimeInputValidator.TimeValidation.InvalidClock -> timeErrInvalid
    }
    val timePickerState = rememberTimePickerState(
        initialHour = time.takeIf { TimeInputValidator.isValid(it) && it.length == TIME_STRING_LENGTH }
            ?.substring(TIME_HOUR_START, TIME_HOUR_END)
            ?.toIntOrNull() ?: DEFAULT_TIME_HOUR,
        initialMinute = time.takeIf { TimeInputValidator.isValid(it) && it.length == TIME_STRING_LENGTH }
            ?.substring(TIME_MINUTE_START, TIME_MINUTE_END)
            ?.toIntOrNull() ?: 0,
        is24Hour = true,
    )
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        initialSelectedDateMillis = plannedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    )
    val reminderCanBeEnabled = TimeInputValidator.isValid(time) && time.isNotBlank()
    val effectiveReminderEnabled = reminderEnabled && reminderCanBeEnabled
    val dateFmt = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr", "TR")) }
    val focusManager = LocalFocusManager.current

    if (showDeleteConfirm) {
        TaskDeleteConfirmDialog(
            taskTitle = task.title,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                callbacks.onDelete()
            },
        )
    }

    Column(
        Modifier
            .verticalScroll(scroll)
            .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.md)
            .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm + AppTokens.Spacing.xxs / 2),
    ) {
        TaskDetailFormFields(
            data = TaskDetailFormData(
                title = title,
                note = note,
                plannedDate = plannedDate,
                time = time,
                timeErrorText = timeErrorText,
                category = category,
                interactionLocked = interactionLocked,
            ),
            dateFmt = dateFmt,
            actions = TaskDetailFormActions(
                onTitleChange = { title = it },
                onNoteChange = { note = it },
                onDatePickerRequest = { showDatePicker = true },
                onTimeChange = { time = it },
                onTimePickerRequest = { showTimePicker = true },
                onCategoryChange = { category = it },
                onCategoryDone = { focusManager.clearFocus() },
            ),
        )
        TaskDetailOptionsSection(
            interactionLocked = interactionLocked,
            reminderCanBeEnabled = reminderCanBeEnabled,
            effectiveReminderEnabled = effectiveReminderEnabled,
            onReminderEnabledChange = { reminderEnabled = it },
            priority = priority,
            priorityLabels = priorityLabels,
            onPriorityChange = { priority = it },
        )
        TaskDetailSubtasksSection(
            subtasks = subtasks,
            interactionLocked = interactionLocked,
            onToggleSubTask = callbacks.onToggleSubTask,
            onDeleteSubTask = callbacks.onDeleteSubTask,
            onAddSubTask = callbacks.onAddSubTask,
        )
        val canSave = title.isNotBlank() && TimeInputValidator.isValid(time)
        val startToSave = time.takeIf { it.isNotBlank() }
        val reminderToSave = if (effectiveReminderEnabled && startToSave != null) startToSave else null
        Button(
            onClick = {
                callbacks.onSave(
                    title,
                    note.takeIf { it.isNotBlank() },
                    plannedDate,
                    startToSave,
                    category.takeIf { it.isNotBlank() },
                    priority,
                    reminderToSave,
                )
            },
            enabled = canSave && !interactionLocked,
            modifier = Modifier.fillMaxWidth().height(ActionButtonHeight),
            shape = RoundedCornerShape(ActionButtonCornerRadius),
            colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White),
        ) { Text(stringResource(R.string.action_save)) }
        Row(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
            OutlinedSmallButton(
                stringResource(R.string.today_move_tomorrow_btn),
                Modifier.weight(1f),
                enabled = !interactionLocked,
                onClick = callbacks.onMoveTomorrow,
            )
            OutlinedSmallButton(
                stringResource(R.string.today_delete_label),
                Modifier.weight(1f),
                enabled = !interactionLocked,
            ) { showDeleteConfirm = true }
        }
        Spacer(Modifier.height(AppTokens.Spacing.sm))
    }
    if (showTimePicker) {
        TaskTimePickerDialog(
            state = timePickerState,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                showTimePicker = false
            },
        )
    }
    if (showDatePicker) {
        TaskDatePickerDialog(
            state = datePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = { picked -> plannedDate = picked },
        )
    }
}
