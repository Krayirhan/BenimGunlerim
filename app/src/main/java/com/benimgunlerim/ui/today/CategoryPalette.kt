package com.benimgunlerim.ui.today

import androidx.compose.ui.graphics.Color
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import com.benimgunlerim.ui.theme.XpGold
import java.util.Locale
import kotlin.math.abs

/**
 * Stable category to accent colors (keyword match first, then deterministic fallback palette).
 */
object CategoryPalette {
    private val FALLBACK: List<Color> = listOf(
        CandyPrimary,
        CandySecondary,
        LevelSky,
        StreakCoral,
        XpGold,
    )

    /** Order matters: first keyword match wins (Turkish-focused defaults). */
    private val KEYWORD_COLORS: List<Pair<String, Color>> = listOf(
        "alışveriş" to CandySecondary,
        "iş" to LevelSky,
        "işler" to LevelSky,
        "toplantı" to LevelSky,
        "sunum" to LevelSky,
        "spor" to CandyPrimary,
        "sağlık" to CandyPrimary,
        "egzersiz" to CandyPrimary,
        "yürüyüş" to CandyPrimary,
        "beslenme" to CandyPrimary,
        "oku" to CandySecondary,
        "ders" to CandySecondary,
        "öğren" to CandySecondary,
        "kitap" to CandySecondary,
        "dil" to CandySecondary,
        "kişisel" to StreakCoral,
        "not" to StreakCoral,
        "günlük" to StreakCoral,
        "ev" to LevelSky,
        "temizlik" to LevelSky,
        "market" to CandySecondary,
        "fatura" to LevelSky,
        "para" to XpGold,
        "finans" to XpGold,
        "aile" to StreakCoral,
        "çocuk" to StreakCoral,
        "sosyal" to CandySecondary,
        "proje" to LevelSky,
        "bugün" to CandyPrimary,
    )

    fun colorFor(seed: String): Color {
        val v = seed.lowercase(Locale("tr", "TR")).trim().ifEmpty { return FALLBACK[0] }
        for ((kw, color) in KEYWORD_COLORS) {
            if (v.contains(kw)) return color
        }
        return FALLBACK[abs(v.hashCode()) % FALLBACK.size]
    }
}
