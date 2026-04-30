package com.benimgunlerim.ui.today.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
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
    backgroundTop = Color(0xFFF7F6FF),
    backgroundBottom = Color(0xFFFFFFFF),
    headerSurface = Color(0xFF66AE90),
    headerBorder = Color(0xFF5C9F84),
    tasksSectionSurface = Color(0xFFF2F5FF),
    tasksSectionBorder = Color(0xFFD0DAF2),
    routinesSectionSurface = Color(0xFFF2FAF4),
    routinesSectionBorder = Color(0xFFCEE4D4),
    itemSurface = Color(0xFFFFFFFF),
    itemBorder = Color(0xFFD8DEE8),
    chipSurface = Color(0xFFF2F4F8),
    chipBorder = Color(0xFFCCD6E3),
    chipText = Color(0xFF344154),
    overdueSurface = Color(0xFFFFF4F2),
    overdueBorder = Color(0xFFF2C3BD),
    closeDaySurface = Color(0xFFFFFFFF),
    closeDayBorder = Color(0xFFD4DCE8),
    missedDaySurface = Color(0xFFFFF1EF),
    missedDayBorder = Color(0xFFF0B9B3),
)

val LocalTodayColorTokens = staticCompositionLocalOf { LightTodayColorTokens }

val MaterialTheme.todayColorTokens: TodayColorTokens
    @Composable
    get() = LocalTodayColorTokens.current

@Composable
fun todayColorTokens(): TodayColorTokens = LightTodayColorTokens

