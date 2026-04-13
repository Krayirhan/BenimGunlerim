@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CandyTertiary
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import com.benimgunlerim.ui.theme.XpGold
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// ── Confetti Particle System ──────────────────────────────────────────────────

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var size: Float,
    var color: Color,
    var alpha: Float = 1f,
)

@Composable
fun ConfettiOverlay(
    trigger: Boolean,
    intensity: ConfettiIntensity = ConfettiIntensity.Medium,
    onFinished: () -> Unit = {},
) {
    if (!trigger) return

    val colors = listOf(CandyPrimary, CandySecondary, CandyTertiary, XpGold, StreakCoral, LevelSky, Color(0xFFFF6B8A))
    val particleCount = when (intensity) {
        ConfettiIntensity.Mini -> 12
        ConfettiIntensity.Medium -> 35
        ConfettiIntensity.Full -> 60
        ConfettiIntensity.LevelUp -> 80
    }

    val particles = remember(trigger) {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f - 0.1f,
                vx = (Random.nextFloat() - 0.5f) * 0.015f,
                vy = Random.nextFloat() * 0.008f + 0.003f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 8f,
                size = Random.nextFloat() * 10f + 4f,
                color = colors.random(),
            )
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = when (intensity) {
                    ConfettiIntensity.Mini -> 800
                    ConfettiIntensity.Medium -> 1200
                    ConfettiIntensity.Full -> 1800
                    ConfettiIntensity.LevelUp -> 2500
                },
                easing = LinearEasing,
            ),
        )
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value
        particles.forEach { p ->
            val px = (p.x + p.vx * t * 60f) * w
            val py = (p.y + p.vy * t * 60f + 0.5f * 0.001f * (t * 60f) * (t * 60f)) * h
            val alpha = (1f - t).coerceIn(0f, 1f)
            if (py < h * 1.2f && alpha > 0.05f) {
                rotate(p.rotation + p.rotationSpeed * t * 60f, Offset(px, py)) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(px - p.size / 2, py - p.size / 2),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f),
                    )
                }
            }
        }
    }
}

enum class ConfettiIntensity { Mini, Medium, Full, LevelUp }

// ── Floating XP/Gold Text ─────────────────────────────────────────────────────

@Composable
fun FloatingRewardText(
    text: String,
    trigger: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) {
    if (!trigger) return

    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(trigger) {
        // Reset
        offsetY.snapTo(0f)
        alpha.snapTo(1f)
        scale.snapTo(0.5f)
        // Pop in
        scale.animateTo(1.2f, tween(150, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(100))
        // Float up + fade (parallel)
        coroutineScope {
            launch { offsetY.animateTo(-120f, tween(600, easing = FastOutSlowInEasing)) }
            launch { alpha.animateTo(0f, tween(600, delayMillis = 200)) }
        }
        onFinished()
    }

    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
        color = XpGold,
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.toInt()) }
            .scale(scale.value)
            .alpha(alpha.value),
    )
}

// ── Star Burst (checkbox animation) ───────────────────────────────────────────

@Composable
fun StarBurst(trigger: Boolean, modifier: Modifier = Modifier) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier.size(40.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val t = progress.value
        val starColors = listOf(XpGold, CandyPrimary, StreakCoral, CandyTertiary)

        repeat(8) { i ->
            val angle = Math.toRadians((i * 45.0) + t * 90)
            val dist = t * size.width * 0.6f
            val alpha = (1f - t).coerceIn(0f, 1f)
            val px = cx + cos(angle).toFloat() * dist
            val py = cy + sin(angle).toFloat() * dist
            drawCircle(
                color = starColors[i % starColors.size].copy(alpha = alpha),
                radius = (1f - t) * 4f + 1f,
                center = Offset(px, py),
            )
        }
    }
}

// ── Level Up Overlay ──────────────────────────────────────────────────────────

@Composable
fun LevelUpOverlay(
    visible: Boolean,
    level: Int,
    title: String,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(400), initialScale = 0.8f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Confetti behind
            ConfettiOverlay(trigger = visible, intensity = ConfettiIntensity.LevelUp)

            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                CandyPrimary.copy(alpha = 0.95f),
                                LevelSky.copy(alpha = 0.95f),
                            ),
                        ),
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                )
                Text(
                    text = "SEVİYE ATLADIN!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Seviye $level",
                    style = MaterialTheme.typography.displaySmall,
                    color = XpGold,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Text(
                    text = "Harika gidiyorsun! Devam et! ✨",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── XP Progress Bar (gradient, animated) ──────────────────────────────────────

@Composable
fun XpProgressBar(
    currentXp: Int,
    xpForNextLevel: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (xpForNextLevel > 0) currentXp.toFloat() / xpForNextLevel else 0f
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            progress.coerceIn(0f, 1f),
            tween(600, easing = FastOutSlowInEasing),
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(12.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Brush.linearGradient(listOf(XpGold, Color(0xFFFF8A65)))),
            )
        }
        Text(
            text = "$currentXp / $xpForNextLevel XP",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Happiness Bar ─────────────────────────────────────────────────────────────

@Composable
fun HappinessBar(
    happiness: Int,
    modifier: Modifier = Modifier,
) {
    val progress = happiness.toFloat() / 100f
    val animatedProgress = remember { Animatable(0f) }
    val barColor = when {
        happiness >= 80 -> listOf(Color(0xFF00C897), Color(0xFF4ECDC4))
        happiness >= 50 -> listOf(XpGold, Color(0xFFFF8A65))
        else -> listOf(StreakCoral, Color(0xFFFF8A65))
    }

    val pulse = rememberInfiniteTransition(label = "happyPulse")
    val heartScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (happiness >= 80) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "heartScale",
    )

    LaunchedEffect(progress) {
        animatedProgress.animateTo(progress.coerceIn(0f, 1f), tween(600, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(10.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Brush.linearGradient(barColor)),
            )
        }
        Text(
            text = "❤\uFE0F $happiness/100",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.scale(heartScale),
        )
    }
}

// ── Bouncy Press Modifier ─────────────────────────────────────────────────────

@Composable
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bounceScale",
    )
    return this
        .scale(scale)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) {
            pressed = true
            onClick()
        }
}

// ── Mood Selector (5 emoji) ───────────────────────────────────────────────────

data class MoodOption(val emoji: String, val label: String, val color: Color)

val moodOptions = listOf(
    MoodOption("😢", "Çok kötü", StreakCoral),
    MoodOption("😕", "Kötü", Color(0xFFFF8A65)),
    MoodOption("😐", "Normal", XpGold),
    MoodOption("🙂", "İyi", CandyTertiary),
    MoodOption("😄", "Harika", CandyPrimary),
)

@Composable
fun MoodSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        moodOptions.forEachIndexed { index, mood ->
            val isSelected = index == selectedIndex
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.3f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "moodScale",
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) Modifier.background(mood.color.copy(alpha = 0.12f)) else Modifier,
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(
                    text = mood.emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.scale(scale),
                )
                if (isSelected) {
                    Text(
                        text = mood.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = mood.color,
                    )
                }
            }
        }
    }
}

// ── Achievement Unlock Overlay ────────────────────────────────────────────────

@Composable
fun AchievementUnlockOverlay(
    visible: Boolean,
    emoji: String,
    title: String,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(400), initialScale = 0.8f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(XpGold.copy(alpha = 0.95f), CandySecondary.copy(alpha = 0.95f)),
                        ),
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                )
                Text(
                    text = "BAŞARIM AÇILDI!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
