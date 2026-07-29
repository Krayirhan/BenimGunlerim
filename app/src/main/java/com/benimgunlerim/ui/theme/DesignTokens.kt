@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════════════════════
//  BenimGünlerim — Design Token System v1
//  Tüm hardcoded spacing/radius/elevation/motion değerleri buradan gelir.
//  Ekranlarda asla "16.dp" yazmayın; AppTokens.spacing.md kullanın.
// ═══════════════════════════════════════════════════════════════════════════════

object AppTokens {

    // ── Spacing ──────────────────────────────────────────────────────────────
    object Spacing {
        /** 4.dp — ikon-metin arası, badge iç boşluk */
        val xxs: Dp = 4.dp
        /** 8.dp — kompakt liste öğesi, chip iç padding */
        val xs: Dp = 8.dp
        /** 12.dp — kart iç boşluklar, liste gap */
        val sm: Dp = 12.dp
        /** 16.dp — standart ekran yatay padding */
        val md: Dp = 16.dp
        /** 20.dp — hero kart iç padding */
        val lg: Dp = 20.dp
        /** 24.dp — section aralığı, büyük kart padding */
        val xl: Dp = 24.dp
        /** 32.dp — ekran üst boşluk */
        val xxl: Dp = 32.dp

        /** Standart ekran yatay kenar boşluğu */
        val screenHorizontal: Dp = md
        /** Ekran üst başlık boşluğu */
        val screenTop: Dp = xxl
        /** Card iç padding (normal) */
        val cardInner: Dp = md
        /** Card iç padding (hero) */
        val cardInnerHero: Dp = lg
        /** Listede öğeler arası gap */
        val listGap: Dp = xs
        /** Section'lar arası boşluk */
        val sectionGap: Dp = xl
    }

    // ── Border Radius ────────────────────────────────────────────────────────
    object Radius {
        /** 8.dp — küçük chip, tag */
        val xs: Dp = 8.dp
        /** 12.dp — küçük kart, compact badge */
        val sm: Dp = 12.dp
        /** 16.dp — orta kart, sheet */
        val md: Dp = 16.dp
        /** 20.dp — standart kart */
        val lg: Dp = 20.dp
        /** 24.dp — hero kart */
        val xl: Dp = 24.dp
        /** 28.dp — büyük hero panel */
        val xxl: Dp = 28.dp
        /** 99.dp — tam yuvarlak (pill) */
        val pill: Dp = 99.dp
    }

    // ── Elevation ────────────────────────────────────────────────────────────
    object Elevation {
        /** 0.dp — içi dolu / borderli kartlar */
        val flat: Dp = 0.dp
        /** 2.dp — standart kart gölgesi */
        val card: Dp = 2.dp
        /** 4.dp — vurgulu kart */
        val cardEmphasized: Dp = 4.dp
        /** 8.dp — modal, bottom sheet */
        val modal: Dp = 8.dp
    }

    // ── Motion (animation durationMillis) ───────────────────────────────────
    object Motion {
        /** 150ms — micro-interaction, renk geçişi */
        const val fast: Int = 150
        /** 300ms — kart açılma, buton tap */
        const val normal: Int = 300
        /** 500ms — progress bar, hero transition */
        const val slow: Int = 500
        /** 800ms — pulse/infinite animation turu */
        const val pulse: Int = 800
    }

    // ── Icon Sizes ───────────────────────────────────────────────────────────
    object IconSize {
        /** 16.dp — inline/badge ikon */
        val xs: Dp = 16.dp
        /** 20.dp — liste satır ikonu */
        val sm: Dp = 20.dp
        /** 24.dp — standart nav/toolbar ikonu */
        val md: Dp = 24.dp
        /** 32.dp — kart başlık ikonu */
        val lg: Dp = 32.dp
        /** 48.dp — empty state / hero illüstrasyonu */
        val xl: Dp = 48.dp
    }

    // ── Bottom Nav ───────────────────────────────────────────────────────────
    object BottomNav {
        /** Sekme etiketi altındaki dot göstergesi boyutu */
        val indicatorDot: Dp = 4.dp
    }
}
