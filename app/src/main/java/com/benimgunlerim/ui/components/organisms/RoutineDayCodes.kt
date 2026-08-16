package com.benimgunlerim.ui.components.organisms

import java.time.DayOfWeek

/**
 * TextStyle.NARROW Türkçede çakışan tek harfler üretir (Pzt/Per/Paz → "P", Cum/Cmt → "C").
 * java.time'ın "2 harf" seçeneği olmadığı için elle tanımlanmış, benzersiz Türkçe gün kodları.
 */
val TurkishDayShortCode: Map<DayOfWeek, String> = mapOf(
    DayOfWeek.MONDAY to "Pt",
    DayOfWeek.TUESDAY to "Sa",
    DayOfWeek.WEDNESDAY to "Ça",
    DayOfWeek.THURSDAY to "Pe",
    DayOfWeek.FRIDAY to "Cu",
    DayOfWeek.SATURDAY to "Ct",
    DayOfWeek.SUNDAY to "Pa",
)
