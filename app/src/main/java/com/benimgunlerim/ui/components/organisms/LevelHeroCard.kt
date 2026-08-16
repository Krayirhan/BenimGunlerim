package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.gamification.GoldBadge
import com.benimgunlerim.ui.components.gamification.StreakBadge
import com.benimgunlerim.ui.components.molecules.ProgressBar
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun LevelHeroCard(
    level: Int,
    levelTitle: String,
    currentXp: Int,
    maxXp: Int,
    streak: Int,
    gold: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (maxXp > 0) (currentXp.toFloat() / maxXp.toFloat()).coerceIn(0f, 1f) else 0f

    val heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFF0D5C3A), // Deep Emerald Green
            Color(0xFF147A4F), // Vibrant Forest Green
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.xxl))
            .background(heroGradient)
            .padding(AppTokens.Spacing.cardInnerHero),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.progress_level, level),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                        text = levelTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs)) {
                    StreakBadge(streak = streak)
                    GoldBadge(gold = gold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.progress_level_progress_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                        text = "$currentXp / $maxXp XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                ProgressBar(
                    progress = progress,
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                )
            }
        }
    }
}
