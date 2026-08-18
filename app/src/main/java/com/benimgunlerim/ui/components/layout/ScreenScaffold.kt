package com.benimgunlerim.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    snackbarHost: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current
    var fabHeightPx by remember { mutableIntStateOf(0) }
    val compactHeight = isCompactHeight()
    // Compact yükseklikte (telefon landscape) FAB'ı içeriğe daha yakın tutuyoruz —
    // dar dikey alanda üst içerikle çakışma riskini azaltır.
    val fabBottomOffset = if (compactHeight) AppTokens.Spacing.xs else AppTokens.Layout.fabBottomOffset

    // FAB'ın ölçülen gerçek yüksekliğine dayanır (ilk frame için varsayılan boyut) —
    // böylece font ölçeği veya ekran yönünden bağımsız olarak içerik hep FAB'ın üstünde kalır.
    val fabClearance: Dp = if (floatingActionButton != null) {
        val effectiveFab = if (fabHeightPx > 0) {
            with(density) { fabHeightPx.toDp() }
        } else {
            AppTokens.Layout.fabDefaultSize
        }
        effectiveFab + fabBottomOffset + AppTokens.Spacing.lg
    } else {
        AppTokens.Spacing.xl
    }

    // İçerik tam yüksekliği kullanır; alt güvenli alan kaydırılabilir içeriğin
    // sonuna verilir. Böylece son kart FAB'ın altında kesilmez.
    val contentPadding = PaddingValues(
        start = AppTokens.Spacing.screenHorizontal,
        top = AppTokens.Spacing.md,
        end = AppTokens.Spacing.screenHorizontal,
        bottom = fabClearance,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        content(contentPadding)

        if (snackbarHost != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = AppTokens.Spacing.screenHorizontal,
                        end = AppTokens.Spacing.screenHorizontal,
                        bottom = AppTokens.Layout.snackbarBottomOffset,
                    ),
            ) {
                snackbarHost()
            }
        }

        if (floatingActionButton != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = AppTokens.Spacing.screenHorizontal,
                        bottom = fabBottomOffset,
                    )
                    .onSizeChanged { fabHeightPx = it.height },
            ) {
                floatingActionButton()
            }
        }
    }
}
