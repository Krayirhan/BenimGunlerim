@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToOnboarding: () -> Unit = {},
) {
    val prefs by viewModel.preferences.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showOssLicenses by remember { mutableStateOf(false) }

    ScreenScaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            // Bildirimler ve Deneyim
            SectionBlock(title = stringResource(R.string.settings_section_experience)) {
                AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
                    Column {
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_status_notification),
                            subtitle = stringResource(R.string.settings_notif_mode_desc_light),
                            checked = prefs.notificationMode != "off",
                            onCheckedChange = { viewModel.setNotificationMode(if (it) "light" else "off") },
                        )
                        AppDivider()
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_celebration_title),
                            subtitle = stringResource(R.string.settings_celebration_subtitle),
                            checked = prefs.celebrationEffectsEnabled,
                            onCheckedChange = { viewModel.setCelebrationEffectsEnabled(it) },
                        )
                        AppDivider()
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_morning_title),
                            subtitle = stringResource(R.string.settings_morning_subtitle),
                            checked = prefs.morningPlannerEnabled,
                            onCheckedChange = { viewModel.setMorningPlannerEnabled(it) },
                        )
                    }
                }
            }

            // Zamanlama ve Hatırlatıcılar
            SectionBlock(title = stringResource(R.string.settings_section_reminders)) {
                AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
                    Column {
                        SettingsInfoRow(
                            title = stringResource(R.string.settings_day_end_time_label),
                            value = prefs.dailySummaryTime,
                            icon = Icons.Rounded.Schedule,
                        )
                        AppDivider()
                        SettingsInfoRow(
                            title = stringResource(R.string.settings_morning_time_label),
                            value = prefs.morningPlannerTime,
                            icon = Icons.Rounded.Notifications,
                        )
                    }
                }
            }

            // Uygulama Hakkında & Gizlilik
            SectionBlock(title = stringResource(R.string.settings_section_privacy)) {
                AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
                    Column {
                        SettingsClickableRow(
                            title = "Başlangıç Rehberi (Onboarding)",
                            subtitle = "Tanıtım adımlarını ve ilk kurulum akışını incele",
                            icon = Icons.Rounded.AutoAwesome,
                            onClick = onNavigateToOnboarding,
                        )
                        AppDivider()
                        SettingsClickableRow(
                            title = "Açık kaynak lisansları",
                            subtitle = "Kullandığımız açık kaynak kütüphaneler",
                            icon = Icons.Rounded.Code,
                            onClick = { showOssLicenses = true },
                        )
                        AppDivider()
                        SettingsInfoRow(
                            title = stringResource(R.string.app_name),
                            value = stringResource(
                                R.string.settings_version_format,
                                BuildConfig.VERSION_NAME,
                                BuildConfig.VERSION_CODE,
                            ),
                            icon = Icons.Rounded.Info,
                        )
                        AppDivider()
                        SettingsInfoRow(
                            title = stringResource(R.string.settings_status_data),
                            value = stringResource(R.string.settings_data_local),
                            icon = Icons.Rounded.Info,
                        )
                    }
                }
            }

            // Tehlikeli Bölge
            SectionBlock(title = stringResource(R.string.settings_section_data)) {
                AppSurface(
                    radius = AppTokens.Radius.md,
                    padding = AppTokens.Spacing.none,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearDialog = true }
                            .padding(AppTokens.Spacing.cardInner),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_clear_data_row_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = stringResource(R.string.settings_data_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.settings_clear_confirm_title)) },
            text = { Text(stringResource(R.string.settings_clear_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLocalData()
                        showClearDialog = false
                    },
                ) {
                    Text(
                        stringResource(R.string.settings_clear_confirm_btn),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showOssLicenses) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showOssLicenses = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        ) {
            OssLicensesScreen(onNavigateBack = { showOssLicenses = false })
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTokens.Spacing.cardInner),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTokens.Spacing.cardInner),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(AppTokens.Spacing.cardInner),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}
