@file:Suppress("LongParameterList", "MagicNumber")

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

@Composable
internal fun TaskDetailFormFields(
    title: String,
    onTitleChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    plannedDate: LocalDate,
    dateFmt: DateTimeFormatter,
    onDatePickerRequest: () -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    timeErrorText: String?,
    onTimePickerRequest: () -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    onCategoryDone: () -> Unit,
    interactionLocked: Boolean,
) {
    Text(stringResource(R.string.today_edit_task_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.today_add_task_name_label)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !interactionLocked,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
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
    TextButton(onClick = onDatePickerRequest, enabled = !interactionLocked) {
        Text(plannedDate.format(dateFmt))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = time,
            onValueChange = { onTimeChange(TimeInputValidator.sanitize(it)) },
            label = { Text(stringResource(R.string.today_task_plan_time_label)) },
            modifier = Modifier.weight(1f),
            enabled = !interactionLocked,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            isError = timeErrorText != null,
            supportingText = { timeErrorText?.let { Text(it, color = StreakCoral) } },
            trailingIcon = {
                IconButton(onClick = onTimePickerRequest, enabled = !interactionLocked) {
                    Icon(Icons.Outlined.Schedule, contentDescription = stringResource(R.string.today_pick_time_cd))
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = category,
            onValueChange = onCategoryChange,
            label = { Text(stringResource(R.string.today_add_task_category_label)) },
            modifier = Modifier.weight(1f),
            enabled = !interactionLocked,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onCategoryDone() }),
        )
    }
}
