@file:Suppress(
    "LongParameterList",
    "LongMethod",
    "CyclomaticComplexMethod",
    "MagicNumber",
    "MaxLineLength",
)

package com.benimgunlerim.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.domain.validation.TimeInputValidator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTaskSheet(
    today: LocalDate,
    plannedDate: LocalDate,
    onPlannedDateChange: (LocalDate) -> Unit,
    text: String,
    note: String,
    time: String,
    category: String,
    priority: Int,
    onTextChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    reminderEnabled: Boolean,
    onReminderEnabledChange: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val priorityLabels = listOf(stringResource(R.string.today_priority_high), stringResource(R.string.today_priority_normal), stringResource(R.string.today_priority_low))
    val dateLabels = listOf(stringResource(R.string.label_today), stringResource(R.string.label_tomorrow), stringResource(R.string.today_date_plus2))
    val timeErrIncomplete = stringResource(R.string.today_time_incomplete)
    val timeErrInvalid = stringResource(R.string.today_time_invalid)
    val timeValidation = TimeInputValidator.validationMessageKey(time)
    val timeErrorText = when (timeValidation) {
        TimeInputValidator.TimeValidation.Ok -> null
        TimeInputValidator.TimeValidation.Incomplete -> timeErrIncomplete
        TimeInputValidator.TimeValidation.InvalidFormat, TimeInputValidator.TimeValidation.InvalidClock -> timeErrInvalid
    }
    val timePickerState = rememberTimePickerState(
        initialHour = time.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }?.substring(0, 2)?.toIntOrNull() ?: 9,
        initialMinute = time.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }?.substring(3, 5)?.toIntOrNull() ?: 0,
        is24Hour = true,
    )
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        initialSelectedDateMillis = plannedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    )
    val dateFmt = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr", "TR")) }
    val selectedPreset = when (plannedDate) {
        today -> 0
        today.plusDays(1) -> 1
        today.plusDays(2) -> 2
        else -> -1
    }
    val reminderCanBeEnabled = TimeInputValidator.isValid(time) && time.isNotBlank()
    val effectiveReminderEnabled = reminderEnabled && reminderCanBeEnabled
    val focusManager = LocalFocusManager.current
    Column(
        Modifier
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.today_add_task_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text(stringResource(R.string.today_add_task_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = time,
            onValueChange = { onTimeChange(TimeInputValidator.sanitize(it)) },
            label = { Text(stringResource(R.string.today_task_plan_time_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            isError = timeErrorText != null,
            supportingText = { timeErrorText?.let { Text(it, color = StreakCoral) } },
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Outlined.Schedule, contentDescription = stringResource(R.string.today_pick_time_cd))
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.today_add_task_priority_label), style = MaterialTheme.typography.labelLarge)
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
        TextButton(onClick = { showAdvanced = !showAdvanced }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showAdvanced) stringResource(R.string.today_advanced_hide) else stringResource(R.string.today_advanced_show))
        }
        if (showAdvanced) {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.today_add_task_note_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = category,
                onValueChange = onCategoryChange,
                label = { Text(stringResource(R.string.today_add_task_category_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.today_add_task_date_label), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 1, 2).forEach { offset ->
                        val sel = selectedPreset == offset
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (sel) CandySecondary.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, if (sel) CandySecondary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable {
                                    onPlannedDateChange(today.plusDays(offset.toLong()))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(dateLabels[offset], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) CandySecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                TextButton(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.today_pick_custom_date, plannedDate.format(dateFmt)))
                }
            }
        }
        val canSave = text.isNotBlank() && TimeInputValidator.isValid(time)
        Button(onClick = onSave, enabled = canSave, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White)) {
            Text(stringResource(R.string.action_save))
        }
        Spacer(Modifier.height(12.dp))
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
                ) { Text(stringResource(R.string.action_save)) }
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
                        datePickerState.selectedDateMillis?.let { ms ->
                            val picked = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                            if (!picked.isBefore(today)) {
                                onPlannedDateChange(picked)
                            }
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.action_save)) }
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
@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
internal fun TaskDetailSheet(
    task: TodayTaskUi,
    today: LocalDate,
    subtasks: List<SubTaskEntity>,
    interactionLocked: Boolean,
    onSave: (String, String?, LocalDate, String?, String?, Int, String?) -> Unit,
    onMoveTomorrow: () -> Unit,
    onDelete: () -> Unit,
    onAddSubTask: (String) -> Unit,
    onToggleSubTask: (SubTaskEntity) -> Unit,
    onDeleteSubTask: (SubTaskEntity) -> Unit,
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var note by remember(task.id) { mutableStateOf(task.note ?: "") }
    var time by remember(task.id) { mutableStateOf(task.startTime ?: "") }
    var category by remember(task.id) { mutableStateOf(task.category ?: "") }
    var priority by remember(task.id) { mutableStateOf(task.priority) }
    var plannedDate by remember(task.id) { mutableStateOf(runCatching { LocalDate.parse(task.plannedDate) }.getOrElse { today }) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newSubTaskText by remember { mutableStateOf("") }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var reminderEnabled by remember(task.id) { mutableStateOf(task.reminderTime != null) }
    val scroll = rememberScrollState()
    val priorityLabels = listOf(stringResource(R.string.today_priority_high), stringResource(R.string.today_priority_normal), stringResource(R.string.today_priority_low))
    val timeErrIncomplete = stringResource(R.string.today_time_incomplete)
    val timeErrInvalid = stringResource(R.string.today_time_invalid)
    val timeValidation = TimeInputValidator.validationMessageKey(time)
    val timeErrorText = when (timeValidation) {
        TimeInputValidator.TimeValidation.Ok -> null
        TimeInputValidator.TimeValidation.Incomplete -> timeErrIncomplete
        TimeInputValidator.TimeValidation.InvalidFormat, TimeInputValidator.TimeValidation.InvalidClock -> timeErrInvalid
    }
    val timePickerState = rememberTimePickerState(
        initialHour = time.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }?.substring(0, 2)?.toIntOrNull() ?: 9,
        initialMinute = time.takeIf { TimeInputValidator.isValid(it) && it.length == 5 }?.substring(3, 5)?.toIntOrNull() ?: 0,
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
    val subtaskToggleCd = stringResource(R.string.today_subtask_toggle_cd)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.today_delete_task_title)) },
            text = { Text(stringResource(R.string.today_delete_task_body, task.title)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text(stringResource(R.string.today_delete_label), color = StreakCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            },
        )
    }
    Column(
        Modifier
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.today_edit_task_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        SheetSectionHeader(stringResource(R.string.today_detail_section_content))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.today_add_task_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !interactionLocked,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.today_edit_task_note_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !interactionLocked,
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        SheetSectionHeader(stringResource(R.string.today_detail_section_plan))
        Text(stringResource(R.string.today_edit_task_date_label), style = MaterialTheme.typography.labelLarge)
        TextButton(onClick = { showDatePicker = true }, enabled = !interactionLocked) {
            Text(plannedDate.format(dateFmt))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = time,
                onValueChange = { time = TimeInputValidator.sanitize(it) },
                label = { Text(stringResource(R.string.today_task_plan_time_label)) },
                modifier = Modifier.weight(1f),
                enabled = !interactionLocked,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                isError = timeErrorText != null,
                supportingText = { timeErrorText?.let { Text(it, color = StreakCoral) } },
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }, enabled = !interactionLocked) {
                        Icon(Icons.Outlined.Schedule, contentDescription = stringResource(R.string.today_pick_time_cd))
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.today_add_task_category_label)) },
                modifier = Modifier.weight(1f),
                enabled = !interactionLocked,
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
        }
        SheetSectionHeader(stringResource(R.string.today_detail_section_options))
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
                enabled = !interactionLocked && reminderCanBeEnabled,
                onCheckedChange = { reminderEnabled = it },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.today_add_task_priority_label), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEachIndexed { i, p ->
                    val sel = priority == p
                    val col = if (i == 0) StreakCoral else if (i == 1) CandyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (sel) col.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (sel) col else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable(enabled = !interactionLocked) { priority = p }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(priorityLabels[i], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) col else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        SheetSectionHeader(stringResource(R.string.today_detail_section_subtasks))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.today_subtasks_label), style = MaterialTheme.typography.labelLarge)
            if (subtasks.isEmpty()) {
                Text(stringResource(R.string.today_subtasks_empty), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                .semantics { contentDescription = subtaskToggleCd }
                                .clickable(enabled = !interactionLocked) { onToggleSubTask(st) },
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
                        IconButton(
                            onClick = { onDeleteSubTask(st) },
                            enabled = !interactionLocked,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.today_delete_label), tint = StreakCoral, modifier = Modifier.size(16.dp))
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
                    label = { Text(stringResource(R.string.today_subtask_add_label)) },
                    modifier = Modifier.weight(1f),
                    enabled = !interactionLocked,
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (!interactionLocked && newSubTaskText.isNotBlank()) {
                                onAddSubTask(newSubTaskText)
                                newSubTaskText = ""
                            }
                        },
                    ),
                )
                IconButton(
                    onClick = {
                        if (!interactionLocked && newSubTaskText.isNotBlank()) {
                            onAddSubTask(newSubTaskText)
                            newSubTaskText = ""
                        }
                    },
                    enabled = !interactionLocked,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(CandyPrimary),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.today_subtask_add_cd), tint = Color.White)
                }
            }
        }
        val canSave = title.isNotBlank() && TimeInputValidator.isValid(time)
        val startToSave = time.takeIf { it.isNotBlank() }
        val reminderToSave = if (effectiveReminderEnabled && startToSave != null) startToSave else null
        Button(
            onClick = {
                onSave(
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White),
        ) { Text(stringResource(R.string.action_save)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedSmallButton(
                stringResource(R.string.today_move_tomorrow_btn),
                Modifier.weight(1f),
                enabled = !interactionLocked,
                onClick = onMoveTomorrow,
            )
            OutlinedSmallButton(
                stringResource(R.string.today_delete_label),
                Modifier.weight(1f),
                enabled = !interactionLocked,
            ) { showDeleteConfirm = true }
        }
        Spacer(Modifier.height(12.dp))
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
                ) { Text(stringResource(R.string.action_save)) }
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
                        datePickerState.selectedDateMillis?.let { ms ->
                            val picked = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                            plannedDate = picked
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.action_save)) }
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
internal fun CloseDaySheet(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    overdueCount: Int,
    onSave: (mood: Int, energy: Int, note: String, bestMoment: String, challenge: String, tomorrowIntention: String, carryTasks: Boolean) -> Unit,
) {
    // rememberSaveable: process death / config change sırasında kullanıcının
    // yazdığı gün kapatma özeti (mood, best moment, challenge, note...) kaybolmasın.
    var step by rememberSaveable { mutableStateOf(0) }
    var mood by rememberSaveable { mutableStateOf(3) }
    var energy by rememberSaveable { mutableStateOf(3) }
    var note by rememberSaveable { mutableStateOf("") }
    var bestMoment by rememberSaveable { mutableStateOf("") }
    var challenge by rememberSaveable { mutableStateOf("") }
    var tomorrowIntention by rememberSaveable { mutableStateOf("") }
    var carryTasks by rememberSaveable { mutableStateOf(overdueCount > 0) }

    val moodLabels = listOf(
        stringResource(R.string.today_close_step1_mood_very_bad),
        stringResource(R.string.today_close_step1_mood_bad),
        stringResource(R.string.today_close_step1_mood_normal),
        stringResource(R.string.today_close_step1_mood_good),
        stringResource(R.string.today_close_step1_mood_great),
    )
    val moodColors = listOf(StreakCoral, StreakCoral.copy(.65f), CandySecondary, CandyPrimary, CompletedGreen)

    val closeScroll = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val carryCheckboxCd = stringResource(R.string.today_carry_checkbox_cd)
    Column(
        Modifier
            .verticalScroll(closeScroll)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                Text(stringResource(R.string.today_close_step0_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Text(
                    stringResource(R.string.today_close_step0_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (totalCount == 0) {
                    Text(
                        stringResource(R.string.today_close_step0_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryTile("$completedCount / $totalCount", stringResource(R.string.today_close_step0_completed), CandyPrimary, Modifier.weight(1f))
                        SummaryTile("%${(progress * 100).toInt()}", stringResource(R.string.today_close_step0_success), CompletedGreen, Modifier.weight(1f))
                        if (overdueCount > 0) SummaryTile("$overdueCount", stringResource(R.string.today_close_step0_overdue), StreakCoral, Modifier.weight(1f))
                    }
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary.copy(.10f))) {
                        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary))
                    }
                }
            }
            1 -> {
                Text(stringResource(R.string.today_close_step1_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Text(
                    stringResource(R.string.today_close_step1_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.today_close_step1_mood_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        moodColors.forEachIndexed { i, col ->
                            Box(
                                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (mood == i) col else col.copy(.14f))
                                    .border(1.dp, if (mood == i) col else col.copy(.20f), RoundedCornerShape(10.dp))
                                    .clickable { mood = i },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(moodLabels[i], style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (mood == i) FontWeight.ExtraBold else FontWeight.Normal), color = if (mood == i) Color.White else col, maxLines = 1)
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.today_close_step1_energy_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text(stringResource(R.string.today_close_step2_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Text(
                    stringResource(R.string.today_close_step2_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = bestMoment,
                    onValueChange = { bestMoment = it },
                    label = { Text(stringResource(R.string.today_close_step2_best_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = challenge,
                    onValueChange = { challenge = it },
                    label = { Text(stringResource(R.string.today_close_step2_challenge_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.today_close_step2_note_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
            }
            3 -> {
                val suggestionOverdue = stringResource(R.string.today_close_step3_suggestion_overdue)
                val suggestionConsistency = stringResource(R.string.today_close_step3_suggestion_consistency)
                val suggestionFocus = stringResource(R.string.today_close_step3_suggestion_focus)
                val suggestions = buildList {
                    if (overdueCount > 0) add(suggestionOverdue)
                    add(suggestionConsistency)
                    add(suggestionFocus)
                }.distinct()
                Text(stringResource(R.string.today_close_step3_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                Text(
                    stringResource(R.string.today_close_step3_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = tomorrowIntention,
                    onValueChange = { tomorrowIntention = it },
                    label = { Text(stringResource(R.string.today_close_step3_intent_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                Text(
                    stringResource(R.string.today_close_step3_suggestion_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestions.take(2).forEach { suggestion ->
                        OutlinedSmallButton(
                            text = suggestion,
                            modifier = Modifier.weight(1f),
                            onClick = { tomorrowIntention = suggestion },
                        )
                    }
                }
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
                            if (carryTasks) Icon(Icons.Rounded.Check, contentDescription = carryCheckboxCd, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(stringResource(R.string.today_close_step3_move_overdue, overdueCount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(stringResource(R.string.today_close_step3_move_overdue_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) {
                OutlinedSmallButton(stringResource(R.string.today_close_back_btn), Modifier.weight(1f)) { step-- }
            }
            if (step < 3) {
                Button(onClick = { step++ }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(CandySecondary, Color.White)) {
                    Text(stringResource(R.string.today_close_next_btn))
                }
            } else {
                Button(
                    onClick = { onSave(mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(CandySecondary, Color.White),
                ) { Text(stringResource(R.string.today_close_save_btn)) }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SheetSectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
internal fun OutlinedSmallButton(text: String, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(42.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(text, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
    }
}
