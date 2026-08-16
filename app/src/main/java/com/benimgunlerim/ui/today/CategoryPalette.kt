package com.benimgunlerim.ui.today

import androidx.compose.ui.graphics.Color
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.Info
import com.benimgunlerim.ui.theme.Streak
import com.benimgunlerim.ui.theme.Success
import com.benimgunlerim.ui.theme.XpGold
import java.util.Locale
import kotlin.math.abs

/**
 * Stable category to accent colors (keyword match first, then deterministic fallback palette).
 */
object CategoryPalette {
    private val CategoryPurple = Color(0xFF7C3AED)
    private val CategoryBlue   = Color(0xFF2563EB)
    private val CategorySlate  = Color(0xFF64748B)

    private val FALLBACK: List<Color> = listOf(
        Success,
        Info,
        CategoryPurple,
        Streak,
        XpGold,
        CategoryBlue,
        CategorySlate,
    )

    /** Order matters: first keyword match wins (Turkish-focused defaults). */
    private val KEYWORD_COLORS: List<Pair<String, Color>> = listOf(
        "spor" to Success,
        "sağlık" to Success,
        "egzersiz" to Success,
        "yürüyüş" to Success,
        "beslenme" to Success,
        "iş" to Info,
        "işler" to Info,
        "toplantı" to Info,
        "sunum" to Info,
        "proje" to Info,
        "fatura" to Info,
        "gelişim" to CategoryPurple,
        "kişisel" to CategoryPurple,
        "not" to CategoryPurple,
        "günlük" to CategoryPurple,
        "oku" to CategoryBlue,
        "ders" to CategoryBlue,
        "öğren" to CategoryBlue,
        "kitap" to CategoryBlue,
        "dil" to CategoryBlue,
        "sosyal" to Streak,
        "aile" to Streak,
        "çocuk" to Streak,
        "arkadaş" to Streak,
        "para" to XpGold,
        "finans" to XpGold,
        "yatırım" to XpGold,
        "ev" to CategorySlate,
        "temizlik" to CategorySlate,
        "market" to CategorySlate,
        "alışveriş" to CategorySlate,
        "bugün" to BrandPrimary,
    )

    fun colorFor(seed: String): Color {
        val v = seed.lowercase(Locale("tr", "TR")).trim().ifEmpty { return FALLBACK[0] }
        for ((kw, color) in KEYWORD_COLORS) {
            if (v.contains(kw)) return color
        }
        return FALLBACK[abs(v.hashCode()) % FALLBACK.size]
    }
}

