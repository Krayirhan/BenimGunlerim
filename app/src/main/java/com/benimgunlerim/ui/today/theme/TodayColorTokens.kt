package com.benimgunlerim.ui.today.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TodayColorTokens(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val headerSurface: Color,
    val headerBorder: Color,
    val tasksSectionSurface: Color,
    val tasksSectionBorder: Color,
    val routinesSectionSurface: Color,
    val routinesSectionBorder: Color,
    val itemSurface: Color,
    val itemBorder: Color,
    val chipSurface: Color,
    val chipBorder: Color,
    val chipText: Color,
    val overdueSurface: Color,
    val overdueBorder: Color,
    val closeDaySurface: Color,
    val closeDayBorder: Color,
    val missedDaySurface: Color,
    val missedDayBorder: Color,
)

private val LightTodayColorTokens = TodayColorTokens(
    backgroundTop          = Color(0xFFF8FAFC),
    backgroundBottom       = Color(0xFFF8FAFC),
    headerSurface          = Color(0xFFFFFFFF),
    headerBorder           = Color(0xFFE2E8F0),
    tasksSectionSurface    = Color(0xFFFFFFFF),
    tasksSectionBorder     = Color(0xFFE2E8F0),
    routinesSectionSurface = Color(0xFFFFFFFF),
    routinesSectionBorder  = Color(0xFFE2E8F0),
    itemSurface            = Color(0xFFFFFFFF),
    itemBorder             = Color(0xFFE2E8F0),
    chipSurface            = Color(0xFFE8F5EE),
    chipBorder             = Color(0xFFBFE8CF),
    chipText               = Color(0xFF004C2D),
    overdueSurface         = Color(0xFFFFF4E5),
    overdueBorder          = Color(0xFFF3D6B0),
    closeDaySurface        = Color(0xFF172536),
    closeDayBorder         = Color(0xFF26394D),
    missedDaySurface       = Color(0xFFFFF4E5),
    missedDayBorder        = Color(0xFFF3D6B0),
)

private val DarkTodayColorTokens = TodayColorTokens(
    backgroundTop          = Color(0xFF0F1512),
    backgroundBottom       = Color(0xFF0F1512),
    headerSurface          = Color(0xFF161E19),
    headerBorder           = Color(0xFF3E4A40),
    tasksSectionSurface    = Color(0xFF161E19),
    tasksSectionBorder     = Color(0xFF3E4A40),
    routinesSectionSurface = Color(0xFF161E19),
    routinesSectionBorder  = Color(0xFF3E4A40),
    itemSurface            = Color(0xFF1E2921),
    itemBorder             = Color(0xFF3E4A40),
    chipSurface            = Color(0xFF00311C),
    chipBorder             = Color(0xFF3E4A40),
    chipText               = Color(0xFF6BBF8E),
    overdueSurface         = Color(0xFF2D1E00),
    overdueBorder          = Color(0xFF8A5E20),
    closeDaySurface        = Color(0xFF172536),
    closeDayBorder         = Color(0xFF26394D),
    missedDaySurface       = Color(0xFF2D1E00),
    missedDayBorder        = Color(0xFF8A5E20),
)

val LocalTodayColorTokens = staticCompositionLocalOf { LightTodayColorTokens }

val MaterialTheme.todayColorTokens: TodayColorTokens
    @Composable get() = LocalTodayColorTokens.current

fun todayColorTokens(isDark: Boolean = false): TodayColorTokens =
    if (isDark) DarkTodayColorTokens else LightTodayColorTokens

