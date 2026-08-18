package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import com.benimgunlerim.ui.theme.XpGoldSoft

/**
 * Birleşik Büyük Kutlama: Level Atlandı + Başarım Açıldı
 */
@Suppress("LongMethod")
@Composable
fun CombinedCelebrationDialog(
    level: Int,
    levelTitle: String,
    achievementTitle: String,
    achievementEmoji: String,
    totalXpBonus: Int,
    onDismiss: () -> Unit,
) {
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(AppTokens.Spacing.xxl + AppTokens.Spacing.xl)
                                .clip(CircleShape)
                                .background(BrandPrimary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "👑", fontSize = 24.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(AppTokens.Spacing.xxl + AppTokens.Spacing.xl)
                                .clip(CircleShape)
                                .background(XpGoldSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = achievementEmoji, fontSize = 24.sp)
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_combined_title),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.celebration_combined_level_body, level, levelTitle),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BrandPrimary,
                        )
                        Text(
                            text = stringResource(R.string.celebration_combined_achievement_body, achievementTitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppTokens.Radius.md))
                            .background(BrandPrimarySoft)
                            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs),
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_combined_xp_badge, totalXpBonus),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimary,
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
                            text = stringResource(R.string.celebration_action_continue),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
            SubtleCelebrationParticles(modifier = Modifier.matchParentSize())
        }
    }
}
