package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.XpGold

@Composable
fun GoldBadge(gold: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.pill))
            .background(XpGold.copy(alpha = 0.15f))
            .padding(horizontal = AppTokens.Spacing.xs, vertical = AppTokens.Spacing.xxs + 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(XpGold),
        )
        Text(
            text = "$gold",
            style = MaterialTheme.typography.labelLarge,
            color = XpGold,
        )
    }
}
