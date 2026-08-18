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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.StreakCoral

private const val OVERDUE_BG_ALPHA = 0.10f
private const val OVERDUE_BORDER_ALPHA = 0.25f
private const val INACTIVE_CHECK_ALPHA = 0.5f
private val FieldCornerRadius = 8.dp
private val OverdueCardCornerRadius = 12.dp

@Composable
internal fun CloseDayStep2Reflection(
    bestMoment: String,
    onBestMomentChange: (String) -> Unit,
    challenge: String,
    onChallengeChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onNoteDone: () -> Unit,
) {
    Text(stringResource(R.string.today_close_step2_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
    Text(
        stringResource(R.string.today_close_step2_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    OutlinedTextField(
        value = bestMoment,
        onValueChange = onBestMomentChange,
        label = { Text(stringResource(R.string.today_close_step2_best_label)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
        shape = RoundedCornerShape(FieldCornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    OutlinedTextField(
        value = challenge,
        onValueChange = onChallengeChange,
        label = { Text(stringResource(R.string.today_close_step2_challenge_label)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
        shape = RoundedCornerShape(FieldCornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text(stringResource(R.string.today_close_step2_note_label)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
        shape = RoundedCornerShape(FieldCornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onNoteDone() }),
    )
}

@Composable
internal fun CloseDayStep3IntentionAndCarry(
    tomorrowIntention: String,
    onTomorrowIntentionChange: (String) -> Unit,
    onIntentionDone: () -> Unit,
    overdueCount: Int,
    carryTasks: Boolean,
    onCarryTasksToggle: () -> Unit,
) {
    val suggestionOverdue = stringResource(R.string.today_close_step3_suggestion_overdue)
    val suggestionConsistency = stringResource(R.string.today_close_step3_suggestion_consistency)
    val suggestionFocus = stringResource(R.string.today_close_step3_suggestion_focus)
    val suggestions = buildList {
        if (overdueCount > 0) add(suggestionOverdue)
        add(suggestionConsistency)
        add(suggestionFocus)
    }.distinct()
    val carryCheckboxCd = stringResource(R.string.today_carry_checkbox_cd)
    Text(stringResource(R.string.today_close_step3_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
    Text(
        stringResource(R.string.today_close_step3_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    OutlinedTextField(
        value = tomorrowIntention,
        onValueChange = onTomorrowIntentionChange,
        label = { Text(stringResource(R.string.today_close_step3_intent_label)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
        shape = RoundedCornerShape(FieldCornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onIntentionDone() }),
    )
    Text(
        stringResource(R.string.today_close_step3_suggestion_title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        suggestions.take(2).forEach { suggestion ->
            OutlinedSmallButton(
                text = suggestion,
                modifier = Modifier.weight(1f),
                onClick = { onTomorrowIntentionChange(suggestion) },
            )
        }
    }
    if (overdueCount > 0) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(OverdueCardCornerRadius))
                .background(if (carryTasks) StreakCoral.copy(alpha = OVERDUE_BG_ALPHA) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (carryTasks) StreakCoral.copy(alpha = OVERDUE_BORDER_ALPHA)
                    else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(OverdueCardCornerRadius),
                )
                .clickable(onClick = onCarryTasksToggle)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(if (carryTasks) StreakCoral else MaterialTheme.colorScheme.outline.copy(alpha = INACTIVE_CHECK_ALPHA)), contentAlignment = Alignment.Center) {
                if (carryTasks) Icon(Icons.Rounded.Check, contentDescription = carryCheckboxCd, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.today_close_step3_move_overdue, overdueCount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(stringResource(R.string.today_close_step3_move_overdue_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
