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
//  BenimGünlerim — Role-Based Design Token System
// ═════════════════════════════════════════════════════════════════════════════

// ── Brand ─────────────────────────────────────────────────────────────────────
val BrandPrimary       = Color(0xFF0B6C43)
val BrandPrimarySoft   = Color(0xFFE8F5EE)
val BrandPrimaryDark   = Color(0xFF004C2D)

// ── Success ───────────────────────────────────────────────────────────────────
val Success            = Color(0xFF138A55)
val SuccessSoft        = Color(0xFFEAF8F0)
val SuccessBorder      = Color(0xFFBFE8CF)

// ── Info ──────────────────────────────────────────────────────────────────────
val Info               = Color(0xFF396282)
val InfoSoft           = Color(0xFFE3F0FF)
val InfoBorder         = Color(0xFFBFD7EF)

// ── Warning / Attention ───────────────────────────────────────────────────────
val Warning            = Color(0xFFB45309)
val WarningSoft        = Color(0xFFFFF4E5)
val WarningBorder      = Color(0xFFF3D6B0)

// ── XP / Reward ───────────────────────────────────────────────────────────────
val XpGold             = Color(0xFFD97706)
val XpGoldSoft         = Color(0xFFFEF3C7)
val XpGoldBorder       = Color(0xFFF7D98B)
val LevelUpGradientStart = Color(0xFF147A4F)
val LevelUpGradientEnd   = Color(0xFF0D5C3A)

// ── Streak ────────────────────────────────────────────────────────────────────
val Streak             = Color(0xFFEA580C)
val StreakSoft         = Color(0xFFFFEDD5)
val StreakBorder       = Color(0xFFFDBA74)

// ── Error / Destructive ───────────────────────────────────────────────────────
val SemanticError      = Color(0xFFBA1A1A)
val ErrorSoft          = Color(0xFFFFE4E6)
val ErrorBorder        = Color(0xFFFCA5A5)
val SemanticDisabled   = Color(0xFF9CA3AF)

// ── Night / End of day ────────────────────────────────────────────────────────
val NightSurface       = Color(0xFF172536)
val NightSurfaceAlt    = Color(0xFF1E2F42)
val NightText          = Color(0xFFFFFFFF)
val NightMuted         = Color(0xFFCBD5E1)
val NightAccent        = Color(0xFFFBBF24)
val NightSurfaceContainer = Color(0xFF1E2921)
val NightPrimaryContainer = Color(0xFF0F3D2A)
val NightPrimary          = Color(0xFF7DDDA5)

// ── Text ──────────────────────────────────────────────────────────────────────
val TextPrimary        = Color(0xFF1A1C1E)
val TextSecondary      = Color(0xFF44474A)
val TextTertiary       = Color(0xFF6B7280)
val TextDisabled       = Color(0xFF9CA3AF)

// ── Surface & Border ──────────────────────────────────────────────────────────
val BackgroundNeutral  = Color(0xFFF8FAFC)
val SurfaceWhite       = Color(0xFFFFFFFF)
val SurfaceMuted       = Color(0xFFF1F5F9)
val Outline            = Color(0xFFBEC9BF)
val OutlineSubtle      = Color(0xFFE2E8F0)
val Divider            = Color(0xFFE8EDF2)
val DividerDark        = Color(0xFF2A372E)
val SuccessBorderDark  = Color(0xFF1E5E3E)
val SlateText          = Color(0xFF334155)

// ── Semantik alias'lar (eski çağrılarla tam uyumluluk) ───────────────────────
val CandyPrimary       = BrandPrimary
val CandyPrimaryLight  = BrandPrimarySoft
val CandyPrimaryDark   = BrandPrimaryDark
val CandySecondary     = Info
val CandySecondaryLt   = InfoSoft
val CandyTertiary      = Warning
val CandyTertiaryLt    = WarningSoft

val SemanticSuccess    = Success
val SemanticWarning    = Warning
val SemanticInfo       = Info

val AccentAmber        = XpGold
val AccentAmberSoft    = XpGoldSoft
val AccentCoral        = SemanticError
val AccentCoralSoft    = ErrorSoft
val AccentSky          = Info
val AccentSkySoft      = InfoSoft
val AccentPurple       = Color(0xFF7C3AED)
val AccentPurpleSoft   = Color(0xFFF3E8FF)

val StreakCoral        = Streak
val LevelSky           = Info
val HeartPink          = SemanticError

// Kategori renkleri
val CatHealth          = Success
val CatDevelop         = Info
val CatWork            = Info
val CatSocial          = Streak
val CatPersonal        = SemanticError
val CatHealthLight     = SuccessSoft
val CatDevelopLight    = InfoSoft
val CatWorkLight       = InfoSoft
val CatSocialLight     = StreakSoft
val CatPersonalLight   = ErrorSoft

// Gradients
val HeroGradient       = Brush.linearGradient(listOf(BrandPrimary, Info))
val XpGradient         = Brush.linearGradient(listOf(XpGold, BrandPrimary))
val StreakGradient     = Brush.linearGradient(listOf(Streak, XpGold))
val LevelGradient      = Brush.linearGradient(listOf(Info, BrandPrimary))
val BannerStart        = BrandPrimary
val BannerEnd          = Info

// Uyumluluk takma adları — eski importlar için korunuyor
val GpPrimary          = BrandPrimary
val GpPrimaryDark      = BrandPrimaryDark
val GpPrimaryLight     = BrandPrimarySoft
val GpPrimaryMid       = BrandPrimarySoft
val StreakOrange       = Streak
val LevelPurple        = BrandPrimary
val CompletedGreen     = Success

// ── Açık renk şeması ─────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary              = BrandPrimary,
    onPrimary            = Color.White,
    primaryContainer     = BrandPrimarySoft,
    onPrimaryContainer   = BrandPrimaryDark,
    secondary            = Info,
    onSecondary          = Color.White,
    secondaryContainer   = InfoSoft,
    onSecondaryContainer = Info,
    tertiary             = Warning,
    onTertiary           = Color.White,
    tertiaryContainer    = WarningSoft,
    onTertiaryContainer  = Warning,
    background           = BackgroundNeutral,
    surface              = SurfaceWhite,
    surfaceVariant       = SurfaceMuted,
    surfaceContainer     = SurfaceWhite,
    surfaceContainerHigh = SurfaceMuted,
    surfaceContainerLow  = BackgroundNeutral,
    onBackground         = TextPrimary,
    onSurface            = TextPrimary,
    onSurfaceVariant     = TextSecondary,
    outline              = Outline,
    outlineVariant       = OutlineSubtle,
    error                = SemanticError,
    onError              = Color.White,
    errorContainer       = ErrorSoft,
    onErrorContainer     = SemanticError,
)

// ── Koyu renk şeması ─────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary              = Color(0xFF7DDDA5),
    onPrimary            = Color(0xFF003922),
    primaryContainer     = Color(0xFF0F3D2A),
    onPrimaryContainer   = Color(0xFF8EDBB0),
    secondary            = Color(0xFF93C5FD),
    onSecondary          = Color(0xFF00344F),
    secondaryContainer   = Color(0xFF1D4D6A),
    onSecondaryContainer = Color(0xFFB0D9FD),
    tertiary             = Color(0xFFFBBF24),
    onTertiary           = Color(0xFF462A00),
    tertiaryContainer    = Color(0xFF633D00),
    onTertiaryContainer  = Color(0xFFFDBC73),
    background           = Color(0xFF0F1512),
    surface              = Color(0xFF161E19),
    surfaceVariant       = Color(0xFF253128),
    surfaceContainer     = Color(0xFF1E2921),
    surfaceContainerHigh = Color(0xFF253128),
    surfaceContainerLow  = Color(0xFF18221B),
    onBackground         = Color(0xFFE2E8E4),
    onSurface            = Color(0xFFE2E8E4),
    onSurfaceVariant     = Color(0xFFB7C2BB),
    outline              = Color(0xFF72807A),
    outlineVariant       = Color(0xFF3E4A40),
    error                = Color(0xFFFCA5A5),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF5C1414),
    onErrorContainer     = Color(0xFFFCA5A5),
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
    @Suppress("UNUSED_PARAMETER") themeMode: String = "light",
    content: @Composable () -> Unit,
) {
    // Karanlık mod devre dışı bırakıldı — uygulama tutarlı ve temiz açık modda çalışır.
    val isDark = false
    CompositionLocalProvider(
        LocalTodayColorTokens provides todayColorTokens(isDark = false),
    ) {
        MaterialTheme(
            colorScheme = LightColors,
            typography  = AppTypography,
            shapes      = AppShapes,
            content     = content,
        )
    }
}
