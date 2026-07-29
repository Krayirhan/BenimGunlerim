package com.benimgunlerim.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benimgunlerim.ui.theme.AccentAmber
import com.benimgunlerim.ui.theme.AccentAmberSoft
import com.benimgunlerim.ui.theme.AccentCoral
import com.benimgunlerim.ui.theme.AccentCoralSoft
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandyPrimaryDark
import com.benimgunlerim.ui.theme.CandyTertiary
import com.benimgunlerim.ui.theme.GpPrimary
import com.benimgunlerim.ui.theme.GpPrimaryDark
import com.benimgunlerim.ui.theme.GpPrimaryLight
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import com.benimgunlerim.ui.theme.XpGold

// ── SurfaceCard (Shared hero/card shell) ──────────────────────────────────────

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp = 24.dp,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    elevation: androidx.compose.ui.unit.Dp = 2.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        colors = androidx.compose.material3.CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = elevation),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth().padding(padding),
            content = content,
        )
    }
}

// ── HeroCardShell (Page-level hero banner) ────────────────────────────────────

@Composable
fun HeroCardShell(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(Color(0xFFEAF8F2), Color(0xFFF0EEFF)),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(gradientColors))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.10f), RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

// ── Section Title (GunPlan: Blok Başlığı) ────────────────────────────────────

@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null,
    accentColor: Color? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (accentColor != null) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor),
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Info Card ────────────────────────────────────────────────────────────────

@Composable
fun InfoCard(title: String, body: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(text = "!", style = MaterialTheme.typography.titleLarge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                )
            }
        }
    }
}

// ── Metric Card (gradient banner) ────────────────────────────────────────────

@Composable
fun MetricCard(
    title: String,
    value: String,
    helper: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(CandyPrimary, CandyTertiary),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradientBrush(gradientColors)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
            Text(
                text = helper,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}

// ── Animated Progress Bar (Candy: 10dp, 500ms) ──────────────────────────────

@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = CandyPrimary,
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animated.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        )
    }
    LinearProgressIndicator(
        progress = { animated.value },
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(99.dp)),
        color = color,
        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        strokeCap = StrokeCap.Round,
    )
}

// ── XP Badge (Altın parıltı) ─────────────────────────────────────────────────

@Composable
fun XpBadge(xp: Int, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "xpPulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "xpScale",
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(Brush.linearGradient(listOf(XpGold, Color(0xFFFF8A65))))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$xp XP",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

// ── Streak Badge (Ateş gradient) ─────────────────────────────────────────────

@Composable
fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    val bgBrush = when {
        streak >= 30  -> Brush.linearGradient(listOf(LevelSky, CandyPrimary))
        streak >= 7   -> Brush.linearGradient(listOf(StreakCoral, Color(0xFFFF8A65)))
        else          -> Brush.linearGradient(listOf(StreakCoral.copy(alpha = 0.8f), StreakCoral))
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgBrush)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$streak günlük seri",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

// ── Inline Empty State ────────────────────────────────────────────────────────

@Composable
fun InlineEmptyState(
    text: String,
    modifier: Modifier = Modifier,
    illustration: String? = null,
    title: String? = null,
    ctaLabel: String? = null,
    onCtaClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (illustration != null) {
            Text(
                text = illustration,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
            )
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (ctaLabel != null && onCtaClick != null) {
            Button(
                onClick = onCtaClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
            ) {
                Text(ctaLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Category Badge ────────────────────────────────────────────────────────────

@Composable
fun CategoryBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

// ── Status Badge ──────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

// ── Level Dot Row ─────────────────────────────────────────────────────────────

@Composable
fun LevelDots(filled: Int, total: Int, dotSize: Dp = 8.dp, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(
                        if (i < filled) CandyPrimary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
            )
        }
    }
}

// ── Gradient Brush helper ─────────────────────────────────────────────────────

fun Brush.Companion.linearGradientBrush(colors: List<Color>): Brush =
    linearGradient(colors)

// ── Spacer helpers ────────────────────────────────────────────────────────────

@Composable fun VerticalSpacer(dp: Int) = Spacer(modifier = Modifier.height(dp.dp))
@Composable fun HorizontalSpacer(dp: Int) = Spacer(modifier = Modifier.width(dp.dp))

// ── Screen Header (Candy: gradient accent) ───────────────────────────────────

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    extra: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (extra != null) {
            Spacer(Modifier.height(8.dp))
            extra()
        }
    }
}

// ── Progress Summary Card (Candy Dream) ───────────────────────────────────────

@Composable
fun ProgressSummaryCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val percent = (progress * 100).toInt()
    val message = when {
        percent == 0   -> "Macera başlıyor!"
        percent <= 25  -> "Güzel başlangıç!"
        percent <= 50  -> "Yarıyı aştın!"
        percent <= 75  -> "Harika gidiyor!"
        percent < 100  -> "Neredeyse tamam!"
        else           -> "Mükemmel gün!"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "$completed / $total tamamlandı",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CandyPrimaryDark,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = CandyPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CandyPrimary.copy(alpha = 0.1f))
                        .border(3.dp, CandyPrimary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "%$percent",
                        style = MaterialTheme.typography.labelSmall,
                        color = CandyPrimaryDark,
                    )
                }
            }
            AnimatedProgressBar(progress = progress, color = CandyPrimary)
        }
    }
}

// ── Gold Badge (Coin göstergesi) ──────────────────────────────────────────────

@Composable
fun GoldBadge(gold: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(XpGold.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(XpGold),
        )
        Text(
            text = "$gold",
            style = MaterialTheme.typography.labelLarge,
            color = XpGold,
        )
    }
}

// ── Level Badge ───────────────────────────────────────────────────────────────

@Composable
fun LevelBadge(level: Int, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.linearGradient(listOf(LevelSky, CandyPrimary)))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
        Text(
            text = "Lv.$level $title",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

// ── Companion Speech Bubble ───────────────────────────────────────────────────

@Composable
fun CompanionBubble(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Stat Pill (Compact stat indicator) ────────────────────────────────────────

@Composable
fun StatPill(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  Design System v1 — Unified Components
//  Tüm yeni ekranlar bu bölümdeki component'leri kullanmalı.
// ════════════════════════════════════════════════════════════════════════════

// ── AppButton — Unified CTA (Primary / Secondary / Ghost) ────────────────────

enum class AppButtonVariant { Primary, Secondary, Ghost }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(AppTokens.Radius.md)
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
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (enabled) CandyPrimary else CandyPrimary.copy(alpha = 0.3f),
            ),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
        AppButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}

// ── SectionHeader — Unified section başlığı ──────────────────────────────────
//  accentColor verilirse sol tarafta renkli dikey çizgi gösterir.
//  trailingContent için aksiyon link/button geçilebilir.

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTokens.Spacing.screenHorizontal, vertical = AppTokens.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (accentColor != null) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(AppTokens.Radius.xs))
                    .background(accentColor),
            )
            Spacer(Modifier.width(AppTokens.Spacing.xs))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

// ── AppCard — Unified kart kabuğu (SurfaceCard'ın token-aware versiyonu) ───────

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Card(
        modifier = if (onClick != null) modifier.fillMaxWidth().clickable(onClick = onClick)
                   else modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppTokens.Radius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppTokens.Spacing.cardInner),
            content = content,
        )
    }
}

// ── EmptyStateView — Full-page boş durum gösterimi ───────────────────────────

@Composable
fun EmptyStateView(
    illustration: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    ctaLabel: String? = null,
    onCtaClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
    ) {
        Text(
            text = illustration,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (ctaLabel != null && onCtaClick != null) {
            Spacer(Modifier.height(AppTokens.Spacing.xs))
            AppButton(
                text = ctaLabel,
                onClick = onCtaClick,
                modifier = Modifier.wrapContentWidth(),
            )
        }
    }
}

// ── AppDivider — Tutarlı ayırıcı çizgi ───────────────────────────────────────

@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = AppTokens.Spacing.screenHorizontal),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    )
}

// ── WarningCard — Sarı uyarı kartı ("Gün sonu" gibi) ────────────────────────

@Composable
fun WarningCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(AccentAmberSoft)
            .border(1.dp, AccentAmber.copy(alpha = 0.25f), RoundedCornerShape(AppTokens.Radius.lg))
            .padding(AppTokens.Spacing.cardInner),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            if (icon != null) {
                Text(text = icon, style = MaterialTheme.typography.titleLarge)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentAmber,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }
            trailingContent?.invoke()
        }
    }
}

// ── AlertCard — Kırmızı/coral aksiyon kartı ("Dünü tamamla" gibi) ────────────

@Composable
fun AlertCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    primaryCtaLabel: String? = null,
    onPrimaryCtaClick: (() -> Unit)? = null,
    secondaryCtaLabel: String? = null,
    onSecondaryCtaClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(AccentCoralSoft)
            .border(1.dp, AccentCoral.copy(alpha = 0.25f), RoundedCornerShape(AppTokens.Radius.lg))
            .padding(AppTokens.Spacing.cardInner),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = AccentCoral,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
            if (primaryCtaLabel != null || secondaryCtaLabel != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (primaryCtaLabel != null && onPrimaryCtaClick != null) {
                        Button(
                            onClick = onPrimaryCtaClick,
                            shape = RoundedCornerShape(AppTokens.Radius.sm),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentCoral,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(text = primaryCtaLabel, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (secondaryCtaLabel != null && onSecondaryCtaClick != null) {
                        TextButton(onClick = onSecondaryCtaClick) {
                            Text(
                                text = secondaryCtaLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  Design System v2 — Screen-level unified shell
//  Her sekme ScreenBackground + ScreenHeroCard + SectionBlock kullanır.
// ════════════════════════════════════════════════════════════════════════════

// ── ScreenBackground — Tüm sekmeler için ortak gradient ──────────────────────

@Composable
fun ScreenBackground(): androidx.compose.ui.graphics.Brush =
    androidx.compose.ui.graphics.Brush.verticalGradient(
        listOf(
            com.benimgunlerim.ui.theme.CandyPrimaryLight,
            com.benimgunlerim.ui.theme.AccentPurpleSoft,
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )

// ── ScreenHeroCard — Her sekmede aynı zarif hero başlık ──────────────────────

@Composable
fun ScreenHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    metricRow: (@Composable () -> Unit)? = null,
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppTokens.Radius.xxl),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTokens.Spacing.cardInnerHero),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (metricRow != null) {
                Spacer(Modifier.height(AppTokens.Spacing.xxs))
                metricRow()
            }
        }
    }
}

// ── SectionBlock — Düz ekran başlığı + isteğe bağlı yumuşak kart sarmalayıcı ──

@Composable
fun SectionBlock(
    title: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    subtitle: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    wrapInCard: Boolean = false,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTokens.Spacing.xxs, vertical = AppTokens.Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (accentColor != null) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(AppTokens.Radius.xs))
                        .background(accentColor),
                )
                Spacer(Modifier.width(AppTokens.Spacing.xs))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailingContent?.invoke()
        }
        if (wrapInCard) {
            AppCard(content = content)
        } else {
            content()
        }
    }
}
