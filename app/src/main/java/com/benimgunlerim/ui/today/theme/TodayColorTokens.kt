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
    backgroundTop = Color(0xFFF8F9FA),
    backgroundBottom = Color(0xFFF8F9FA),
    headerSurface = Color(0xFF6BBF8E),
    headerBorder = Color(0xFF6BBF8E),
    tasksSectionSurface = Color(0xFFFFFFFF),
    tasksSectionBorder = Color(0xFFBEC9BF),
    routinesSectionSurface = Color(0xFFFFFFFF),
    routinesSectionBorder = Color(0xFFBEC9BF),
    itemSurface = Color(0xFFFFFFFF),
    itemBorder = Color(0xFFBEC9BF),
    chipSurface = Color(0xFFE8F5EE),
    chipBorder = Color(0xFFBEC9BF),
    chipText = Color(0xFF004C2D),
    overdueSurface = Color(0xFFFFF4E5),
    overdueBorder = Color(0xFFDBA36D),
    closeDaySurface = Color(0xFFFFFFFF),
    closeDayBorder = Color(0xFFBEC9BF),
    missedDaySurface = Color(0xFFFFF4E5),
    missedDayBorder = Color(0xFFDBA36D),
)

val LocalTodayColorTokens = staticCompositionLocalOf { LightTodayColorTokens }

val MaterialTheme.todayColorTokens: TodayColorTokens
    @Composable
    get() = LocalTodayColorTokens.current

@Composable
fun todayColorTokens(): TodayColorTokens = LightTodayColorTokens
