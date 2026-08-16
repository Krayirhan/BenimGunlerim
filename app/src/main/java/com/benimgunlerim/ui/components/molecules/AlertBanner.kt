package com.benimgunlerim.ui.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.ui.theme.AppTokens

import com.benimgunlerim.ui.theme.ErrorBorder
import com.benimgunlerim.ui.theme.ErrorSoft
import com.benimgunlerim.ui.theme.Info
import com.benimgunlerim.ui.theme.InfoBorder
import com.benimgunlerim.ui.theme.InfoSoft
import com.benimgunlerim.ui.theme.SemanticError
import com.benimgunlerim.ui.theme.Warning
import com.benimgunlerim.ui.theme.WarningBorder
import com.benimgunlerim.ui.theme.WarningSoft

enum class AlertBannerSeverity { Info, Warning, Error }

@Composable
fun AlertBanner(
    message: String,
    modifier: Modifier = Modifier,
    severity: AlertBannerSeverity = AlertBannerSeverity.Info,
    title: String? = null,
    actionLabel: String? = null,
    action: (() -> Unit)? = null,
) {
    val containerColor: Color
    val contentColor: Color
    val borderColor: Color

    when (severity) {
        AlertBannerSeverity.Info -> {
            containerColor = InfoSoft
            contentColor = Info
            borderColor = InfoBorder
        }
        AlertBannerSeverity.Warning -> {
            containerColor = WarningSoft
            contentColor = Warning
            borderColor = WarningBorder
        }
        AlertBannerSeverity.Error -> {
            containerColor = ErrorSoft
            contentColor = SemanticError
            borderColor = ErrorBorder
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.md))
            .background(containerColor)
            .border(AppTokens.BorderWidth.thin, borderColor, RoundedCornerShape(AppTokens.Radius.md))
            .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = contentColor,
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.85f),
                )
            }

            if (actionLabel != null && action != null) {
                OutlinedButton(
                    onClick = action,
                    modifier = Modifier.heightIn(min = AppTokens.TouchTarget.compact),
                    shape = RoundedCornerShape(AppTokens.Radius.pill),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = contentColor,
                    ),
                    border = BorderStroke(AppTokens.BorderWidth.thin, contentColor.copy(alpha = 0.40f)),
                    contentPadding = PaddingValues(horizontal = AppTokens.Spacing.sm),
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = contentColor,
                    )
                }
            }
        }
    }
}
