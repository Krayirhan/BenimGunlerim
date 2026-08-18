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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.StreakCoral

private const val SELECTED_BG_ALPHA = 0.15f
private val OptionCornerRadius = 8.dp

@Composable
internal fun TaskDetailOptionsSection(
    interactionLocked: Boolean,
    reminderCanBeEnabled: Boolean,
    effectiveReminderEnabled: Boolean,
    onReminderEnabledChange: (Boolean) -> Unit,
    priority: Int,
    priorityLabels: List<String>,
    onPriorityChange: (Int) -> Unit,
) {
    SheetSectionHeader(stringResource(R.string.today_detail_section_options))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.today_reminder_switch_label), style = MaterialTheme.typography.bodyMedium)
            Text(
                when {
                    !reminderCanBeEnabled -> stringResource(R.string.today_reminder_switch_desc_needs_time)
                    effectiveReminderEnabled -> stringResource(R.string.today_reminder_switch_desc_on)
                    else -> stringResource(R.string.today_reminder_switch_desc_off)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = effectiveReminderEnabled,
            enabled = !interactionLocked && reminderCanBeEnabled,
            onCheckedChange = onReminderEnabledChange,
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.today_add_task_priority_label), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3).forEachIndexed { i, p ->
                val sel = priority == p
                val col = if (i == 0) StreakCoral else if (i == 1) CandyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(OptionCornerRadius))
                        .background(if (sel) col.copy(alpha = SELECTED_BG_ALPHA) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (sel) col else MaterialTheme.colorScheme.outline, RoundedCornerShape(OptionCornerRadius))
                        .clickable(enabled = !interactionLocked) { onPriorityChange(p) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        priorityLabels[i],
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (sel) FontWeight.ExtraBold else FontWeight.Normal),
                        color = if (sel) col else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
