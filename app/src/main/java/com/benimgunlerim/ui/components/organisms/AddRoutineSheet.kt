package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.benimgunlerim.R
import com.benimgunlerim.domain.validation.TimeInputValidator
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppFilterChip
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.StreakCoral
import com.benimgunlerim.ui.today.TaskTimePickerDialog
import java.time.DayOfWeek
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
fun AddRoutineSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, targetDays: Set<DayOfWeek>, reminderTime: String?, category: String, targetCount: Int?, unit: String?) -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    initialTitle: String = "",
    initialDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    initialReminderTime: String? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var selectedDays by rememberSaveable { mutableStateOf(initialDays) }
    var category by rememberSaveable { mutableStateOf("Genel") }
    var reminderTime by rememberSaveable { mutableStateOf(initialReminderTime.orEmpty()) }
    var isGoalRoutine by rememberSaveable { mutableStateOf(false) }
    var targetValueText by rememberSaveable { mutableStateOf("") }
    var targetUnit by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val timeErrIncomplete = stringResource(R.string.today_time_incomplete)
    val timeErrInvalid = stringResource(R.string.today_time_invalid)
    val timeValidation = TimeInputValidator.validationMessageKey(reminderTime)
    val timeErrorText = when {
        reminderTime.isBlank() -> null
        timeValidation == TimeInputValidator.TimeValidation.Ok -> null
        timeValidation == TimeInputValidator.TimeValidation.Incomplete -> timeErrIncomplete
        else -> timeErrInvalid
    }
    val timePickerState = rememberTimePickerState(
        initialHour = reminderTime.takeIf { TimeInputValidator.isValid(it) && it.length == TIME_STRING_LENGTH }
            ?.substring(TIME_HOUR_START, TIME_HOUR_END)?.toIntOrNull() ?: DEFAULT_TIME_HOUR,
        initialMinute = reminderTime.takeIf { TimeInputValidator.isValid(it) && it.length == TIME_STRING_LENGTH }
            ?.substring(TIME_MINUTE_START, TIME_MINUTE_END)?.toIntOrNull() ?: 0,
        is24Hour = true,
    )

    if (showTimePicker) {
        TaskTimePickerDialog(
            state = timePickerState,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                reminderTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
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
                text = stringResource(
                    if (isEditMode) R.string.routine_sheet_edit_title else R.string.routine_sheet_add_title,
                ),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isError = false
                },
                label = { Text(stringResource(R.string.routine_sheet_name_label)) },
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                Text(
                    text = stringResource(R.string.routine_sheet_repeat_days_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        val shortLabel = TurkishDayShortCode.getValue(day)
                        AppFilterChip(
                            label = shortLabel,
                            selected = isSelected,
                            onSelectedChange = {
                                selectedDays = if (isSelected) {
                                    if (selectedDays.size > 1) selectedDays - day else selectedDays
                                } else {
                                    selectedDays + day
                                }
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.routine_sheet_category_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = reminderTime,
                onValueChange = { reminderTime = TimeInputValidator.sanitize(it) },
                label = { Text(stringResource(R.string.routine_sheet_reminder_time_label)) },
                modifier = Modifier.fillMaxWidth(),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.routine_sheet_goal_switch_label),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(
                            if (isGoalRoutine) R.string.routine_sheet_goal_switch_desc_on
                            else R.string.routine_sheet_goal_switch_desc_off,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = isGoalRoutine, onCheckedChange = { isGoalRoutine = it })
            }

            if (isGoalRoutine) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                    OutlinedTextField(
                        value = targetValueText,
                        onValueChange = { input -> targetValueText = input.filter { it.isDigit() } },
                        label = { Text(stringResource(R.string.routine_sheet_target_value_label)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    )
                    OutlinedTextField(
                        value = targetUnit,
                        onValueChange = { targetUnit = it },
                        label = { Text(stringResource(R.string.routine_sheet_target_unit_label)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                }
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
                            val reminderToSave = reminderTime.takeIf {
                                it.isNotBlank() && TimeInputValidator.isValid(it)
                            }
                            val targetCount = if (isGoalRoutine) targetValueText.toIntOrNull() else null
                            val unit = if (isGoalRoutine) targetUnit.ifBlank { null } else null
                            onSave(title, selectedDays, reminderToSave, category.ifBlank { "Genel" }, targetCount, unit)
                            onDismiss()
                        }
                    },
                    variant = AppButtonVariant.Primary,
                )
            }
        }
    }
}
