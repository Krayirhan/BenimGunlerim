package com.benimgunlerim.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.theme.AccentCoral
import com.benimgunlerim.ui.theme.AccentCoralSoft
import com.benimgunlerim.ui.theme.AccentPurple
import com.benimgunlerim.ui.theme.AccentPurpleSoft
import com.benimgunlerim.ui.theme.AccentSky
import com.benimgunlerim.ui.theme.AccentSkySoft
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandyPrimaryDark
import com.benimgunlerim.ui.theme.CandyPrimaryLight
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsState()
    val dataOperationMessage by viewModel.dataOperationMessage.collectAsState()
    var dailySummaryTime by remember(preferences.dailySummaryTime) {
        mutableStateOf(preferences.dailySummaryTime)
    }
    var morningPlannerTime by remember(preferences.morningPlannerTime) {
        mutableStateOf(preferences.morningPlannerTime)
    }
    var quietHoursStart by remember(preferences.quietHoursStart) {
        mutableStateOf(preferences.quietHoursStart)
    }
    var quietHoursEnd by remember(preferences.quietHoursEnd) {
        mutableStateOf(preferences.quietHoursEnd)
    }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showImportDataDialog by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var notificationsGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationsGranted = granted
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val pendingJson = pendingExportJson
        if (uri != null && pendingJson != null) {
            val exported = context.writeTextToUri(uri, pendingJson)
            viewModel.setDataOperationMessage(
                if (exported) {
                    SettingsEvent.ExportSaved
                } else {
                    SettingsEvent.ExportWriteFailed
                },
            )
        }
        pendingExportJson = null
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importDataFromFileContent(context.readTextFromUri(uri))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is SettingsUiEffect.SaveExportJson -> {
                    pendingExportJson = effect.content
                    exportLauncher.launch(effect.fileName)
                }

                SettingsUiEffect.RequestImportJson -> {
                    importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                }
            }
        }
    }

    if (showClearDataDialog) {
        ClearDataDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                viewModel.clearLocalData()
                showClearDataDialog = false
            },
        )
    }
    if (showImportDataDialog) {
        ImportDataDialog(
            onDismiss = { showImportDataDialog = false },
            onConfirm = {
                showImportDataDialog = false
                viewModel.requestImportFromFile()
            },
        )
    }

    Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.SettingsRoot)
                .background(SettingsBackground())
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = 18.dp,
                end = 16.dp,
                bottom = 106.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SettingsHeader(preferences = preferences)
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.settings_section_reminders),
                    subtitle = stringResource(R.string.settings_section_reminders_sub),
                )
            }
            item {
                NotificationSettingsCard(
                    mode = preferences.notificationMode,
                    dailySummaryTime = dailySummaryTime,
                    notificationsGranted = notificationsGranted,
                    onModeChange = viewModel::setNotificationMode,
                    onTimeChange = { dailySummaryTime = it.sanitizedTimeInput() },
                    onSaveTime = { viewModel.setDailySummaryTime(dailySummaryTime) },
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
            }
            item {
                MorningPlannerCard(
                    enabled = preferences.morningPlannerEnabled,
                    time = morningPlannerTime,
                    onEnabledChange = viewModel::setMorningPlannerEnabled,
                    onTimeChange = { morningPlannerTime = it.sanitizedTimeInput() },
                    onSaveTime = { viewModel.setMorningPlannerTime(morningPlannerTime) },
                )
            }
            item {
                QuietHoursCard(
                    enabled = preferences.quietHoursEnabled,
                    start = quietHoursStart,
                    end = quietHoursEnd,
                    onEnabledChange = viewModel::setQuietHoursEnabled,
                    onStartChange = { quietHoursStart = it.sanitizedTimeInput() },
                    onEndChange = { quietHoursEnd = it.sanitizedTimeInput() },
                    onSaveStart = { viewModel.setQuietHoursStart(quietHoursStart) },
                    onSaveEnd = { viewModel.setQuietHoursEnd(quietHoursEnd) },
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.settings_section_experience),
                    subtitle = stringResource(R.string.settings_section_experience_sub),
                )
            }
            item {
                CelebrationEffectsCard(
                    enabled = preferences.celebrationEffectsEnabled,
                    onEnabledChange = viewModel::setCelebrationEffectsEnabled,
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.settings_section_privacy),
                    subtitle = stringResource(R.string.settings_section_privacy_sub),
                )
            }
            item {
                PrivacySettingsCard(
                    analyticsEnabled = preferences.analyticsEnabled,
                    onAnalyticsChange = viewModel::setAnalyticsEnabled,
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.settings_section_data),
                    subtitle = stringResource(R.string.settings_section_data_sub),
                )
            }
            item {
                LocalDataCard(
                    preferences = preferences,
                    dataOperationMessage = dataOperationMessage,
                    onExportData = {
                        viewModel.exportDataToFile()
                    },
                    onImportData = { showImportDataDialog = true },
                    onClearData = { showClearDataDialog = true },
                    onDismissMessage = viewModel::clearDataOperationMessage,
                )
            }

            item {
                AppInfoCard()
            }
        }
    }
}

@Composable
private fun SettingsBackground(): Brush = Brush.verticalGradient(
    listOf(
        Color(0xFFEAF8F2),
        Color(0xFFF0EEFF),
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.background,
    ),
)

@Composable
private fun SettingsHeader(preferences: UserPreferences) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                val modeLabel = when (preferences.notificationMode) {
                    "off" -> stringResource(R.string.settings_notif_mode_off)
                    "normal" -> stringResource(R.string.settings_notif_mode_normal)
                    else -> stringResource(R.string.settings_notif_mode_light)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeaderStatusPill(
                        label = stringResource(R.string.settings_status_notification),
                        value = modeLabel,
                        modifier = Modifier.weight(1f),
                    )
                    HeaderStatusPill(
                        label = stringResource(R.string.settings_status_day_end),
                        value = preferences.dailySummaryTime,
                        modifier = Modifier.weight(1f),
                    )
                    HeaderStatusPill(
                        label = stringResource(R.string.settings_status_data),
                        value = stringResource(R.string.settings_data_local),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderStatusPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CandyPrimaryLight)
            .border(
                border = BorderStroke(1.dp, CandyPrimary.copy(alpha = 0.18f)),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = CandyPrimary,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CandyPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotificationSettingsCard(
    mode: String,
    dailySummaryTime: String,
    notificationsGranted: Boolean,
    onModeChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onSaveTime: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    SettingsCard(
        accent = CandyPrimary,
        icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
    ) {
        Text(
            text = stringResource(R.string.settings_notif_mode_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = when (mode) {
                "off" -> stringResource(R.string.settings_notif_mode_desc_off)
                "normal" -> stringResource(R.string.settings_notif_mode_desc_normal)
                else -> stringResource(R.string.settings_notif_mode_desc_light)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceChips(
            options = listOf(
                "off" to stringResource(R.string.settings_notif_mode_off),
                "light" to stringResource(R.string.settings_notif_mode_light),
                "normal" to stringResource(R.string.settings_notif_mode_normal),
            ),
            selected = mode,
            onSelected = onModeChange,
        )
        PermissionStatus(
            enabled = mode != "off",
            notificationsGranted = notificationsGranted,
            onRequestPermission = onRequestPermission,
        )
        OutlinedTextField(
            value = dailySummaryTime,
            onValueChange = onTimeChange,
            label = { Text(stringResource(R.string.settings_day_end_time_label)) },
            placeholder = { Text(stringResource(R.string.settings_default_daily_summary_time)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = mode != "off",
            singleLine = true,
        )
        Button(
            onClick = onSaveTime,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
            enabled = mode != "off",
        ) {
            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.settings_save_time_btn))
        }
    }
}

@Composable
private fun PermissionStatus(
    enabled: Boolean,
    notificationsGranted: Boolean,
    onRequestPermission: () -> Unit,
) {
    if (!enabled) {
        StatusMessage(
            text = stringResource(R.string.settings_notif_disabled_msg),
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    if (notificationsGranted) {
        StatusMessage(
            text = stringResource(R.string.settings_notif_permission_granted),
            container = CandyPrimaryLight,
            content = CandyPrimary,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusMessage(
                text = stringResource(R.string.settings_notif_permission_needed),
                container = AccentCoralSoft,
                content = AccentCoral,
            )
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
            ) {
                Text(stringResource(R.string.settings_notif_permission_btn))
            }
        }
    }
}

@Composable
private fun MorningPlannerCard(
    enabled: Boolean,
    time: String,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit,
    onSaveTime: () -> Unit,
) {
    SettingsCard(
        accent = AccentSky,
        icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_morning_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.settings_morning_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            OutlinedTextField(
                value = time,
                onValueChange = onTimeChange,
                label = { Text(stringResource(R.string.settings_morning_time_label)) },
                placeholder = { Text(stringResource(R.string.settings_default_morning_planner_time)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            Button(
                onClick = onSaveTime,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentSky),
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_save_time_btn))
            }
        }
    }
}

@Composable
private fun CelebrationEffectsCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    SettingsCard(
        accent = CandyPrimary,
        icon = { Icon(Icons.Rounded.Celebration, contentDescription = null) },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_celebration_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.settings_celebration_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
    }
}

@Composable
private fun QuietHoursCard(
    enabled: Boolean,
    start: String,
    end: String,
    onEnabledChange: (Boolean) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onSaveStart: () -> Unit,
    onSaveEnd: () -> Unit,
) {
    SettingsCard(
        accent = AccentPurple,
        icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_quiet_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.settings_quiet_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            OutlinedTextField(
                value = start,
                onValueChange = onStartChange,
                label = { Text(stringResource(R.string.settings_quiet_start_label)) },
                placeholder = { Text(stringResource(R.string.settings_default_quiet_hours_start)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            Button(
                onClick = onSaveStart,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_quiet_save_start))
            }
            OutlinedTextField(
                value = end,
                onValueChange = onEndChange,
                label = { Text(stringResource(R.string.settings_quiet_end_label)) },
                placeholder = { Text(stringResource(R.string.settings_default_quiet_hours_end)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
            Button(
                onClick = onSaveEnd,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_quiet_save_end))
            }
        }
    }
}

@Composable
private fun PrivacySettingsCard(
    analyticsEnabled: Boolean,
    onAnalyticsChange: (Boolean) -> Unit,
) {
    SettingsCard(
        accent = AccentPurple,
        icon = { Icon(Icons.Rounded.PrivacyTip, contentDescription = null) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_privacy_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (analyticsEnabled) {
                        stringResource(R.string.settings_analytics_on_desc)
                    } else {
                        stringResource(R.string.settings_analytics_off_desc)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = analyticsEnabled,
                onCheckedChange = onAnalyticsChange,
            )
        }
        StatusMessage(
            text = stringResource(R.string.settings_privacy_pii_note),
            container = AccentPurpleSoft,
            content = AccentPurple,
        )
    }
}

@Composable
private fun LocalDataCard(
    preferences: UserPreferences,
    dataOperationMessage: SettingsEvent?,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    onClearData: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    SettingsCard(
        accent = AccentSky,
        icon = { Icon(Icons.Rounded.Storage, contentDescription = null) },
    ) {
        Text(
            text = stringResource(R.string.settings_data_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.settings_data_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusMessage(
            text = stringResource(R.string.settings_data_export_warning),
            container = AccentSkySoft,
            content = AccentSky,
        )
        dataOperationMessage?.let { event ->
            val message = when (event) {
                SettingsEvent.DataCleared -> stringResource(R.string.settings_msg_cleared)
                SettingsEvent.ExportFailed -> stringResource(R.string.settings_msg_export_failed)
                SettingsEvent.ExportSaved -> stringResource(R.string.settings_msg_export_saved)
                SettingsEvent.ExportWriteFailed -> stringResource(R.string.settings_msg_export_write_failed)
                SettingsEvent.ImportReadFailed -> stringResource(R.string.settings_msg_import_read_failed)
                SettingsEvent.ImportParseFailed -> stringResource(R.string.settings_msg_import_parse_failed)
                is SettingsEvent.ImportSuccess -> stringResource(R.string.settings_msg_import_success, event.tasks, event.routines)
            }
            StatusMessage(
                text = message,
                container = AccentSkySoft,
                content = AccentSky,
            )
            TextButton(onClick = onDismissMessage) {
                Text(stringResource(R.string.settings_dismiss_msg))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DataMetric(
                label = stringResource(R.string.settings_metric_xp),
                value = preferences.totalXp.toString(),
                modifier = Modifier.weight(1f),
            )
            DataMetric(
                label = stringResource(R.string.settings_metric_task),
                value = preferences.totalTasksCompleted.toString(),
                modifier = Modifier.weight(1f),
            )
            DataMetric(
                label = stringResource(R.string.settings_metric_routine),
                value = preferences.totalRoutinesCompleted.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = onExportData,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag(TestTags.SettingsExportButton),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentSky),
        ) {
            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.settings_export_btn))
        }
        OutlinedButton(
            onClick = onImportData,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag(TestTags.SettingsImportButton),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AccentSky),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentSky),
        ) {
            Icon(Icons.Rounded.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.settings_import_btn))
        }
        OutlinedButton(
            onClick = onClearData,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AccentCoral),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral),
        ) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.settings_clear_btn))
        }
    }
}

@Composable
private fun DataMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun AppInfoCard() {
    SettingsCard(
        accent = CandyPrimary,
        icon = {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(4.dp, CandyPrimary, CircleShape),
            )
        },
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.settings_app_offline_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusMessage(
            text = stringResource(R.string.settings_theme_note),
            container = AccentSkySoft,
            content = AccentSky,
        )
    }
}

@Composable
private fun SettingsCard(
    accent: Color,
    icon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(220.dp)
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalAccentIcon(accent = accent, icon = icon)
                }
                content()
            }
        }
    }
}

@Composable
private fun CompositionLocalAccentIcon(accent: Color, icon: @Composable () -> Unit) {
    Surface(color = Color.Transparent, contentColor = accent) {
        icon()
    }
}

@Composable
private fun StatusMessage(text: String, container: Color, content: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = content,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SingleChoiceChips(
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(value) },
                label = { Text(label) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = CandyPrimary,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = CandyPrimary,
                ),
            )
        }
    }
}

@Composable
private fun ClearDataDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_clear_confirm_title)) },
        text = {
            Text(
                text = stringResource(R.string.settings_clear_confirm_body),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.settings_clear_confirm_btn), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun ImportDataDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_import_confirm_title)) },
        text = {
            Text(
                text = stringResource(R.string.settings_import_confirm_body),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.settings_import_confirm_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private fun String.sanitizedTimeInput(): String = filter { it.isDigit() || it == ':' }.take(5)

private fun Context.writeTextToUri(uri: Uri, text: String): Boolean =
    runCatching {
        contentResolver.openOutputStream(uri)?.use { output ->
            output.write(text.toByteArray(Charsets.UTF_8))
        } ?: error("No output stream")
    }.isSuccess

private fun Context.readTextFromUri(uri: Uri): String? =
    runCatching {
        contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
    }.getOrNull()
