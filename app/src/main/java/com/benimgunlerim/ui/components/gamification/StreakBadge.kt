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
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.Streak
import com.benimgunlerim.ui.theme.StreakSoft

@Composable
fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.pill))
            .background(StreakSoft)
            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🔥 Seri $streak gün",
            style = MaterialTheme.typography.labelMedium,
            color = Streak,
        )
    }
}

