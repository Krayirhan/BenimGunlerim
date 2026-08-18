package com.benimgunlerim.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.StreakCoral

private val ItemCornerRadius = 10.dp
private val InputCornerRadius = 8.dp
private const val COMPLETED_TEXT_ALPHA = 0.5f
private const val ACTIVE_TEXT_ALPHA = 1.0f

@Composable
internal fun TaskDetailSubtasksSection(
    subtasks: List<SubTaskEntity>,
    interactionLocked: Boolean,
    onToggleSubTask: (SubTaskEntity) -> Unit,
    onDeleteSubTask: (SubTaskEntity) -> Unit,
    onAddSubTask: (String) -> Unit,
) {
    var newSubTaskText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val subtaskToggleCd = stringResource(R.string.today_subtask_toggle_cd)

    SheetSectionHeader(stringResource(R.string.today_detail_section_subtasks))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.today_subtasks_label), style = MaterialTheme.typography.labelLarge)
        if (subtasks.isEmpty()) {
            Text(
                stringResource(R.string.today_subtasks_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            subtasks.forEach { st ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(ItemCornerRadius))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier.size(22.dp).clip(CircleShape)
                            .background(if (st.isCompleted) CandyPrimary else CandyPrimary.copy(alpha = 0.12f))
                            .border(1.5.dp, CandyPrimary.copy(alpha = if (st.isCompleted) 1f else 0.4f), CircleShape)
                            .semantics { contentDescription = subtaskToggleCd }
                            .clickable(enabled = !interactionLocked) { onToggleSubTask(st) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (st.isCompleted) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        st.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = if (st.isCompleted) TextDecoration.LineThrough else null,
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (st.isCompleted) COMPLETED_TEXT_ALPHA else ACTIVE_TEXT_ALPHA),
                    )
                    IconButton(
                        onClick = { onDeleteSubTask(st) },
                        enabled = !interactionLocked,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.today_delete_label),
                            tint = StreakCoral,
                            modifier = Modifier.size(16.dp),
                        )
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
                shape = RoundedCornerShape(InputCornerRadius),
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
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(InputCornerRadius)).background(CandyPrimary),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.today_subtask_add_cd), tint = Color.White)
            }
        }
    }
}
