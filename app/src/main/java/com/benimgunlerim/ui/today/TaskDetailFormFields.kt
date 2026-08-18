package com.benimgunlerim.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.domain.validation.TimeInputValidator
import com.benimgunlerim.ui.theme.StreakCoral
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal data class TaskDetailFormData(
    val title: String,
    val note: String,
    val plannedDate: LocalDate,
    val time: String,
    val timeErrorText: String?,
    val category: String,
    val interactionLocked: Boolean,
)

internal data class TaskDetailFormActions(
    val onTitleChange: (String) -> Unit,
    val onNoteChange: (String) -> Unit,
    val onDatePickerRequest: () -> Unit,
    val onTimeChange: (String) -> Unit,
    val onTimePickerRequest: () -> Unit,
    val onCategoryChange: (String) -> Unit,
    val onCategoryDone: () -> Unit,
)

private val FieldCornerRadius = 8.dp

@Composable
internal fun TaskDetailFormFields(
    data: TaskDetailFormData,
    dateFmt: DateTimeFormatter,
    actions: TaskDetailFormActions,
) {
    Text(stringResource(R.string.today_edit_task_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
    OutlinedTextField(
        value = data.title,
        onValueChange = actions.onTitleChange,
        label = { Text(stringResource(R.string.today_add_task_name_label)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !data.interactionLocked,
        singleLine = true,
        shape = RoundedCornerShape(FieldCornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    OutlinedTextField(
        value = data.note,
        onValueChange = actions.onNoteChange,
        label = { Text(stringResource(R.string.today_edit_task_note_label)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !data.interactionLocked,
        minLines = 2,
        maxLines = 3,
        shape = RoundedCornerShape(FieldCornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    SheetSectionHeader(stringResource(R.string.today_detail_section_plan))
    Text(stringResource(R.string.today_edit_task_date_label), style = MaterialTheme.typography.labelLarge)
    TextButton(onClick = actions.onDatePickerRequest, enabled = !data.interactionLocked) {
        Text(data.plannedDate.format(dateFmt))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = data.time,
            onValueChange = { actions.onTimeChange(TimeInputValidator.sanitize(it)) },
            label = { Text(stringResource(R.string.today_task_plan_time_label)) },
            modifier = Modifier.weight(1f),
            enabled = !data.interactionLocked,
            singleLine = true,
            shape = RoundedCornerShape(FieldCornerRadius),
            isError = data.timeErrorText != null,
            supportingText = { data.timeErrorText?.let { Text(it, color = StreakCoral) } },
            trailingIcon = {
                IconButton(onClick = actions.onTimePickerRequest, enabled = !data.interactionLocked) {
                    Icon(Icons.Outlined.Schedule, contentDescription = stringResource(R.string.today_pick_time_cd))
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = data.category,
            onValueChange = actions.onCategoryChange,
            label = { Text(stringResource(R.string.today_add_task_category_label)) },
            modifier = Modifier.weight(1f),
            enabled = !data.interactionLocked,
            singleLine = true,
            shape = RoundedCornerShape(FieldCornerRadius),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { actions.onCategoryDone() }),
        )
    }
}
