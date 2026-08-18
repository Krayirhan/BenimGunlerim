package com.benimgunlerim.ui.components.calm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import kotlinx.coroutines.delay

private enum class ResetState {
    INTRO,
    BREATHING,
    COMPLETED,
}

private const val FADE_IN_DURATION = 250
private const val FADE_OUT_DURATION = 200
private const val CYCLE_DURATION_SECONDS = 10
private const val EXHALE_START_SECOND = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetDialog(
    onDismiss: () -> Unit,
    onEnableLightDay: () -> Unit,
    onPickTask: () -> Unit,
    initialDurationSeconds: Int = 60,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentState by remember { mutableStateOf(ResetState.INTRO) }
    var selectedDuration by remember { mutableIntStateOf(initialDurationSeconds) }
    var remainingSeconds by remember { mutableIntStateOf(initialDurationSeconds) }
    var isExhaling by remember { mutableStateOf(false) }

    LaunchedEffect(currentState) {
        if (currentState == ResetState.BREATHING) {
            while (remainingSeconds > 0 && currentState == ResetState.BREATHING) {
                val cycleSecond = (selectedDuration - remainingSeconds) % CYCLE_DURATION_SECONDS
                isExhaling = cycleSecond >= EXHALE_START_SECOND
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds <= 0) {
                currentState = ResetState.COMPLETED
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = AppTokens.Radius.xxl, topEnd = AppTokens.Radius.xxl),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                ) {
                    Box(
                        modifier = Modifier.size(AppTokens.IconSize.lg).clip(CircleShape).background(BrandPrimarySoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(AppTokens.Calm.resetTitleIconSize),
                        )
                    }
                    Text(
                        text = stringResource(R.string.reset_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.action_cancel),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTokens.Spacing.md))

            AnimatedContent(
                targetState = currentState,
                transitionSpec = { fadeIn(tween(FADE_IN_DURATION)) togetherWith fadeOut(tween(FADE_OUT_DURATION)) },
                label = "reset_state_transition",
            ) { state ->
                when (state) {
                    ResetState.INTRO -> ResetIntroContent(
                        selectedDuration = selectedDuration,
                        onDurationSelect = {
                            selectedDuration = it
                            remainingSeconds = it
                        },
                        onStart = {
                            remainingSeconds = selectedDuration
                            currentState = ResetState.BREATHING
                        },
                    )
                    ResetState.BREATHING -> ResetBreathingContent(
                        remainingSeconds = remainingSeconds,
                        isExhaling = isExhaling,
                        onCancel = { currentState = ResetState.COMPLETED },
                    )
                    ResetState.COMPLETED -> ResetCompletedContent(
                        onPickTask = {
                            onPickTask()
                            onDismiss()
                        },
                        onEnableLightDay = {
                            onEnableLightDay()
                            onDismiss()
                        },
                        onDismiss = onDismiss,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTokens.Spacing.sm))
        }
    }
}
