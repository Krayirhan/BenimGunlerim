package com.benimgunlerim.ui.components.gamification

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.XpGold

@Composable
fun XpBadge(xp: Int, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "xpPulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(AppTokens.Motion.pulse),
            RepeatMode.Reverse,
        ),
        label = "xpScale",
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(AppTokens.Radius.pill))
            .background(Brush.linearGradient(listOf(XpGold, Color(0xFFFF8A65))))
            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs + 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$xp XP",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}
