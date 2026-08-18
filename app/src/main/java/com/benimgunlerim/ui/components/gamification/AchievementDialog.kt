package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.XpGold
import com.benimgunlerim.ui.theme.XpGoldSoft

/**
 * 4. Kademe Büyük Kutlama: Başarım Kazanıldı Modalı
 */
@Suppress("LongMethod")
@Composable
fun AchievementDialog(
    id: String = "",
    emoji: String,
    title: String,
    description: String,
    xpReward: Int = 50,
    showParticles: Boolean = false,
    onDismiss: () -> Unit,
) {
    val category = remember(id) { getAchievementCategory(id) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(AppTokens.Radius.xxl),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = AppTokens.Elevation.modal,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTokens.Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
                ) {
                    // Kategorize Vektörel Rozet
                    AchievementBadgeView(
                        emoji = emoji,
                        category = category,
                        size = AppTokens.Spacing.xxl + AppTokens.Spacing.xxl + AppTokens.Spacing.sm,
                        fontSize = 36,
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_achievement_unlocked_title),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimary,
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    // XP Ödülü
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppTokens.Radius.md))
                            .background(XpGoldSoft)
                            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs),
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_xp_reward_badge, xpReward),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = XpGold,
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppTokens.TouchTarget.min),
                        shape = RoundedCornerShape(AppTokens.Radius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_achievement_action),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }

            if (showParticles) {
                SubtleCelebrationParticles(modifier = Modifier.matchParentSize())
            }
        }
    }
}
