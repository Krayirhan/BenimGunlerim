package com.benimgunlerim.ui.components.calm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import java.util.Locale

private const val SHORT_RESET_SECONDS = 30
private const val LONG_RESET_SECONDS = 60
private const val SECONDS_PER_MINUTE = 60

@Composable
internal fun ResetIntroContent(
    selectedDuration: Int,
    onDurationSelect: (Int) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.reset_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )

        Spacer(modifier = Modifier.height(AppTokens.Spacing.lg + AppTokens.Spacing.xs))

        Row(horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs + AppTokens.Spacing.xxs / 2)) {
            listOf(SHORT_RESET_SECONDS, LONG_RESET_SECONDS).forEach { sec ->
                val isSelected = selectedDuration == sec
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppTokens.Radius.md))
                        .background(if (isSelected) BrandPrimarySoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            if (isSelected) AppTokens.BorderWidth.thin + AppTokens.BorderWidth.thin / 2 else AppTokens.Spacing.none,
                            if (isSelected) BrandPrimary else Color.Transparent,
                            RoundedCornerShape(AppTokens.Radius.md),
                        )
                        .clickable { onDurationSelect(sec) }
                        .padding(horizontal = AppTokens.Spacing.lg, vertical = AppTokens.Spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (sec == SHORT_RESET_SECONDS) {
                            stringResource(R.string.reset_duration_30s)
                        } else {
                            stringResource(R.string.reset_duration_60s)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTokens.Spacing.xxl))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(AppTokens.Calm.resetButtonHeight),
            shape = RoundedCornerShape(AppTokens.Calm.resetButtonRadius),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
        ) {
            Text(
                text = stringResource(R.string.reset_start),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }
    }
}

@Composable
internal fun ResetBreathingContent(
    remainingSeconds: Int,
    isExhaling: Boolean,
    onCancel: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = String.format(
                Locale.ROOT,
                "%02d:%02d",
                remainingSeconds / SECONDS_PER_MINUTE,
                remainingSeconds % SECONDS_PER_MINUTE,
            ),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = BrandPrimary,
        )

        Spacer(modifier = Modifier.height(AppTokens.Spacing.xxl + AppTokens.Spacing.xs))

        BreathingCircle(isExhaling = isExhaling)

        Spacer(modifier = Modifier.height(AppTokens.Spacing.xxl + AppTokens.Spacing.lg / 2))

        TextButton(onClick = onCancel) {
            Text(
                text = stringResource(R.string.action_cancel),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Suppress("LongMethod")
@Composable
internal fun ResetCompletedContent(
    onPickTask: () -> Unit,
    onEnableLightDay: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.size(AppTokens.Calm.resetCompletedIcon).clip(CircleShape).background(BrandPrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(AppTokens.Calm.resetCompletedIconSize),
            )
        }

        Spacer(modifier = Modifier.height(AppTokens.Spacing.md))

        Text(
            text = stringResource(R.string.reset_completed_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(AppTokens.Spacing.xxs + AppTokens.Spacing.xxs / 2))
        Text(
            text = stringResource(R.string.reset_completed_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AppTokens.Spacing.lg + AppTokens.Spacing.xs))

        Button(
            onClick = onPickTask,
            modifier = Modifier.fillMaxWidth().height(AppTokens.TouchTarget.min),
            shape = RoundedCornerShape(AppTokens.Radius.md),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
        ) {
            Text(
                text = stringResource(R.string.reset_pick_task),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(AppTokens.Spacing.xs + AppTokens.Spacing.xxs / 2))

        OutlinedButton(
            onClick = onEnableLightDay,
            modifier = Modifier.fillMaxWidth().height(AppTokens.TouchTarget.min),
            shape = RoundedCornerShape(AppTokens.Radius.md),
        ) {
            Text(
                text = stringResource(R.string.reset_light_day),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = BrandPrimary,
            )
        }

        Spacer(modifier = Modifier.height(AppTokens.Spacing.xs))

        TextButton(onClick = onDismiss) {
            Text(
                text = stringResource(R.string.reset_continue),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
