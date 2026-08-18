package com.benimgunlerim.ui.components.calm

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft

private const val BREATH_CYCLE_DURATION_MS = 4500
private const val PULSE_SCALE_MIN = 0.82f
private const val PULSE_SCALE_MAX = 1.25f

@Composable
internal fun BreathingCircle(
    isExhaling: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath_scale")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = PULSE_SCALE_MIN,
        targetValue = PULSE_SCALE_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = BREATH_CYCLE_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathing_circle",
    )

    Box(
        modifier = modifier
            .size(AppTokens.Calm.breathingCircleOuter)
            .scale(breathingScale)
            .clip(CircleShape)
            .background(BrandPrimarySoft.copy(alpha = 0.7f))
            .border(AppTokens.Calm.breathingBorder, BrandPrimary.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(AppTokens.Calm.breathingCircleInner)
                .clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (!isExhaling) stringResource(R.string.reset_breathe_in) else stringResource(R.string.reset_breathe_out),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = BrandPrimary,
            )
        }
    }
}
