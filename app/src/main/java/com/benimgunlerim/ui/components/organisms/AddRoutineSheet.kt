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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppFilterChip
import com.benimgunlerim.ui.theme.AppTokens
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
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

    var title by remember { mutableStateOf(initialTitle) }
    var selectedDays by remember { mutableStateOf(initialDays) }
    var category by remember { mutableStateOf("Genel") }
    var isError by remember { mutableStateOf(false) }

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
                            onSave(title, selectedDays, initialReminderTime, category, null, null)
                            onDismiss()
                        }
                    },
                    variant = AppButtonVariant.Primary,
                )
            }
        }
    }
}
