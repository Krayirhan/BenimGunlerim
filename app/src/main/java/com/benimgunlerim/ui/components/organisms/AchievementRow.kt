package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppBadge
import com.benimgunlerim.ui.components.core.AppBadgeVariant
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.molecules.ProgressBar
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun AchievementRow(
    title: String,
    description: String,
    icon: String,
    isUnlocked: Boolean,
    progress: Float,
    progressText: String,
    modifier: Modifier = Modifier,
    unlockedDate: String? = null,
) {
    AppSurface(
        modifier = modifier.fillMaxWidth(),
        radius = AppTokens.Radius.md,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineLarge,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isUnlocked) {
                        AppBadge(
                            text = unlockedDate ?: stringResource(R.string.achievement_row_unlocked_label),
                            variant = AppBadgeVariant.Success,
                        )
                    } else {
                        AppBadge(
                            text = progressText,
                            variant = AppBadgeVariant.Neutral,
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!isUnlocked) {
                    ProgressBar(
                        progress = progress,
                        height = AppTokens.Spacing.xxs,
                    )
                }
            }
        }
    }
}
