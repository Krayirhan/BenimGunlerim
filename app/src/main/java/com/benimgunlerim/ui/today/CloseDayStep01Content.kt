@file:Suppress("LongParameterList", "MagicNumber")

package com.benimgunlerim.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral

@Composable
internal fun CloseDayStep0Summary(completedCount: Int, totalCount: Int, progress: Float, overdueCount: Int) {
    Text(stringResource(R.string.today_close_step0_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
    Text(
        stringResource(R.string.today_close_step0_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (totalCount == 0) {
        Text(
            stringResource(R.string.today_close_step0_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryTile("$completedCount / $totalCount", stringResource(R.string.today_close_step0_completed), CandyPrimary, Modifier.weight(1f))
            SummaryTile("%${(progress * 100).toInt()}", stringResource(R.string.today_close_step0_success), CompletedGreen, Modifier.weight(1f))
            if (overdueCount > 0) SummaryTile("$overdueCount", stringResource(R.string.today_close_step0_overdue), StreakCoral, Modifier.weight(1f))
        }
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary.copy(.10f))) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(8.dp).clip(RoundedCornerShape(99.dp)).background(CandySecondary))
        }
    }
}

@Composable
internal fun CloseDayStep1MoodEnergy(
    mood: Int,
    onMoodChange: (Int) -> Unit,
    energy: Int,
    onEnergyChange: (Int) -> Unit,
    moodLabels: List<String>,
    moodColors: List<Color>,
) {
    Text(stringResource(R.string.today_close_step1_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
    Text(
        stringResource(R.string.today_close_step1_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.today_close_step1_mood_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            moodColors.forEachIndexed { i, col ->
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (mood == i) col else col.copy(.14f))
                        .border(1.dp, if (mood == i) col else col.copy(.20f), RoundedCornerShape(10.dp))
                        .clickable { onMoodChange(i) },
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
                        .clickable { onEnergyChange(e) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$e", style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (energy == e) FontWeight.ExtraBold else FontWeight.Normal), color = if (energy == e) Color.White else LevelSky)
                }
            }
        }
    }
}
