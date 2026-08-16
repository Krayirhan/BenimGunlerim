package com.benimgunlerim.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens

data class BarChartEntry(
    val label: String,
    val value: Float,
    val color: Color,
    val isHighlighted: Boolean = false,
)

@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    maxValue: Float,
    modifier: Modifier = Modifier,
    barWidth: Dp = 16.dp,
    maxBarHeight: Dp = 100.dp,
) {
    val safeMax = if (maxValue <= 0f) 100f else maxValue

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        entries.forEach { entry ->
            val ratio = (entry.value / safeMax).coerceIn(0f, 1f)
            val currentHeight = maxBarHeight * ratio

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                // Value indicator
                Text(
                    text = if (entry.value > 0) "${entry.value.toInt()}" else "-",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (entry.isHighlighted) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (entry.isHighlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                )

                // Bar with Track
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(maxBarHeight)
                        .clip(RoundedCornerShape(AppTokens.Radius.sm))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (currentHeight > 0.dp) {
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(currentHeight)
                                .clip(RoundedCornerShape(AppTokens.Radius.sm))
                                .background(entry.color),
                        )
                    }
                }

                // Day Label
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (entry.isHighlighted) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (entry.isHighlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
