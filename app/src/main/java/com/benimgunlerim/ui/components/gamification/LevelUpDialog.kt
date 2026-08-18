package com.benimgunlerim.ui.components.gamification

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.molecules.ProgressBar
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import com.benimgunlerim.ui.theme.LevelUpGradientEnd
import com.benimgunlerim.ui.theme.LevelUpGradientStart
import com.benimgunlerim.ui.theme.XpGoldBorder

/**
 * 4. Kademe Büyük Kutlama: Seviye Atlama Modalı
 */
@Suppress("LongMethod")
@Composable
fun LevelUpDialog(
    level: Int,
    title: String,
    xpBonus: Int = 100,
    showParticles: Boolean = true,
    onDismiss: () -> Unit,
) {
    var animatedTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animatedTarget,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "LevelUpXpBar",
    )

    LaunchedEffect(Unit) {
        animatedTarget = 1.0f
    }

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
                    // Büyük Seviye Rozeti + Soft Halo
                    Box(
                        modifier = Modifier
                            .size(AppTokens.Spacing.xxl * 3)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(LevelUpGradientStart, LevelUpGradientEnd),
                                ),
                            )
                            .border(AppTokens.BorderWidth.thin, XpGoldBorder, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👑", fontSize = 32.sp)
                            Text(
                                text = "Lv.$level",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_level_up_title),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.celebration_level_up_subtitle, level, title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BrandPrimary,
                        )
                        Text(
                            text = stringResource(R.string.celebration_level_up_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Animasyonlu XP Bar ve XP Rozeti
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProgressBar(
                            progress = animatedProgress,
                            color = BrandPrimary,
                            trackColor = BrandPrimarySoft,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppTokens.Radius.md))
                                .background(BrandPrimarySoft)
                                .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xxs),
                        ) {
                            Text(
                                text = stringResource(R.string.celebration_xp_bonus_badge, xpBonus),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary,
                            )
                        }
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

            if (showParticles) {
                SubtleCelebrationParticles(modifier = Modifier.matchParentSize())
            }
        }
    }
}
