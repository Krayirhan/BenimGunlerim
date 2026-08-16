package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.ui.components.core.AppBadge
import com.benimgunlerim.ui.components.core.AppBadgeVariant
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppIcon
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.gamification.GoldBadge
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun ShopItemCard(
    title: String,
    description: String,
    priceGold: Int,
    isPurchased: Boolean,
    onPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    userGold: Int = 0,
) {
    val canAfford = userGold >= priceGold

    AppSurface(
        modifier = modifier.fillMaxWidth(),
        radius = AppTokens.Radius.md,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                AppIcon(
                    icon = icon,
                    contentDescription = title,
                    size = AppTokens.IconSize.lg,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isPurchased) {
                AppBadge(
                    text = "Satın Alındı",
                    variant = AppBadgeVariant.Success,
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                ) {
                    GoldBadge(gold = priceGold)
                    AppButton(
                        text = "Al",
                        onClick = onPurchaseClick,
                        enabled = canAfford,
                        variant = if (canAfford) AppButtonVariant.Primary else AppButtonVariant.Secondary,
                    )
                }
            }
        }
    }
}
