package com.benimgunlerim.ui.components.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.SemanticError
import com.benimgunlerim.ui.theme.SemanticInfo
import com.benimgunlerim.ui.theme.SemanticSuccess
import com.benimgunlerim.ui.theme.SemanticWarning

enum class AppBadgeVariant { Success, Warning, Error, Info, Neutral }

@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: AppBadgeVariant = AppBadgeVariant.Neutral,
) {
    val (bg, fg) = when (variant) {
        AppBadgeVariant.Success -> SemanticSuccess.copy(alpha = 0.12f) to SemanticSuccess
        AppBadgeVariant.Warning -> SemanticWarning.copy(alpha = 0.12f) to SemanticWarning
        AppBadgeVariant.Error   -> SemanticError.copy(alpha = 0.12f)   to SemanticError
        AppBadgeVariant.Info    -> SemanticInfo.copy(alpha = 0.12f)    to SemanticInfo
        AppBadgeVariant.Neutral -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.sm))
            .background(bg)
            .padding(horizontal = AppTokens.Spacing.xs, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}

// Renk doğrudan belirtilmek istendiğinde kullanılır (kategori rozeti gibi)
@Composable
fun AppColorBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.sm))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = AppTokens.Spacing.xs, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
