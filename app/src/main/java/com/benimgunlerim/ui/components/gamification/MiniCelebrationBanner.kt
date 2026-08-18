package com.benimgunlerim.ui.components.gamification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.SuccessSoft

/**
 * 2. Kademe Küçük Kutlama: Mini Motivasyon Banner'ı (2-3 sn belirip kaybolur)
 */
@Composable
fun MiniCelebrationBanner(
    message: String,
    icon: String = "✨",
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTokens.Radius.lg))
                .background(SuccessSoft)
                .border(AppTokens.BorderWidth.thin, CompletedGreen.copy(alpha = 0.35f), RoundedCornerShape(AppTokens.Radius.lg))
                .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
            ) {
                Text(text = icon, fontSize = 18.sp)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = CompletedGreen,
                )
            }
        }
    }
}
