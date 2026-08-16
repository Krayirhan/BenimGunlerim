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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val isDark = MaterialTheme.colorScheme.surface.let {
        (it.red * 0.299 + it.green * 0.587 + it.blue * 0.114) < 0.5
    }

    val topBarBg = MaterialTheme.colorScheme.surface
    val dividerColor = if (isDark) Color(0xFF2A372E) else Color(0xFFE8EDF2)
    val avatarBg = if (isDark) Color(0xFF0F3D2A) else Color(0xFFE8F5EE)
    val avatarStroke = if (isDark) Color(0xFF1E5E3E) else Color(0xFFBFE8CF)
    val avatarIconColor = if (isDark) Color(0xFF7DDDA5) else Color(0xFF0B6C43)
    val bellBg = if (isDark) Color(0xFF1E2921) else Color(0xFFF1F5F9)
    val bellIconColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val badgeColor = Color(0xFFBA1A1A)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = topBarBg,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ── Sol: Profil / Avatar ──
                if (showProfile) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 24.dp),
                                onClick = onProfileClick,
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = "Profil"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(avatarBg)
                                .border(1.dp, avatarStroke, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = avatarIconColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 24.dp),
                                onClick = onNotificationClick,
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = "Bildirimler"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(bellBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsNone,
                                contentDescription = null,
                                tint = bellIconColor,
                                modifier = Modifier.size(20.dp),
                            )
                            if (hasNotificationBadge) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
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
                    .height(1.dp)
                    .background(dividerColor),
            )
        }
    }
}
