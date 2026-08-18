package com.benimgunlerim.ui.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

/** Material adaptive "compact height" eşiği — bu değerin altında (tipik olarak telefon
 * landscape modu) floating action button gibi öğeler içerikle çakışabilir. */
private const val COMPACT_HEIGHT_THRESHOLD_DP = 480

/** Ekran yüksekliği compact mi (örn. telefon landscape modu)? Floating öğelerin
 * boyutunu/offsetini küçültmek için kullanılır — bkz. ScreenScaffold, TodayScreen,
 * PlanScreen, RoutinesScreen FAB'ları. */
@Composable
@ReadOnlyComposable
fun isCompactHeight(): Boolean = LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_THRESHOLD_DP
