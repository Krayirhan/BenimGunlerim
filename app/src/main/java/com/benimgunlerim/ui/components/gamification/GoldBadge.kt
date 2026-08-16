package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun GoldBadge(gold: Int, modifier: Modifier = Modifier) {
    val containerColor = Color(0xFFFFD54F) // Bright Amber Gold
    val contentColor = Color(0xFF5D4037)   // Deep Warm Dark Brown for high contrast

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.pill))
            .background(containerColor)
            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs + 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
    ) {
        Text(
            text = "🪙",
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = "$gold",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}
