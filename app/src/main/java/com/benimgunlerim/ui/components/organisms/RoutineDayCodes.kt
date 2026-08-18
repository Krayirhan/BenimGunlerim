package com.benimgunlerim.ui.components.organisms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import java.time.DayOfWeek

/**
 * TextStyle.NARROW Türkçede çakışan tek harfler üretir (Pzt/Per/Paz → "P", Cum/Cmt → "C").
 * java.time'ın "2 harf" seçeneği olmadığı için elle tanımlanmış, benzersiz Türkçe gün kodları.
 */
@Composable
@ReadOnlyComposable
fun turkishDayShortCode(): Map<DayOfWeek, String> = mapOf(
    DayOfWeek.MONDAY to stringResource(R.string.routine_day_short_mon),
    DayOfWeek.TUESDAY to stringResource(R.string.routine_day_short_tue),
    DayOfWeek.WEDNESDAY to stringResource(R.string.routine_day_short_wed),
    DayOfWeek.THURSDAY to stringResource(R.string.routine_day_short_thu),
    DayOfWeek.FRIDAY to stringResource(R.string.routine_day_short_fri),
    DayOfWeek.SATURDAY to stringResource(R.string.routine_day_short_sat),
    DayOfWeek.SUNDAY to stringResource(R.string.routine_day_short_sun),
)
