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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedPriority by remember { mutableIntStateOf(2) }
    var category by remember { mutableStateOf("") }
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
                text = "Yeni Görev Ekle",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isError = false
                },
                label = { Text("Görev Başlığı") },
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                Text(
                    text = "Öncelik",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppFilterChip(
                        label = "Yüksek",
                        selected = selectedPriority == 1,
                        onSelectedChange = { if (it) selectedPriority = 1 },
                    )
                    AppFilterChip(
                        label = "Normal",
                        selected = selectedPriority == 2,
                        onSelectedChange = { if (it) selectedPriority = 2 },
                    )
                    AppFilterChip(
                        label = "Düşük",
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
                    text = "İptal",
                    onClick = onDismiss,
                    variant = AppButtonVariant.Ghost,
                )
                Spacer(modifier = Modifier.height(AppTokens.Spacing.xs))
                AppButton(
                    text = "Kaydet",
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
