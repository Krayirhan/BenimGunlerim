package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppFilterChip
import com.benimgunlerim.ui.theme.AppTokens
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
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
    var isError by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                            onSave(title, selectedDate, null, category.ifBlank { null }, selectedPriority, null)
                            onDismiss()
                        }
                    },
                    variant = AppButtonVariant.Primary,
                )
            }
        }
    }
}
