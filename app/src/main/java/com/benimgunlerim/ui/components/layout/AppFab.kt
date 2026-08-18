package com.benimgunlerim.ui.components.layout

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Compact yükseklikte (telefon landscape) `SmallFloatingActionButton`'a, normal yükseklikte
 * standart `FloatingActionButton`'a düşen tek FAB bileşeni. Dar dikey alanda FAB'ın üst
 * içerikle çakışma riskini azaltır — bkz. [ScreenScaffold]'daki compact-height offset düşüşü.
 */
@Composable
fun AppFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    if (isCompactHeight()) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            modifier = modifier,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            modifier = modifier,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}
