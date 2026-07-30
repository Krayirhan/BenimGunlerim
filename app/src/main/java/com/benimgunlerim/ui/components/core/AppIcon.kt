package com.benimgunlerim.ui.components.core

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.benimgunlerim.ui.theme.AppTokens
import androidx.compose.foundation.layout.size

@Composable
fun AppIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = AppTokens.IconSize.md,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}
