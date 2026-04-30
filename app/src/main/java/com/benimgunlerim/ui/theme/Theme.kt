@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R
import com.benimgunlerim.ui.today.theme.LocalTodayColorTokens
import com.benimgunlerim.ui.today.theme.todayColorTokens

// ── Nunito Font Family ────────────────────────────────────────────────────────
val Nunito = FontFamily(
    Font(R.font.nunito_regular,   FontWeight.Normal),
    Font(R.font.nunito_medium,    FontWeight.Medium),
    Font(R.font.nunito_semibold,  FontWeight.SemiBold),
    Font(R.font.nunito_bold,      FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

// ── BenimGünlerim — Core Palette ─────────────────────────────────────────────
val CandyPrimary      = Color(0xFF19B97A)  // Primary Mint
val CandyPrimaryLight = Color(0xFFE7FAF1)  // Mint Soft
val CandyPrimaryDark  = Color(0xFF0FA46A)  // Deep Mint
val CandySecondary    = Color(0xFF7C5CFF)  // Accent Purple
val CandySecondaryLt  = Color(0xFFF0EBFF)  // Purple Soft
val CandyTertiary     = Color(0xFFFFB648)  // Amber
val CandyTertiaryLt   = Color(0xFFFFF4DF)  // Amber Soft
val AccentPurple      = CandySecondary
val AccentPurpleSoft  = CandySecondaryLt
val AccentAmber       = CandyTertiary
val AccentAmberSoft   = CandyTertiaryLt
val AccentCoral       = Color(0xFFFF7B72)
val AccentCoralSoft   = Color(0xFFFFF0EE)
val AccentSky         = Color(0xFF4FA6FF)
val AccentSkySoft     = Color(0xFFEEF7FF)

// ── Oyunlaştırma Accent Renkleri ──────────────────────────────────────────────
val XpGold            = AccentAmber
val StreakCoral       = AccentCoral
val LevelSky          = AccentSky
val HeartPink         = AccentCoral

// ── Uyumluluk isimleri (eski importlara kırık vermemek için) ──────────────────
val GpPrimary       = CandyPrimary
val GpPrimaryDark   = CandyPrimaryDark
val GpPrimaryLight  = CandyPrimaryLight
val GpPrimaryMid    = CandyPrimaryLight
val StreakOrange     = StreakCoral
val LevelPurple     = CandyPrimary
val CompletedGreen  = CandyPrimary

// ── Kategori Renkleri (Candy Dream) ───────────────────────────────────────────
val CatHealth     = CandyPrimary
val CatDevelop    = AccentPurple
val CatWork       = AccentSky
val CatSocial     = AccentAmber
val CatPersonal   = AccentCoral

// ── Kategori Açık Renkler ─────────────────────────────────────────────────────
val CatHealthLight   = CandyPrimaryLight
val CatDevelopLight  = AccentPurpleSoft
val CatWorkLight     = AccentSkySoft
val CatSocialLight   = AccentAmberSoft
val CatPersonalLight = AccentCoralSoft

// ── Semantik Renkler ──────────────────────────────────────────────────────────
val SemanticSuccess  = CandyPrimary
val SemanticWarning  = AccentAmber
val SemanticError    = AccentCoral
val SemanticInfo     = AccentSky
val SemanticDisabled = Color(0xFF9CA3AF)

// ── Blok Renkleri (Sabah/Öğle/Akşam) ─────────────────────────────────────────
val BlockMorning   = AccentAmber
val BlockAfternoon = AccentSky
val BlockEvening   = AccentPurple

// ── Gradient Helpers ──────────────────────────────────────────────────────────
val HeroGradient = Brush.linearGradient(listOf(CandyPrimary, AccentPurple))
val XpGradient   = Brush.linearGradient(listOf(XpGold, CandyPrimary))
val StreakGradient = Brush.linearGradient(listOf(StreakCoral, AccentAmber))
val LevelGradient  = Brush.linearGradient(listOf(LevelSky, AccentPurple))

val BannerStart = CandyPrimary
val BannerEnd   = AccentPurple

// ── Light Mode (Candy Dream) ──────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary              = CandyPrimary,
    onPrimary            = Color.White,
    primaryContainer     = CandyPrimaryLight,
    onPrimaryContainer   = CandyPrimary,
    secondary            = AccentPurple,
    onSecondary          = Color.White,
    secondaryContainer   = AccentPurpleSoft,
    onSecondaryContainer = AccentPurple,
    tertiary             = AccentAmber,
    onTertiary           = Color.White,
    tertiaryContainer    = AccentAmberSoft,
    onTertiaryContainer  = AccentAmber,
    background           = Color(0xFFF6F8FC),
    surface              = Color(0xFFFFFFFF),
    surfaceVariant       = Color(0xFFF1F5F9),
    onBackground         = Color(0xFF0F172A),
    onSurface            = Color(0xFF0F172A),
    onSurfaceVariant     = Color(0xFF475569),
    outline              = Color(0xFFE5EAF2),
    outlineVariant       = Color(0xFFE5EAF2),
    error                = SemanticError,
    onError              = Color.White,
)

// ── Tipografi (Nunito — yuvarlak, sevimli) ────────────────────────────────────
private val AppTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge  = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,      fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp),
    headlineSmall  = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 26.sp),
    titleLarge     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall      = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.8.sp),
    labelLarge     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium,    fontSize = 12.sp, letterSpacing = 0.3.sp),
    labelSmall     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 10.sp, letterSpacing = 0.3.sp),
)

@Composable
fun BenimGunlerimTheme(themeMode: String = "system", content: @Composable () -> Unit) {
    val darkTheme = when (themeMode.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(
        LocalTodayColorTokens provides todayColorTokens(darkTheme),
    ) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}

