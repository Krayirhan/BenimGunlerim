package com.benimgunlerim.ui.components.calm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import kotlinx.coroutines.delay

private enum class ResetState {
    INTRO,
    BREATHING,
    COMPLETED,
}

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

    // Breathing cycle: 4s inhale, 6s exhale (total 10s per cycle)
    var isExhaling by remember { mutableStateOf(false) }

    LaunchedEffect(currentState) {
        if (currentState == ResetState.BREATHING) {
            while (remainingSeconds > 0 && currentState == ResetState.BREATHING) {
                val cycleSecond = (selectedDuration - remainingSeconds) % 10
                isExhaling = cycleSecond >= 4
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header / Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrandPrimarySoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(18.dp),
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
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.action_cancel),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentState,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "reset_state_transition",
            ) { state ->
                when (state) {
                    ResetState.INTRO -> {
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

                            Spacer(modifier = Modifier.height(28.dp))

                            // Duration selector
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                listOf(30, 60).forEach { sec ->
                                    val isSelected = selectedDuration == sec
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) BrandPrimarySoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .border(
                                                if (isSelected) 1.5.dp else 0.dp,
                                                if (isSelected) BrandPrimary else Color.Transparent,
                                                RoundedCornerShape(12.dp),
                                            )
                                            .clickable {
                                                selectedDuration = sec
                                                remainingSeconds = sec
                                            }
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (sec == 30) "30 saniye" else "1 dakika",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            ),
                                            color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = {
                                    remainingSeconds = selectedDuration
                                    currentState = ResetState.BREATHING
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
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

                    ResetState.BREATHING -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "breath_scale")
                        val breathingScale by infiniteTransition.animateFloat(
                            initialValue = 0.82f,
                            targetValue = 1.25f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 4500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "breathing_circle",
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary,
                            )

                            Spacer(modifier = Modifier.height(36.dp))

                            // Calming animated pulse circle
                            Box(
                                modifier = Modifier
                                    .size(180.dp)
                                    .scale(breathingScale)
                                    .clip(CircleShape)
                                    .background(BrandPrimarySoft.copy(alpha = 0.7f))
                                    .border(2.dp, BrandPrimary.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
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

                            Spacer(modifier = Modifier.height(40.dp))

                            TextButton(
                                onClick = { currentState = ResetState.COMPLETED },
                            ) {
                                Text(
                                    text = stringResource(R.string.action_cancel),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    ResetState.COMPLETED -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimarySoft),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(32.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.reset_completed_title),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.reset_completed_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Action 1: Bir Görev Seç
                            Button(
                                onClick = {
                                    onPickTask()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            ) {
                                Text(
                                    text = stringResource(R.string.reset_pick_task),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action 2: Bugünü Hafiflet
                            OutlinedButton(
                                onClick = {
                                    onEnableLightDay()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.reset_light_day),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrandPrimary,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action 3: Devam Et
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = stringResource(R.string.reset_continue),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
