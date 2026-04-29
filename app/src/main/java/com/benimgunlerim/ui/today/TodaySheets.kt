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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.time.LocalDate

@Composable
internal fun AddTaskSheet(
    text: String,
    note: String,
    time: String,
    category: String,
    priority: Int,
    dateOffset: Int,
    onTextChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onDateOffsetChange: (Int) -> Unit,
    onSave: () -> Unit,
) {
    val priorityLabels = listOf(stringResource(R.string.today_priority_high), stringResource(R.string.today_priority_normal), stringResource(R.string.today_priority_low))
    val dateLabels = listOf(stringResource(R.string.label_today), stringResource(R.string.label_tomorrow), stringResource(R.string.today_date_plus2))
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(R.string.today_add_task_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        OutlinedTextField(value = text, onValueChange = onTextChange, label = { Text(stringResource(R.string.today_add_task_name_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp))
        OutlinedTextField(value = note, onValueChange = onNoteChange, label = { Text(stringResource(R.string.today_add_task_note_label)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = time, onValueChange = onTimeChange, label = { Text(stringResource(R.string.today_add_task_time_label)) }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
            OutlinedTextField(value = category, onValueChange = onCategoryChange, label = { Text(stringResource(R.string.today_add_task_category_label)) }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
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
                            .clickable { onPriorityChange(p) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(priorityLabels[i], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) col else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.today_add_task_date_label), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2).forEach { offset ->
                    val sel = dateOffset == offset
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (sel) CandySecondary.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (sel) CandySecondary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { onDateOffsetChange(offset) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(dateLabels[offset], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) CandySecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Button(onClick = onSave, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White)) {
            Text(stringResource(R.string.action_save))
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
internal fun TaskDetailSheet(
    task: TodayTaskUi,
    subtasks: List<SubTaskEntity>,
    onSave: (String, String?, LocalDate, String?, String?, Int) -> Unit,
    onMoveTomorrow: () -> Unit,
    onDelete: () -> Unit,
    onAddSubTask: (String) -> Unit,
    onToggleSubTask: (SubTaskEntity) -> Unit,
    onDeleteSubTask: (SubTaskEntity) -> Unit,
) {
    var title by remember { mutableStateOf(task.title) }
    var note by remember { mutableStateOf(task.note ?: "") }
    var time by remember { mutableStateOf(task.startTime ?: "") }
    var category by remember { mutableStateOf(task.category ?: "") }
    var priority by remember { mutableStateOf(task.priority) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newSubTaskText by remember { mutableStateOf("") }
    val priorityLabels = listOf(stringResource(R.string.today_priority_high), stringResource(R.string.today_priority_normal), stringResource(R.string.today_priority_low))

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
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(R.string.today_edit_task_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.today_add_task_name_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp))
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.today_edit_task_note_label)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = time, onValueChange = { time = it.sanitizedTimeInput() }, label = { Text(stringResource(R.string.today_add_task_time_label)) }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.today_add_task_category_label)) }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(8.dp))
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
                            .clickable { priority = p }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(priorityLabels[i], style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal), color = if (sel) col else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
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
                                .clickable { onToggleSubTask(st) },
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
                        IconButton(onClick = { onDeleteSubTask(st) }, modifier = Modifier.size(28.dp)) {
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
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                )
                IconButton(
                    onClick = {
                        if (newSubTaskText.isNotBlank()) {
                            onAddSubTask(newSubTaskText)
                            newSubTaskText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(CandyPrimary),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.today_subtask_add_cd), tint = Color.White)
                }
            }
        }
        Button(
            onClick = { onSave(title, note.takeIf { it.isNotBlank() }, LocalDate.parse(task.plannedDate), time.takeIf { it.isNotBlank() }, category.takeIf { it.isNotBlank() }, priority) },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(CandyPrimary, Color.White),
        ) { Text(stringResource(R.string.action_save)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedSmallButton(stringResource(R.string.today_move_tomorrow_btn), Modifier.weight(1f), onMoveTomorrow)
            OutlinedSmallButton(stringResource(R.string.today_delete_label), Modifier.weight(1f)) { showDeleteConfirm = true }
        }
        Spacer(Modifier.height(12.dp))
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
    var step by remember { mutableStateOf(0) }
    var mood by remember { mutableStateOf(3) }
    var energy by remember { mutableStateOf(3) }
    var note by remember { mutableStateOf("") }
    var bestMoment by remember { mutableStateOf("") }
    var challenge by remember { mutableStateOf("") }
    var tomorrowIntention by remember { mutableStateOf("") }
    var carryTasks by remember { mutableStateOf(overdueCount > 0) }

    val moodLabels = listOf(
        stringResource(R.string.today_close_step1_mood_very_bad),
        stringResource(R.string.today_close_step1_mood_bad),
        stringResource(R.string.today_close_step1_mood_normal),
        stringResource(R.string.today_close_step1_mood_good),
        stringResource(R.string.today_close_step1_mood_great),
    )
    val moodColors = listOf(StreakCoral, StreakCoral.copy(.65f), CandySecondary, CandyPrimary, CompletedGreen)

    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryTile("$completedCount / $totalCount", stringResource(R.string.today_close_step0_completed), CandyPrimary, Modifier.weight(1f))
                    SummaryTile("%${(progress * 100).toInt()}", stringResource(R.string.today_close_step0_success), CompletedGreen, Modifier.weight(1f))
                    if (overdueCount > 0) SummaryTile("$overdueCount", stringResource(R.string.today_close_step0_overdue), StreakCoral, Modifier.weight(1f))
                }
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary.copy(.10f))) {
                    Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary))
                }
            }
            1 -> {
                Text(stringResource(R.string.today_close_step1_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
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
                OutlinedTextField(value = bestMoment, onValueChange = { bestMoment = it }, label = { Text(stringResource(R.string.today_close_step2_best_label)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = challenge, onValueChange = { challenge = it }, label = { Text(stringResource(R.string.today_close_step2_challenge_label)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.today_close_step2_note_label)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
            }
            3 -> {
                Text(stringResource(R.string.today_close_step3_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                OutlinedTextField(value = tomorrowIntention, onValueChange = { tomorrowIntention = it }, label = { Text(stringResource(R.string.today_close_step3_intent_label)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, shape = RoundedCornerShape(8.dp))
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
                            if (carryTasks) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
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
internal fun OutlinedSmallButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(42.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(text, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun String.sanitizedTimeInput(): String = filter { it.isDigit() || it == ':' }.take(5)
