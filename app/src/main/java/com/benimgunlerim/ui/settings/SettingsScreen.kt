package com.benimgunlerim.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.AlertBanner
import com.benimgunlerim.ui.components.molecules.AlertBannerSeverity
import com.benimgunlerim.ui.theme.AppTokens

@Suppress("LongMethod", "CyclomaticComplexMethod")
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
                .padding(contentPadding)
                .testTag(TestTags.SettingsRoot),
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

            SettingsExperienceSection(
                prefs = prefs,
                onSetNotificationMode = { viewModel.setNotificationMode(it) },
                onSetCelebrationEffects = { viewModel.setCelebrationEffectsEnabled(it) },
                onSetMorningPlanner = { viewModel.setMorningPlannerEnabled(it) },
            )

            SettingsRemindersSection(prefs = prefs)

            SettingsPrivacySection(
                prefs = prefs,
                onSetAnalyticsEnabled = { viewModel.setAnalyticsEnabled(it) },
                onOpenPrivacyPolicy = { showPrivacyPolicy = true },
                onNavigateToOnboarding = onNavigateToOnboarding,
                onOpenOssLicenses = { showOssLicenses = true },
            )

            SettingsDataSection(
                onExportClick = { viewModel.exportDataToFile() },
                onImportClick = { showImportConfirmDialog = true },
                onClearClick = { showClearDialog = true },
            )
        }
    }

    if (showClearDialog) {
        SettingsClearConfirmDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = {
                viewModel.clearLocalData()
                showClearDialog = false
            },
        )
    }

    if (showImportConfirmDialog) {
        SettingsImportConfirmDialog(
            onDismiss = { showImportConfirmDialog = false },
            onConfirm = {
                showImportConfirmDialog = false
                viewModel.requestImportFromFile()
            },
        )
    }

    if (showOssLicenses) {
        Dialog(
            onDismissRequest = { showOssLicenses = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            OssLicensesScreen(onNavigateBack = { showOssLicenses = false })
        }
    }

    if (showPrivacyPolicy) {
        Dialog(
            onDismissRequest = { showPrivacyPolicy = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            PrivacyPolicyScreen(onNavigateBack = { showPrivacyPolicy = false })
        }
    }
}
