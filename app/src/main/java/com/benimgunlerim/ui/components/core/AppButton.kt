package com.benimgunlerim.ui.components.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.SemanticError

enum class AppButtonVariant { Primary, Secondary, Ghost, Danger }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(AppTokens.Radius.md)

    @Composable
    fun iconContent() {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(AppTokens.IconSize.sm),
            )
            Spacer(Modifier.width(AppTokens.Spacing.xs))
        }
    }

    when (variant) {
        AppButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = CandyPrimary,
                contentColor = Color.White,
                disabledContainerColor = CandyPrimary.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f),
            ),
        ) {
            iconContent()
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }

        AppButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CandyPrimary,
                disabledContentColor = CandyPrimary.copy(alpha = 0.4f),
            ),
            border = BorderStroke(
                1.dp,
                if (enabled) CandyPrimary else CandyPrimary.copy(alpha = 0.3f),
            ),
        ) {
            iconContent()
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }

        AppButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
        ) {
            iconContent()
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }

        AppButtonVariant.Danger -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SemanticError,
                contentColor = Color.White,
                disabledContainerColor = SemanticError.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f),
            ),
        ) {
            iconContent()
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
