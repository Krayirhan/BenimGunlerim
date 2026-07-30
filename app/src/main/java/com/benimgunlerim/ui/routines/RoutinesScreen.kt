package com.benimgunlerim.ui.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.data.shortTr
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.domain.normalizedTimeOrNull
import com.benimgunlerim.ui.theme.AccentPurple
import com.benimgunlerim.ui.theme.AccentPurpleSoft
import com.benimgunlerim.ui.theme.AccentSky
import com.benimgunlerim.ui.theme.AccentSkySoft
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandyPrimaryDark
import com.benimgunlerim.ui.theme.CandyPrimaryLight
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit = {},
) {
    val routines by viewModel.routines.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingRoutineId by remember { mutableStateOf<String?>(null) }
    var editedRoutineName by remember { mutableStateOf("") }
    var editedRoutineDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var editedRoutineTime by remember { mutableStateOf("") }

    if (showAddSheet) {
        AddRoutineSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, days, time, targetType, targetValue, targetUnit ->
                viewModel.addRoutine(name, days, time, targetType, targetValue, targetUnit)
                showAddSheet = false
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = CandyPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp).testTag(com.benimgunlerim.ui.TestTags.RoutinesFab),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.routines_add_cd))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(com.benimgunlerim.ui.TestTags.RoutinesRoot)
                .background(RoutinesBackground())
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = 18.dp,
                end = 16.dp,
                bottom = 110.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "definition") {
                RoutineDefinitionCard()
            }

            if (routines.isEmpty()) {
                item(key = "empty") {
                    EmptyRoutinesCard(onAddRoutine = { showAddSheet = true })
                }
            } else {
                item(key = "section_header") {
                    com.benimgunlerim.ui.components.SectionHeader(
                        title = stringResource(R.string.routines_section_title),
                        subtitle = stringResource(R.string.routines_section_subtitle),
                        accentColor = CandyPrimary,
                    )
                }
                items(
                    items = routines,
                    key = { it.id },
                ) { item ->
                    RoutineItemCard(
                        item = item,
                        isEditing = editingRoutineId == item.id,
                        editedName = editedRoutineName,
                        editedDays = editedRoutineDays,
                        editedTime = editedRoutineTime,
                        onEdit = {
                            editingRoutineId = item.id
                            editedRoutineName = item.name
                            editedRoutineDays = item.targetDays
                            editedRoutineTime = item.preferredTime.orEmpty()
                        },
                        onEditedNameChange = { editedRoutineName = it },
                        onEditedDaysChange = { editedRoutineDays = it },
                        onEditedTimeChange = { editedRoutineTime = it.sanitizedTimeInput() },
                        onCancelEdit = { editingRoutineId = null },
                        onSaveEdit = {
                            viewModel.updateRoutine(
                                routineId = item.id,
                                name = editedRoutineName,
                                targetDays = editedRoutineDays,
                                preferredTime = editedRoutineTime.normalizedTimeOrNull(),
                            )
                            editingRoutineId = null
                        },
                        onArchive = {
                            viewModel.archiveRoutine(item.id)
                            if (editingRoutineId == item.id) editingRoutineId = null
                        },
                        onSkip = { viewModel.skipRoutine(item.id) },
                        onDetail = { onNavigateToDetail(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutinesBackground(): Brush = com.benimgunlerim.ui.components.ScreenBackground()

@Composable
private fun RoutineDefinitionCard() {
    BaseCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentPurpleSoft),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(4.dp, AccentPurple, CircleShape),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = stringResource(R.string.routines_definition_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.routines_definition_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyRoutinesCard(onAddRoutine: () -> Unit) {
    BaseCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .height(86.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(CandyPrimaryLight, AccentSkySoft, AccentPurpleSoft),
                        ),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .height((26 + index * 8).dp)
                                .width(20.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    listOf(CandyPrimary, AccentSky, AccentPurple, CandyPrimaryDark)[index]
                                        .copy(alpha = 0.78f),
                                ),
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.routines_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.routines_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onAddRoutine,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.routines_empty_cta))
            }
        }
    }
}


@Composable
@Suppress("LongParameterList")
private fun RoutineItemCard(
    item: RoutineCardUi,
    isEditing: Boolean,
    editedName: String,
    editedDays: Set<DayOfWeek>,
    editedTime: String,
    onEdit: () -> Unit,
    onEditedNameChange: (String) -> Unit,
    onEditedDaysChange: (Set<DayOfWeek>) -> Unit,
    onEditedTimeChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onArchive: () -> Unit,
    onSkip: () -> Unit,
    onDetail: () -> Unit,
) {
    BaseCard(contentPadding = 0.dp) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(if (isEditing) 330.dp else 178.dp)
                    .background(CandyPrimary),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (isEditing) {
                    RoutineEditContent(
                        name = editedName,
                        days = editedDays,
                        time = editedTime,
                        onNameChange = onEditedNameChange,
                        onDaysChange = onEditedDaysChange,
                        onTimeChange = onEditedTimeChange,
                        onCancel = onCancelEdit,
                        onSave = onSaveEdit,
                    )
                } else {
                    RoutineViewContent(
                        item = item,
                        onEdit = onEdit,
                        onArchive = onArchive,
                        onSkip = onSkip,
                        onDetail = onDetail,
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("LongMethod")
private fun RoutineViewContent(
    item: RoutineCardUi,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onSkip: () -> Unit,
    onDetail: () -> Unit,
) {
    val daysText = formatRoutineDays(item.targetDays)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RoutineStreakPill(streak = item.currentStreak)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!item.preferredTime.isNullOrBlank()) {
                InfoPill(
                    text = stringResource(R.string.routines_time_label, item.preferredTime!!),
                    container = AccentSkySoft,
                    content = AccentSky,
                )
            }
            InfoPill(
                text = if (item.targetDays.size == 7) {
                    stringResource(R.string.routines_every_day)
                } else {
                    stringResource(R.string.routines_days_count, item.targetDays.size)
                },
                container = CandyPrimaryLight,
                content = CandyPrimary,
            )
        }

        WeekConsistencyStrip(completedDays = item.last7Days)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Skip today
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.routines_skip), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Detail / Edit / Archive
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDetail) {
                    Text(stringResource(R.string.routines_detail), style = MaterialTheme.typography.labelMedium, color = CandyPrimary)
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.routines_edit_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = stringResource(R.string.routines_archive_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineEditContent(
    name: String,
    days: Set<DayOfWeek>,
    time: String,
    onNameChange: (String) -> Unit,
    onDaysChange: (Set<DayOfWeek>) -> Unit,
    onTimeChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.routines_edit_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.routines_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
        )
        OutlinedTextField(
            value = time,
            onValueChange = onTimeChange,
            label = { Text(stringResource(R.string.routines_time_field_label)) },
            leadingIcon = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
        )
        Text(
            text = stringResource(R.string.routines_repeat_days_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        DaySelector(selectedDays = days, onChange = onDaysChange)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancel) {
                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.action_dismiss))
            }
            TextButton(onClick = onSave, enabled = name.isNotBlank()) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRoutineSheet(
    onDismiss: () -> Unit,
    onSave: (String, Set<DayOfWeek>, String?, String, Int?, String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(DayOfWeek.entries.toSet()) }
    var time by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(RoutineTargetType.CHECK.value) }
    var targetValueText by remember { mutableStateOf("") }
    var targetUnit by remember { mutableStateOf("") }

    val goalTypes = listOf(
        RoutineTargetType.CHECK.value to stringResource(R.string.routines_goal_checkbox),
        "count" to stringResource(R.string.routines_goal_counter),
        "amount" to stringResource(R.string.routines_goal_amount),
        "duration" to stringResource(R.string.routines_goal_duration),
        "limit" to stringResource(R.string.routines_goal_limit),
        "negative" to stringResource(R.string.routines_goal_avoid),
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.routines_add_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.routines_add_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.routines_name_label)) },
                placeholder = { Text(stringResource(R.string.routines_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            // Goal Type Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.routines_goal_type_label), style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(goalTypes.size) { i ->
                        val (type, label) = goalTypes[i]
                        val selected = targetType == type
                        FilterChip(
                            selected = selected,
                            onClick = { targetType = type },
                            label = { Text(label) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CandyPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }
            // Target Value & Unit (only for non-checkbox, non-negative)
            if (targetType != RoutineTargetType.CHECK.value && targetType != "negative") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetValueText,
                        onValueChange = { targetValueText = it.filter { c -> c.isDigit() } },
                        label = {
                            Text(if (targetType == "limit") stringResource(R.string.routines_goal_max_label) else stringResource(R.string.routines_goal_target_label))
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                    OutlinedTextField(
                        value = targetUnit,
                        onValueChange = { targetUnit = it },
                        label = {
                            Text(if (targetType == "duration") stringResource(R.string.routines_goal_unit_dk) else if (targetType == "amount") stringResource(R.string.routines_goal_unit_l) else stringResource(R.string.routines_goal_unit_generic))
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
            OutlinedTextField(
                value = time,
                onValueChange = { time = it.sanitizedTimeInput() },
                label = { Text(stringResource(R.string.routines_time_field_label)) },
                placeholder = { Text("09:00") },
                leadingIcon = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            Text(
                text = stringResource(R.string.routines_repeat_days_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            DaySelector(selectedDays = days, onChange = { days = it })
            Button(
                onClick = {
                    val tv = targetValueText.toIntOrNull()
                    onSave(
                        name.trim(),
                        days,
                        time.normalizedTimeOrNull(),
                        targetType,
                        tv,
                        targetUnit.takeIf { it.isNotBlank() },
                    )
                    name = ""
                    time = ""
                    days = DayOfWeek.entries.toSet()
                    targetType = RoutineTargetType.CHECK.value
                    targetValueText = ""
                    targetUnit = ""
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
            ) {
                Text(stringResource(R.string.routines_save_btn))
            }
        }
    }
}

@Composable
private fun RoutineStreakPill(streak: Int) {
    val text = if (streak > 0) stringResource(R.string.routines_streak_days, streak) else stringResource(R.string.routines_streak_waiting)
    val container = if (streak > 0) CandyPrimaryLight else MaterialTheme.colorScheme.surfaceVariant
    val content = if (streak > 0) CandyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    InfoPill(text = text, container = container, content = content)
}

@Composable
private fun InfoPill(text: String, container: Color, content: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@Composable
private fun WeekConsistencyStrip(completedDays: List<Boolean>) {
    val labels = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.routines_last7),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            completedDays.take(7).forEachIndexed { index, completed ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .height(if (completed) 28.dp else 14.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(if (completed) CandyPrimary else MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Text(
                        text = labels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DaySelector(selectedDays: Set<DayOfWeek>, onChange: (Set<DayOfWeek>) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DayOfWeek.entries.forEach { day ->
            val selected = day in selectedDays
            FilterChip(
                selected = selected,
                onClick = {
                    val next = if (selected) selectedDays - day else selectedDays + day
                    onChange(next.ifEmpty { setOf(day) })
                },
                label = { Text(day.shortTr()) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = CandyPrimary,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = CandyPrimary,
                ),
            )
        }
    }
}

@Composable
private fun BaseCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(com.benimgunlerim.ui.theme.AppTokens.Radius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

private fun formatRoutineDays(days: Set<DayOfWeek>): String {
    if (days.size == DayOfWeek.entries.size) return "Her gün tekrar eder"
    return days.sorted().joinToString(" · ") { it.shortTr() }
}

private fun String.sanitizedTimeInput(): String = filter { it.isDigit() || it == ':' }.take(5)
