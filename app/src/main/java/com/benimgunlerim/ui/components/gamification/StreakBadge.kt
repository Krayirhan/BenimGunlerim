package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral

@Composable
fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    val brush = when {
        streak >= 30 -> Brush.linearGradient(listOf(LevelSky, CandyPrimary))
        streak >= 7  -> Brush.linearGradient(listOf(StreakCoral, Color(0xFFFF8A65)))
        else         -> Brush.linearGradient(listOf(StreakCoral.copy(alpha = 0.8f), StreakCoral))
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.pill))
            .background(brush)
            .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.xs - 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$streak günlük seri",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}
