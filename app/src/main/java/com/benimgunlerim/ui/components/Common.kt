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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
