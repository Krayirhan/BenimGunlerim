@file:Suppress(
    "LongParameterList",
    "LongMethod",
    "MagicNumber",
)
package com.benimgunlerim.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val MAX_COLLAPSED_OVERDUE_TASKS = 5

@Composable
fun DayTasksCard(
    date: LocalDate,
    tasks: List<PlanTaskUi>,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val dateLabel = date.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR")))
    val daySectionTitle = stringResource(R.string.plan_day_section_title, dateLabel)
    val taskCount = stringResource(R.string.plan_task_count, tasks.size)
    val addAction = stringResource(R.string.plan_add_action)
    val emptyState = stringResource(R.string.plan_empty_state)
    PlanSectionShell(
        title = daySectionTitle,
        badge = taskCount,
        color = LevelSky,
        actionLabel = addAction,
        onAction = onAdd,
    ) {
        if (tasks.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LevelSky.copy(.08f))
                    .border(1.dp, LevelSky.copy(.14f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text(
                    emptyState,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            tasks.forEach { task ->
                PlanTaskRow(
                    task = task,
                    accentColor = LevelSky,
                    onToggle = onToggle,
                    onDelete = onDelete,
                    onOpen = onOpen,
                    dateLabel = null,
                    rowTag = TestTags.planTaskRow(task.id),
                    secondaryAction = null,
                )
            }
        }
    }
}

@Composable
fun PlanOverdueCard(
    tasks: List<PlanTaskUi>,
    today: LocalDate,
    onToggle: (String) -> Unit,
    onMoveToSelected: (String) -> Unit,
    onMoveAllToday: () -> Unit,
    onMoveAllTomorrow: () -> Unit,
    onMoveAllSelected: () -> Unit,
    onDelete: (String) -> Unit,
    onOpen: (String) -> Unit,
) {
    var showAll by remember { mutableStateOf(false) }
    val overdueTitle = stringResource(R.string.plan_overdue_title)
    val overdueCount = stringResource(R.string.plan_overdue_count, tasks.size)
    val visibleTasks = if (showAll) tasks else tasks.take(MAX_COLLAPSED_OVERDUE_TASKS)
    PlanSectionShell(
        title = overdueTitle,
        badge = overdueCount,
        color = StreakCoral,
        actionLabel = null,
        onAction = null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PlanOverdueActionButton(
                    text = stringResource(R.string.plan_overdue_move_today),
                    modifier = Modifier.weight(1f),
                    onClick = onMoveAllToday,
                )
                PlanOverdueActionButton(
                    text = stringResource(R.string.plan_overdue_move_tomorrow),
                    modifier = Modifier.weight(1f),
                    onClick = onMoveAllTomorrow,
                )
            }
            PlanOverdueActionButton(
                text = stringResource(R.string.plan_overdue_move_selected),
                modifier = Modifier.fillMaxWidth(),
                onClick = onMoveAllSelected,
            )
        }
        visibleTasks.forEach { task ->
            PlanTaskRow(
                task = task,
                accentColor = StreakCoral,
                onToggle = onToggle,
                onDelete = onDelete,
                onOpen = onOpen,
                dateLabel = task.relativePlannedDateLabel(today),
                rowTag = TestTags.planOverdueRow(task.id),
                secondaryAction = { onMoveToSelected(task.id) },
            )
        }
        if (tasks.size > MAX_COLLAPSED_OVERDUE_TASKS) {
            TextButton(onClick = { showAll = !showAll }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (showAll) {
                        stringResource(R.string.plan_overdue_show_less)
                    } else {
                        stringResource(R.string.plan_overdue_show_all, tasks.size)
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = StreakCoral,
                )
            }
        }
    }
}

@Composable
private fun PlanOverdueActionButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StreakCoral.copy(.12f))
            .border(1.dp, StreakCoral.copy(.30f), RoundedCornerShape(8.dp)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = StreakCoral,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlanTaskRow(
    task: PlanTaskUi,
    accentColor: Color,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onOpen: (String) -> Unit,
    dateLabel: String? = task.plannedDate,
    rowTag: String,
    secondaryAction: (() -> Unit)?,
) {
    val done = task.isCompleted
    val toggleDescription = if (done) {
        stringResource(R.string.today_done_cd)
    } else {
        stringResource(R.string.today_mark_done_label)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(rowTag)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .semantics {
                    contentDescription = toggleDescription
                }
                .clickable { onToggle(task.id) },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (done) accentColor else accentColor.copy(.10f))
                    .border(1.5.dp, accentColor.copy(if (done) 0f else .40f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (done) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onOpen(task.id) },
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                ),
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!dateLabel.isNullOrBlank()) {
                Text(
                    dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.8f),
                )
            }
            if (!task.startTime.isNullOrBlank() || !task.category.isNullOrBlank() || task.priority == 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.startTime?.takeIf { it.isNotBlank() }?.let { PlanMetaPill(it, LevelSky) }
                    task.category?.takeIf { it.isNotBlank() }?.let { PlanMetaPill(it, CandySecondary) }
                    if (task.priority == 1) {
                        PlanMetaPill(stringResource(R.string.today_priority_short_high), StreakCoral)
                    }
                }
            }
        }
        if (secondaryAction != null) {
            IconButton(onClick = secondaryAction, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = stringResource(R.string.plan_move_to_selected_day_cd),
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        IconButton(onClick = { onDelete(task.id) }, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun PlanMetaPill(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(.12f))
            .border(1.dp, color.copy(.22f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlanSectionShell(
    title: String,
    badge: String,
    color: Color,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(.12f))
                            .border(1.dp, color.copy(.22f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = color,
                        )
                    }
                }
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            actionLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = color,
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun PlanTaskUi.relativePlannedDateLabel(today: LocalDate): String {
    val planned = runCatching { LocalDate.parse(plannedDate.orEmpty()) }.getOrNull()
        ?: return plannedDate.orEmpty()
    val daysAgo = ChronoUnit.DAYS.between(planned, today)
    return when {
        daysAgo == 1L -> stringResource(R.string.label_yesterday)
        daysAgo in 2L..6L -> stringResource(R.string.today_relative_days_ago, daysAgo)
        else -> plannedDate.orEmpty()
    }
}
