@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

// ═════════════════════════════════════════════════════════════════════════════
//  BenimGünlerim — Çekirdek Palet
//  Warm Companion: sage yeşil (primary), destekleyici mavi (secondary),
//  sıcak kayısı (tertiary).
// ═════════════════════════════════════════════════════════════════════════════

val CandyPrimary      = Color(0xFF0B6C43)
val CandyPrimaryLight = Color(0xFFE8F5EE)
val CandyPrimaryDark  = Color(0xFF004C2D)
val CandySecondary    = Color(0xFF396282)
val CandySecondaryLt  = Color(0xFFB0D9FD)
val CandyTertiary     = Color(0xFF815526)
val CandyTertiaryLt   = Color(0xFFFFF4E5)

// Semantik renkler
val SemanticSuccess   = CandyPrimary
val SemanticWarning   = CandyTertiary
val SemanticError     = Color(0xFFBA1A1A)
val SemanticInfo      = CandySecondary
val SemanticDisabled  = Color(0xFF9CA3AF)

// Accent takma adları (tek kaynak: yukarıdaki sabitler)
val AccentAmber       = CandyTertiary
val AccentAmberSoft   = CandyTertiaryLt
val AccentCoral       = SemanticError
val AccentCoralSoft   = Color(0xFFFDE8E8)
val AccentSky         = CandySecondary
val AccentSkySoft     = Color(0xFFCBE6FF)
val AccentPurple      = CandySecondary
val AccentPurpleSoft  = CandySecondaryLt

// Oyunlaştırma renkleri
val XpGold            = AccentAmber
val StreakCoral        = CandyTertiary
val LevelSky          = CandySecondary
val HeartPink         = AccentCoral

// Kategori renkleri
val CatHealth         = CandyPrimary
val CatDevelop        = CandySecondary
val CatWork           = CandySecondary
val CatSocial         = CandyTertiary
val CatPersonal       = SemanticError
val CatHealthLight    = CandyPrimaryLight
val CatDevelopLight   = CandySecondaryLt
val CatWorkLight      = AccentSkySoft
val CatSocialLight    = CandyTertiaryLt
val CatPersonalLight  = AccentCoralSoft

// Gradients
val HeroGradient      = Brush.linearGradient(listOf(CandyPrimary, CandySecondary))
val XpGradient        = Brush.linearGradient(listOf(XpGold, CandyPrimary))
val StreakGradient     = Brush.linearGradient(listOf(StreakCoral, AccentAmber))
val LevelGradient     = Brush.linearGradient(listOf(LevelSky, CandyPrimary))
val BannerStart       = CandyPrimary
val BannerEnd         = CandySecondary

// Container renkleri (soft)
val SuccessSoft       = Color(0xFFE8F5EE)
val WarningSoft       = Color(0xFFFFF4E5)
val ErrorSoft         = Color(0xFFFDE8E8)
val TextPrimary       = Color(0xFF1A1C1E)
val TextSecondary     = Color(0xFF44474A)

// Uyumluluk takma adları — eski importlar için korunuyor
val GpPrimary         = CandyPrimary
val GpPrimaryDark     = CandyPrimaryDark
val GpPrimaryLight    = CandyPrimaryLight
val GpPrimaryMid      = CandyPrimaryLight
val StreakOrange       = StreakCoral
val LevelPurple        = CandyPrimary
val CompletedGreen     = CandyPrimary

// ── Açık renk şeması ─────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary              = CandyPrimary,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFF6BBF8E),
    onPrimaryContainer   = Color(0xFF004C2D),
    secondary            = CandySecondary,
    onSecondary          = Color.White,
    secondaryContainer   = CandySecondaryLt,
    onSecondaryContainer = Color(0xFF365F7F),
    tertiary             = CandyTertiary,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFDBA36D),
    onTertiaryContainer  = Color(0xFF5F390B),
    background           = Color(0xFFF7F9FF),
    surface              = Color(0xFFFFFFFF),
    surfaceVariant       = Color(0xFFDCE3EC),
    surfaceContainer     = Color(0xFFE8EEF8),
    surfaceContainerHigh = Color(0xFFE2E9F2),
    surfaceContainerLow  = Color(0xFFEEF4FD),
    onBackground         = TextPrimary,
    onSurface            = TextPrimary,
    onSurfaceVariant     = TextSecondary,
    outline              = Color(0xFF6F7A71),
    outlineVariant       = Color(0xFFBEC9BF),
    error                = SemanticError,
    onError              = Color.White,
)

// ── Koyu renk şeması ─────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary              = Color(0xFF6BBF8E),
    onPrimary            = Color(0xFF003922),
    primaryContainer     = Color(0xFF00522F),
    onPrimaryContainer   = Color(0xFF8EDBB0),
    secondary            = Color(0xFF7DC2F4),
    onSecondary          = Color(0xFF00344F),
    secondaryContainer   = Color(0xFF1D4D6A),
    onSecondaryContainer = Color(0xFFB0D9FD),
    tertiary             = Color(0xFFDBA36D),
    onTertiary           = Color(0xFF462A00),
    tertiaryContainer    = Color(0xFF633D00),
    onTertiaryContainer  = Color(0xFFFDBC73),
    background           = Color(0xFF0F1512),
    surface              = Color(0xFF161E19),
    surfaceVariant       = Color(0xFF3E4A40),
    surfaceContainer     = Color(0xFF1E2921),
    surfaceContainerHigh = Color(0xFF253128),
    surfaceContainerLow  = Color(0xFF18221B),
    onBackground         = Color(0xFFE2E8E4),
    onSurface            = Color(0xFFE2E8E4),
    onSurfaceVariant     = Color(0xFFA8B5AC),
    outline              = Color(0xFF72807A),
    outlineVariant       = Color(0xFF3E4A40),
    error                = Color(0xFFF28B82),
    onError              = Color(0xFF690005),
)

// ── Tipografi ─────────────────────────────────────────────────────────────────
private val AppPlatformTextStyle = PlatformTextStyle(includeFontPadding = false)

private val AppTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge  = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Bold,      fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp),
    headlineSmall  = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 26.sp),
    titleLarge     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall      = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.8.sp),
    labelLarge     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.Medium,    fontSize = 12.sp, letterSpacing = 0.3.sp),
    labelSmall     = TextStyle(fontFamily = Nunito, platformStyle = AppPlatformTextStyle, fontWeight = FontWeight.SemiBold,  fontSize = 10.sp, letterSpacing = 0.3.sp),
)

// ── Shape seti (Radius token'larından türetilmiş) ─────────────────────────────
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(AppTokens.Radius.xs),   // 4dp
    small      = RoundedCornerShape(AppTokens.Radius.sm),   // 8dp
    medium     = RoundedCornerShape(AppTokens.Radius.md),   // 12dp
    large      = RoundedCornerShape(AppTokens.Radius.lg),   // 16dp
    extraLarge = RoundedCornerShape(AppTokens.Radius.xl),   // 20dp
)

// ── Tema giriş noktası ────────────────────────────────────────────────────────
@Composable
fun BenimGunlerimTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        "dark"  -> true
        "light" -> false
        else    -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(
        LocalTodayColorTokens provides todayColorTokens(isDark),
    ) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColors else LightColors,
            typography  = AppTypography,
            shapes      = AppShapes,
            content     = content,
        )
    }
}
