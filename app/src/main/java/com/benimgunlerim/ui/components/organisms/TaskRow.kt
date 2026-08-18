package com.benimgunlerim.ui.components.organisms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppBadge
import com.benimgunlerim.ui.components.core.AppBadgeVariant
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.CompletedGreen

@Composable
fun TaskRow(
    title: String,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dueTime: String? = null,
    priority: Int = 2,
    category: String? = null,
    tags: List<String> = emptyList(),
    useSurface: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    // Mikro Animasyonlar: 160ms check color + 160ms check scale + %50 text alpha
    val checkColor by animateColorAsState(
        targetValue = if (isCompleted) CompletedGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "task_check_color",
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "task_check_scale",
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 160),
        label = "task_text_alpha",
    )

    val handleToggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggleComplete()
    }

    val effectiveClick = onClick ?: handleToggle

    val rowContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
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
                    imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = stringResource(
                        if (isCompleted) R.string.today_task_uncomplete_cd else R.string.today_task_toggle_cd,
                    ),
                    tint = checkColor,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkScale),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (dueTime != null || category != null || tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (dueTime != null) {
                            Text(
                                text = dueTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                            )
                        }

                        if (category != null) {
                            AppBadge(
                                text = category,
                                variant = AppBadgeVariant.Neutral,
                            )
                        }

                        tags.take(2).forEach { tag ->
                            AppBadge(
                                text = tag,
                                variant = AppBadgeVariant.Neutral,
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.today_task_delete_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    if (useSurface) {
        AppSurface(
            modifier = modifier.clickable(onClick = effectiveClick),
            radius = AppTokens.Radius.md,
            padding = AppTokens.Spacing.none,
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
