package com.benimgunlerim.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import com.benimgunlerim.ui.theme.Info
import com.benimgunlerim.ui.theme.InfoSoft
import com.benimgunlerim.ui.theme.Warning
import com.benimgunlerim.ui.theme.WarningSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayFabMenuSheet(
    isLightDayMode: Boolean,
    onDismiss: () -> Unit,
    onAddTaskClick: () -> Unit,
    onBrainDumpClick: () -> Unit,
    onResetClick: () -> Unit,
    onToggleLightDayClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.quick_actions_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            // 1. Görev Ekle
            QuickActionRow(
                title = "Görev Ekle",
                subtitle = "Bugün için tek seferlik yapılacak iş",
                icon = Icons.Rounded.Add,
                iconTint = BrandPrimary,
                iconBg = BrandPrimarySoft,
                onClick = onAddTaskClick,
            )

            // 2. Kafam Dolu (Zihin Boşaltma)
            QuickActionRow(
                title = stringResource(R.string.braindump_title),
                subtitle = "Aklındakileri dök ve göreve dönüştür",
                icon = Icons.Rounded.Psychology,
                iconTint = Info,
                iconBg = InfoSoft,
                onClick = onBrainDumpClick,
            )

            // 3. 1 Dakikalık Reset & Nefes
            QuickActionRow(
                title = stringResource(R.string.reset_title),
                subtitle = "Zihnini boşalt, nefes al, toparlan",
                icon = Icons.Rounded.SelfImprovement,
                iconTint = BrandPrimary,
                iconBg = BrandPrimarySoft,
                onClick = onResetClick,
            )

            // 4. Hafif Gün Modu
            QuickActionRow(
                title = if (isLightDayMode) stringResource(R.string.light_day_disable_btn) else stringResource(R.string.light_day_title),
                subtitle = if (isLightDayMode) "Normal tempoya geri dön" else "Bugünü iptal etmiyoruz, hafifletiyoruz",
                icon = Icons.Rounded.Spa,
                badge = if (isLightDayMode) "Aktif" else null,
                iconTint = if (isLightDayMode) BrandPrimary else Warning,
                iconBg = if (isLightDayMode) BrandPrimarySoft else WarningSoft,
                onClick = onToggleLightDayClick,
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String? = null,
    iconTint: Color = BrandPrimary,
    iconBg: Color = BrandPrimarySoft,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BrandPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimary,
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
