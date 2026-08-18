package com.benimgunlerim.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import com.benimgunlerim.ui.theme.Divider
import com.benimgunlerim.ui.theme.SemanticError
import com.benimgunlerim.ui.theme.SlateText
import com.benimgunlerim.ui.theme.SuccessBorder
import com.benimgunlerim.ui.theme.SurfaceMuted

/**
 * Profesyonel, bağlamsal ve sakin tasarıma sahip merkezi AppTopBar bileşeni.
 * - 40dp avatar / bildirim görsel alanı, 48dp minimum touch target
 * - Sayfaya göre dinamik başlık ve altyazı desteği
 * - Çok yumuşak ve göz yormayan alt sınır (divider)
 */
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    showProfile: Boolean = true,
    showNotification: Boolean = true,
    hasNotificationBadge: Boolean = false,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val topBarBg = MaterialTheme.colorScheme.surface
    val dividerColor = Divider
    val avatarBg = BrandPrimarySoft
    val avatarStroke = SuccessBorder
    val avatarIconColor = BrandPrimary
    val bellBg = SurfaceMuted
    val bellIconColor = SlateText
    val badgeColor = SemanticError

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = topBarBg,
        shadowElevation = AppTokens.Elevation.flat,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppTokens.Layout.topBarHeight)
                    .padding(horizontal = AppTokens.Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ── Sol: Profil / Avatar ──
                if (showProfile) {
                    val profileCd = stringResource(R.string.today_profile_cd)
                    Box(
                        modifier = Modifier
                            .size(AppTokens.TouchTarget.min)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = AppTokens.Spacing.xl),
                                onClick = onProfileClick,
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = profileCd
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(AppTokens.Layout.topBarAvatarDiameter)
                                .clip(CircleShape)
                                .background(avatarBg)
                                .border(AppTokens.BorderWidth.thin, avatarStroke, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = avatarIconColor,
                                modifier = Modifier.size(AppTokens.IconSize.sm),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(AppTokens.Spacing.sm))
                }

                // ── Orta: Başlık & Altyazı ──
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }

                // ── Sağ: Bildirim Butonu ──
                if (showNotification) {
                    val notificationCd = stringResource(R.string.today_notifications_cd)
                    Box(
                        modifier = Modifier
                            .size(AppTokens.TouchTarget.min)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = AppTokens.Spacing.xl),
                                onClick = onNotificationClick,
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = notificationCd
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(AppTokens.Layout.topBarAvatarDiameter)
                                .clip(CircleShape)
                                .background(bellBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsNone,
                                contentDescription = null,
                                tint = bellIconColor,
                                modifier = Modifier.size(AppTokens.IconSize.sm),
                            )
                            if (hasNotificationBadge) {
                                Box(
                                    modifier = Modifier
                                        .size(AppTokens.Spacing.xs)
                                        .align(Alignment.TopEnd)
                                        .padding(top = AppTokens.Spacing.xs, end = AppTokens.Spacing.xs)
                                        .clip(CircleShape)
                                        .background(badgeColor),
                                )
                            }
                        }
                    }
                }
            }

            // ── Alt: Çok Soft Divider ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppTokens.BorderWidth.thin)
                    .background(dividerColor),
            )
        }
    }
}
