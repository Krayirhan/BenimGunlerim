package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.gamification.LevelBadge
import com.benimgunlerim.ui.components.gamification.StreakBadge
import com.benimgunlerim.ui.components.gamification.XpBadge
import com.benimgunlerim.ui.components.molecules.ProgressBar
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun HeaderProgressCard(
    streakCount: Int,
    completedTasks: Int,
    totalTasks: Int,
    level: Int,
    xp: Int,
    modifier: Modifier = Modifier,
    isLightDayMode: Boolean = false,
    levelTitle: String = stringResource(R.string.today_level_default_title),
    onMoodClick: (() -> Unit)? = null,
) {
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val percent = (progress * 100).toInt()

    val subtitleText = if (isLightDayMode) {
        if (completedTasks == 0) {
            stringResource(R.string.light_day_progress_steps)
        } else {
            stringResource(R.string.light_day_progress_done, completedTasks)
        }
    } else {
        val statusText = if (totalTasks == 0) {
            stringResource(R.string.today_status_no_steps)
        } else if (completedTasks == 0) {
            stringResource(R.string.today_status_none_completed)
        } else if (completedTasks == totalTasks) {
            stringResource(R.string.today_status_all_completed)
        } else {
            stringResource(R.string.today_support_steps_remaining, totalTasks - completedTasks)
        }
        val countText = stringResource(R.string.today_status_completed_count, completedTasks, totalTasks)
        "$statusText · $countText"
    }

    AppSurface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onMoodClick != null) Modifier.clickable(onClick = onMoodClick) else Modifier),
        radius = AppTokens.Radius.lg,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.today_progress_header_question),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.today_status_percent, percent),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ProgressBar(progress = progress)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LevelBadge(level = level, title = levelTitle)
                StreakBadge(streak = streakCount)
                XpBadge(xp = xp)
            }
        }
    }
}
