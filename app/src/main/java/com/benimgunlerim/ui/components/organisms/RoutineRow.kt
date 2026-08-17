package com.benimgunlerim.ui.components.organisms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.gamification.StreakBadge
import com.benimgunlerim.ui.theme.AppTokens
import java.time.DayOfWeek
import java.time.LocalDate

import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun RoutineRow(
    title: String,
    isCompletedToday: Boolean,
    onToggle: () -> Unit,
    weekHistory: List<Boolean>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    streakCount: Int = 0,
    preferredTime: String? = null,
    targetDaysSummary: String? = null,
    targetDays: Set<DayOfWeek> = emptySet(),
    category: String? = null,
    targetType: String = "check",
    targetValue: Int? = null,
    targetUnit: String? = null,
    currentValue: Float = 0f,
    onProgressChange: ((Float) -> Unit)? = null,
    useSurface: Boolean = true,
    onClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val checkColor by animateColorAsState(
        targetValue = if (isCompletedToday) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = AppTokens.Motion.fast),
        label = "routine_check_color",
    )

    val handleToggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle()
    }

    val effectiveClick = onClick ?: handleToggle

    // Compute last 7 days day initials and date for habit matrix
    val today = remember { LocalDate.now() }
    val days7 = remember(today) {
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            date to (TurkishDayShortCode[date.dayOfWeek] ?: "")
        }
    }

    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(vertical = AppTokens.Spacing.xs, horizontal = if (useSurface) AppTokens.Spacing.cardInner else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            IconButton(
                onClick = handleToggle,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { role = Role.Checkbox },
            ) {
                Icon(
                    imageVector = if (isCompletedToday) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isCompletedToday) {
                        stringResource(R.string.routine_row_mark_incomplete_cd)
                    } else {
                        stringResource(R.string.routine_row_mark_complete_cd)
                    },
                    tint = checkColor,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (streakCount > 0) {
                        StreakBadge(streak = streakCount)
                    }
                }

                val repeatDesc = targetDaysSummary ?: stringResource(R.string.today_routine_daily_short)
                val streakText = stringResource(R.string.today_routine_streak_format, streakCount)
                val subtitle = if (preferredTime != null) {
                    "⏰ $preferredTime • $repeatDesc · $streakText"
                } else {
                    "$repeatDesc · $streakText"
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (targetType == "goal" && targetValue != null && targetValue > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
                    ) {
                        if (onProgressChange != null) {
                            IconButton(
                                onClick = {
                                    val next = maxOf(0f, currentValue - 1f)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onProgressChange(next)
                                },
                                enabled = currentValue > 0f,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Azalt",
                                    tint = if (currentValue > 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(AppTokens.Radius.sm),
                            color = if (isCompletedToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(24.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            ) {
                                Text(
                                    text = "${currentValue.toInt()} / $targetValue ${targetUnit ?: ""}".trim(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isCompletedToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (onProgressChange != null) {
                            IconButton(
                                onClick = {
                                    val next = currentValue + 1f
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onProgressChange(next)
                                },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Artır",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }

                if (weekHistory.isNotEmpty()) {
                    val history7 = weekHistory.takeLast(7)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        history7.forEachIndexed { index, done ->
                            val (date, initial) = days7.getOrElse(index) { today to "" }
                            val isTargetDay = targetDays.isEmpty() || date.dayOfWeek in targetDays
                            val isCurrentDay = index == history7.size - 1

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isCurrentDay) FontWeight.Bold else if (isTargetDay) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                                    color = if (isTargetDay) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    },
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (done) {
                                                Modifier.background(color)
                                            } else if (isCurrentDay) {
                                                Modifier
                                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                    .background(Color.Transparent)
                                            } else {
                                                Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                            }
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = { onMenuClick?.invoke() ?: (onClick?.invoke() ?: handleToggle()) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.routine_row_details_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    if (useSurface) {
        AppSurface(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = effectiveClick),
            radius = AppTokens.Radius.md,
            padding = AppTokens.Spacing.cardInner,
        ) {
            rowContent()
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = effectiveClick),
        ) {
            rowContent()
        }
    }
}
