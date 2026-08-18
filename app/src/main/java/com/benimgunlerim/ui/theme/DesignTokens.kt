@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════════════════════
//  BenimGünlerim — Design Token System v2
//  Katı kural: Ekran ve bileşen dosyalarında asla DP literal yazmayın.
//  Her ölçü buradan gelir veya buradan türetilir.
//  Ölçek: 4dp grid. xxs=4 xs=8 sm=12 md=16 lg=20 xl=24 xxl=32
// ═══════════════════════════════════════════════════════════════════════════════

object AppTokens {

    // ── Spacing ──────────────────────────────────────────────────────────────
    object Spacing {
        /** 0dp — AppSurface'i iç boşluksuz kullanmak için, örn. bölünmüş satır listeleri */
        val none: Dp = 0.dp
        /** 4dp — mikro: badge iç boşluk, ikon-metin arası */
        val xxs: Dp = 4.dp
        /** 8dp — kompakt: chip iç dolgu, liste gap */
        val xs:  Dp = 8.dp
        /** 12dp — küçük: yoğun kart dolgu, eleman grubu */
        val sm:  Dp = 12.dp
        /** 16dp — standart: kart iç dolgu, yatay padding */
        val md:  Dp = 16.dp
        /** 20dp — orta: hero kart dolgu, navigasyon yüksekliği */
        val lg:  Dp = 20.dp
        /** 24dp — büyük: section arası, bölüm boşluğu */
        val xl:  Dp = 24.dp
        /** 32dp — ekstra: ekran üst boşluğu */
        val xxl: Dp = 32.dp

        /** Standart ekran yatay kenar boşluğu — 20dp */
        val screenHorizontal: Dp = lg
        /** Ekran içerik üst boşluğu — 24dp */
        val screenTop: Dp = xl
        /** Kart iç dolgu (normal) — 16dp */
        val cardInner: Dp = md
        /** Kart iç dolgu (hero) — 20dp */
        val cardInnerHero: Dp = lg
        /** Liste öğeleri arası gap — 8dp */
        val listGap: Dp = xs
        /** Section'lar arası boşluk — 24dp */
        val sectionGap: Dp = xl
    }

    // ── Border Radius ────────────────────────────────────────────────────────
    object Radius {
        /** 4dp — çok küçük: dot göstergesi, mikro badge */
        val xs:   Dp = 4.dp
        /** 8dp — küçük: chip, tag, kompakt badge */
        val sm:   Dp = 8.dp
        /** 12dp — standart: kart, buton */
        val md:   Dp = 12.dp
        /** 16dp — büyük: modal, bottom sheet, büyük kart */
        val lg:   Dp = 16.dp
        /** 20dp — ekstra büyük: hero kart */
        val xl:   Dp = 20.dp
        /** 28dp — maksimum: büyük hero panel */
        val xxl:  Dp = 28.dp
        /** 99dp — tam yuvarlak (pill) */
        val pill: Dp = 99.dp
    }

    // ── Elevation ────────────────────────────────────────────────────────────
    object Elevation {
        /** 0dp — gölgesiz: border/arka plan ile ayrılan kartlar */
        val flat:           Dp = 0.dp
        /** 0dp — standart kart (border ile ayrışır) */
        val card:           Dp = 0.dp
        /** 0dp — vurgulu kart (renk ile ayrışır) */
        val cardEmphasized: Dp = 0.dp
        /** 8dp — modal yüzeyler, bottom sheet */
        val modal:          Dp = 8.dp
    }

    // ── Motion (animasyon süresi — ms) ──────────────────────────────────────
    object Motion {
        /** 150ms — mikro: renk geçişi, opacity */
        const val fast:     Int = 150
        /** 300ms — standart: kart açılma, buton tap */
        const val normal:   Int = 300
        /** 500ms — yavaş: progress bar, hero geçişi */
        const val slow:     Int = 500
        /** 800ms — infinite animasyon turu (pulse, shimmer) */
        const val pulse:    Int = 800
    }

    // ── Icon Sizes ───────────────────────────────────────────────────────────
    object IconSize {
        /** 16dp — inline / badge ikon */
        val xs: Dp = 16.dp
        /** 20dp — liste satır ikonu */
        val sm: Dp = 20.dp
        /** 24dp — standart nav / toolbar ikonu */
        val md: Dp = 24.dp
        /** 32dp — kart başlık ikonu */
        val lg: Dp = 32.dp
        /** 48dp — empty state / hero illüstrasyon ikonu */
        val xl: Dp = 48.dp
        /** 36dp — dairesel ikon konteyneri (örn. gün kapatma ok butonu) */
        val container: Dp = 36.dp
        /** 44dp — büyük seçim kartı ikon konteyneri (örn. onboarding ihtiyaç/tempo kartları) */
        val containerLarge: Dp = 44.dp
    }

    // ── Bottom Nav ───────────────────────────────────────────────────────────
    object BottomNav {
        /** Seçili sekme üstündeki dot gösterge boyutu */
        val indicatorDot: Dp = 6.dp
    }

    // ── Touch Target ─────────────────────────────────────────────────────────
    object TouchTarget {
        /** 48dp — birincil dokunma alanı asgari boyutu */
        val min: Dp = 48.dp
        /** 44dp — ikincil/kompakt eylem dokunma alanı (banner aksiyonu vb.) */
        val compact: Dp = 44.dp
    }

    // ── Border Width ─────────────────────────────────────────────────────────
    object BorderWidth {
        /** 1dp — standart ince kenarlık */
        val thin: Dp = 1.dp
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    object Layout {
        /** 32dp — FAB'ın ekranın alt kenarından offseti */
        val fabBottomOffset: Dp = 32.dp
        /** 72dp — Snackbar'ın ekranın alt kenarından offseti */
        val snackbarBottomOffset: Dp = 72.dp
        /** 68dp — AppTopBar içerik yüksekliği */
        val topBarHeight: Dp = 68.dp
        /** 40dp — AppTopBar avatar/bildirim ikon konteyneri çapı */
        val topBarAvatarDiameter: Dp = 40.dp
        /** 56dp — Material FAB varsayılan boyutu; ilk frame'de gerçek ölçüm gelmeden önce kullanılır */
        val fabDefaultSize: Dp = 56.dp
    }

    // ── Calm / Reset ─────────────────────────────────────────────────────────
    object Calm {
        val breathingCircleOuter: Dp = 180.dp
        val breathingCircleInner: Dp = 110.dp
        val breathingBorder: Dp = 2.dp
        val resetTitleIcon: Dp = IconSize.lg
        val resetTitleIconSize: Dp = 18.dp
        val resetButtonHeight: Dp = 52.dp
        val resetButtonRadius: Dp = Radius.lg
        val resetCompletedIcon: Dp = 56.dp
        val resetCompletedIconSize: Dp = IconSize.lg
    }
}
