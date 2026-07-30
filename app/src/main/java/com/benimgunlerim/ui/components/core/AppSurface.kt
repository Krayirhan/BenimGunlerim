package com.benimgunlerim.ui.components.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    radius: Dp = AppTokens.Radius.md,
    color: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderWidth: Dp = 1.dp,
    elevation: Dp = AppTokens.Elevation.flat,
    padding: Dp = AppTokens.Spacing.cardInner,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = if (onClick != null)
            modifier.fillMaxWidth().clickable(onClick = onClick)
        else
            modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding),
            content = content,
        )
    }
}
