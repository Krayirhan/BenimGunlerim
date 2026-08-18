package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.benimgunlerim.R
import com.benimgunlerim.domain.validation.TimeInputValidator
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppFilterChip
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.StreakCoral
import com.benimgunlerim.ui.today.TaskTimePickerDialog
import java.time.LocalDate
import java.util.Locale

private const val TIME_STRING_LENGTH = 5
private const val DEFAULT_TIME_HOUR = 9
private const val TIME_HOUR_START = 0
private const val TIME_HOUR_END = 2
private const val TIME_MINUTE_START = 3
private const val TIME_MINUTE_END = 5

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, date: String, startTime: String?, category: String?, priority: Int, reminderTime: String?) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: String = LocalDate.now().toString(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
    var selectedPriority by rememberSaveable { mutableIntStateOf(2) }
    var category by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var reminderEnabled by rememberSaveable { mutableStateOf(false) }
    var isError by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val timeErrIncomplete = stringResource(R.string.today_time_incomplete)
    val timeErrInvalid = stringResource(R.string.today_time_invalid)
    val timeValidation = TimeInputValidator.validationMessageKey(time)
    val timeErrorText = when {
        time.isBlank() -> null
        timeValidation == TimeInputValidator.TimeValidation.Ok -> null
        timeValidation == TimeInputValidator.TimeValidation.Incomplete -> timeErrIncomplete
        else -> timeErrInvalid
    }
    val reminderCanBeEnabled = time.isNotBlank() && TimeInputValidator.isValid(time)
    val effectiveReminderEnabled = reminderEnabled && reminderCanBeEnabled
    val timePickerState = rememberTimePickerState(
        initialHour = time.takeIf { TimeInputValidator.isValid(it) && it.length == TIME_STRING_LENGTH }
            ?.substring(TIME_HOUR_START, TIME_HOUR_END)?.toIntOrNull() ?: DEFAULT_TIME_HOUR,
        initialMinute = time.takeIf { TimeInputValidator.isValid(it) && it.length == TIME_STRING_LENGTH }
            ?.substring(TIME_MINUTE_START, TIME_MINUTE_END)?.toIntOrNull() ?: 0,
        is24Hour = true,
    )

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.add_task_sheet_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isError = false
                },
                label = { Text(stringResource(R.string.add_task_sheet_title_label)) },
                isError = isError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.AddTaskTitleField),
            )

            Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                Text(
                    text = stringResource(R.string.add_task_sheet_priority_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppFilterChip(
                        label = stringResource(R.string.add_task_sheet_priority_high),
                        selected = selectedPriority == 1,
                        onSelectedChange = { if (it) selectedPriority = 1 },
                    )
                    AppFilterChip(
                        label = stringResource(R.string.add_task_sheet_priority_normal),
                        selected = selectedPriority == 2,
                        onSelectedChange = { if (it) selectedPriority = 2 },
                    )
                    AppFilterChip(
                        label = stringResource(R.string.add_task_sheet_priority_low),
                        selected = selectedPriority == 3,
                        onSelectedChange = { if (it) selectedPriority = 3 },
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = TimeInputValidator.sanitize(it) },
                    label = { Text(stringResource(R.string.today_task_plan_time_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = timeErrorText != null,
                    supportingText = { timeErrorText?.let { Text(it, color = StreakCoral) } },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.today_reminder_switch_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = effectiveReminderEnabled,
                    enabled = reminderCanBeEnabled,
                    onCheckedChange = { reminderEnabled = it },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = onDismiss,
                    variant = AppButtonVariant.Ghost,
                )
                Spacer(modifier = Modifier.height(AppTokens.Spacing.xs))
                AppButton(
                    text = stringResource(R.string.action_save),
                    onClick = {
                        if (title.isBlank()) {
                            isError = true
                        } else {
                            val startToSave = time.takeIf { it.isNotBlank() && TimeInputValidator.isValid(it) }
                            val reminderToSave = if (effectiveReminderEnabled && startToSave != null) startToSave else null
                            onSave(title, selectedDate, startToSave, category.ifBlank { null }, selectedPriority, reminderToSave)
                            onDismiss()
                        }
                    },
                    variant = AppButtonVariant.Primary,
                )
            }
        }
    }
}
