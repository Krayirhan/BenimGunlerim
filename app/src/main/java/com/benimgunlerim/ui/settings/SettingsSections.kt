package com.benimgunlerim.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.R
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.TestTags

@Composable
internal fun SettingsExperienceSection(
    prefs: UserPreferences,
    onSetNotificationMode: (String) -> Unit,
    onSetCelebrationEffects: (Boolean) -> Unit,
    onSetMorningPlanner: (Boolean) -> Unit,
) {
    SectionBlock(title = stringResource(R.string.settings_section_experience)) {
        AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
            Column {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_status_notification),
                    subtitle = stringResource(R.string.settings_notif_mode_desc_light),
                    checked = prefs.notificationMode != "off",
                    onCheckedChange = { onSetNotificationMode(if (it) "light" else "off") },
                )
                AppDivider()
                SettingsToggleRow(
                    title = stringResource(R.string.settings_celebration_title),
                    subtitle = stringResource(R.string.settings_celebration_subtitle),
                    checked = prefs.celebrationEffectsEnabled,
                    onCheckedChange = onSetCelebrationEffects,
                )
                AppDivider()
                SettingsToggleRow(
                    title = stringResource(R.string.settings_morning_title),
                    subtitle = stringResource(R.string.settings_morning_subtitle),
                    checked = prefs.morningPlannerEnabled,
                    onCheckedChange = onSetMorningPlanner,
                )
            }
        }
    }
}

@Composable
internal fun SettingsRemindersSection(prefs: UserPreferences) {
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
}

@Composable
internal fun SettingsPrivacySection(
    prefs: UserPreferences,
    onSetAnalyticsEnabled: (Boolean) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onOpenOssLicenses: () -> Unit,
) {
    SectionBlock(title = stringResource(R.string.settings_section_privacy)) {
        AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
            Column {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_privacy_title),
                    subtitle = stringResource(
                        if (prefs.analyticsEnabled) R.string.settings_analytics_on_desc
                        else R.string.settings_analytics_off_desc,
                    ),
                    checked = prefs.analyticsEnabled,
                    onCheckedChange = onSetAnalyticsEnabled,
                )
                AppDivider()
                SettingsClickableRow(
                    title = stringResource(R.string.settings_privacy_policy_title),
                    subtitle = stringResource(R.string.settings_privacy_policy_desc),
                    icon = Icons.Rounded.Security,
                    onClick = onOpenPrivacyPolicy,
                )
                AppDivider()
                SettingsClickableRow(
                    title = stringResource(R.string.settings_onboarding_row_title),
                    subtitle = stringResource(R.string.settings_onboarding_row_desc),
                    icon = Icons.Rounded.AutoAwesome,
                    onClick = onNavigateToOnboarding,
                )
                AppDivider()
                SettingsClickableRow(
                    title = stringResource(R.string.settings_oss_licenses_title),
                    subtitle = stringResource(R.string.settings_oss_licenses_desc),
                    icon = Icons.Rounded.Code,
                    onClick = onOpenOssLicenses,
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
}

@Composable
internal fun SettingsDataSection(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    SectionBlock(title = stringResource(R.string.settings_section_data)) {
        AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
            Column {
                SettingsClickableRow(
                    title = stringResource(R.string.settings_export_btn),
                    subtitle = stringResource(R.string.settings_export_desc),
                    icon = Icons.Rounded.CloudUpload,
                    modifier = Modifier.testTag(TestTags.SettingsExportButton),
                    onClick = onExportClick,
                )
                AppDivider()
                SettingsClickableRow(
                    title = stringResource(R.string.settings_import_btn),
                    subtitle = stringResource(R.string.settings_import_desc),
                    icon = Icons.Rounded.CloudDownload,
                    modifier = Modifier.testTag(TestTags.SettingsImportButton),
                    onClick = onImportClick,
                )
                AppDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = AppTokens.TouchTarget.min)
                        .clickable(onClick = onClearClick)
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

@Composable
internal fun SettingsToggleRow(
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
internal fun SettingsInfoRow(
    title: String,
    value: String,
    icon: ImageVector,
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
internal fun SettingsClickableRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppTokens.TouchTarget.min)
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

@Composable
internal fun SettingsClearConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_clear_confirm_title)) },
        text = { Text(stringResource(R.string.settings_clear_confirm_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.settings_clear_confirm_btn),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
internal fun SettingsImportConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_import_confirm_title)) },
        text = { Text(stringResource(R.string.settings_import_confirm_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.settings_import_confirm_btn),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}
