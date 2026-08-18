package com.benimgunlerim.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private const val TILE_BG_ALPHA = 0.10f
private const val TILE_BORDER_ALPHA = 0.16f
private val TileHeight = 72.dp
private val TileCornerRadius = 16.dp
private val ButtonCornerRadius = 8.dp
private val ButtonHeight = 42.dp

@Composable
internal fun SheetSectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun SummaryTile(value: String, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier.height(TileHeight)
            .clip(RoundedCornerShape(TileCornerRadius))
            .background(color.copy(alpha = TILE_BG_ALPHA))
            .border(1.dp, color.copy(alpha = TILE_BORDER_ALPHA), RoundedCornerShape(TileCornerRadius))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun OutlinedSmallButton(text: String, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(ButtonHeight)
            .clip(RoundedCornerShape(ButtonCornerRadius))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(ButtonCornerRadius)),
        shape = RoundedCornerShape(ButtonCornerRadius),
    ) {
        Text(text, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
    }
}
