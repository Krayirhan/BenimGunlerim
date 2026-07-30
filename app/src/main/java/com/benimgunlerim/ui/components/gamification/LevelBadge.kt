package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun LevelBadge(level: Int, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.pill))
            .background(Brush.linearGradient(listOf(LevelSky, CandyPrimary)))
            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs + 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppTokens.Spacing.xxs),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
        Text(
            text = "Lv.$level $title",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}
