@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppDivider
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.AlertBanner
import com.benimgunlerim.ui.components.molecules.AlertBannerSeverity
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToOnboarding: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsState()
    val dataMessage by viewModel.dataOperationMessage.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var showOssLicenses by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    var pendingExportContent by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val content = pendingExportContent
        pendingExportContent = null
        if (uri != null && content != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                } ?: error("Output stream is null")
            }.onSuccess {
                viewModel.setDataOperationMessage(SettingsEvent.ExportSaved)
            }.onFailure {
                viewModel.setDataOperationMessage(SettingsEvent.ExportWriteFailed)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val content = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            viewModel.importDataFromFileContent(content)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is SettingsUiEffect.SaveExportJson -> {
                    pendingExportContent = effect.content
                    exportLauncher.launch(effect.fileName)
                }
                is SettingsUiEffect.RequestImportJson -> {
                    importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                }
            }
        }
    }

    ScreenScaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            // Operasyon bildirim banner'ı
            if (dataMessage != null) {
                val (severity, msgText) = when (val msg = dataMessage) {
                    is SettingsEvent.DataCleared -> AlertBannerSeverity.Info to stringResource(R.string.settings_msg_cleared)
                    is SettingsEvent.ExportSaved -> AlertBannerSeverity.Info to stringResource(R.string.settings_msg_export_saved)
                    is SettingsEvent.ExportFailed -> AlertBannerSeverity.Error to stringResource(R.string.settings_msg_export_failed)
                    is SettingsEvent.ExportWriteFailed -> AlertBannerSeverity.Error to stringResource(R.string.settings_msg_export_write_failed)
                    is SettingsEvent.ImportReadFailed -> AlertBannerSeverity.Error to stringResource(R.string.settings_msg_import_read_failed)
                    is SettingsEvent.ImportParseFailed -> AlertBannerSeverity.Error to stringResource(R.string.settings_msg_import_parse_failed)
                    is SettingsEvent.ImportSuccess -> AlertBannerSeverity.Info to stringResource(R.string.settings_msg_import_success, msg.tasks, msg.routines)
                    null -> AlertBannerSeverity.Info to ""
                }
                AlertBanner(
                    message = msgText,
                    severity = severity,
                    actionLabel = stringResource(R.string.settings_dismiss_msg),
                    action = { viewModel.clearDataOperationMessage() },
                )
            }

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

            // Gizlilik & Güvenlik
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
                            onCheckedChange = { viewModel.setAnalyticsEnabled(it) },
                        )
                        AppDivider()
                        SettingsClickableRow(
                            title = "Gizlilik Politikası",
                            subtitle = "Veri güvenliği ve gizlilik taahhüdümüz",
                            icon = Icons.Rounded.Security,
                            onClick = { showPrivacyPolicy = true },
                        )
                        AppDivider()
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

            // Yedekleme ve Veri Yönetimi
            SectionBlock(title = stringResource(R.string.settings_section_data)) {
                AppSurface(radius = AppTokens.Radius.md, padding = AppTokens.Spacing.none) {
                    Column {
                        SettingsClickableRow(
                            title = stringResource(R.string.settings_export_btn),
                            subtitle = "Tüm görev, rutin ve ayarları JSON dosyasına aktar",
                            icon = Icons.Rounded.CloudUpload,
                            onClick = { viewModel.exportDataToFile() },
                        )
                        AppDivider()
                        SettingsClickableRow(
                            title = stringResource(R.string.settings_import_btn),
                            subtitle = "Daha önce alınan JSON yedeğini geri yükle",
                            icon = Icons.Rounded.CloudDownload,
                            onClick = { showImportConfirmDialog = true },
                        )
                        AppDivider()
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

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text(stringResource(R.string.settings_import_confirm_title)) },
            text = { Text(stringResource(R.string.settings_import_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        viewModel.requestImportFromFile()
                    },
                ) {
                    Text(
                        stringResource(R.string.settings_import_confirm_btn),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
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

    if (showPrivacyPolicy) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showPrivacyPolicy = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        ) {
            PrivacyPolicyScreen(onNavigateBack = { showPrivacyPolicy = false })
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
