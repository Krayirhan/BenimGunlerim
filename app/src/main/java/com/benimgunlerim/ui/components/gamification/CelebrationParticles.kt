package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * Sakin, kontrollü ve 1 saniyelik hafif konfeti parçacık efekti.
 * Sadece Seviye Atlama, Büyük Başarımlar ve %100 Gün Kapanışında tetiklenir.
 */
@Composable
fun SubtleCelebrationParticles(
    modifier: Modifier = Modifier,
) {
    val party = remember {
        Party(
            speed = 8f,
            maxSpeed = 18f,
            damping = 0.92f,
            spread = 55,
            colors = listOf(
                0xFF147A4F.toInt(), // Brand Forest Green
                0xFFD97706.toInt(), // Warm Gold
                0xFF3B82F6.toInt(), // Soft Sky Blue
                0xFF10B981.toInt(), // Mint Green
            ),
            emitter = Emitter(duration = 1000L, TimeUnit.MILLISECONDS).max(35),
            position = Position.Relative(0.5, 0.0),
        )
    }

    KonfettiView(
        modifier = modifier.fillMaxSize(),
        parties = listOf(party),
    )
}
