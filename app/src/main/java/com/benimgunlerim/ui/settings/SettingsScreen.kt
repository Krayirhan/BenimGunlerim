package com.benimgunlerim.ui.settings

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.ui.theme.AccentCoral
import com.benimgunlerim.ui.theme.AccentCoralSoft
import com.benimgunlerim.ui.theme.AccentPurple
import com.benimgunlerim.ui.theme.AccentPurpleSoft
import com.benimgunlerim.ui.theme.AccentSky
import com.benimgunlerim.ui.theme.AccentSkySoft
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandyPrimaryDark
import com.benimgunlerim.ui.theme.CandyPrimaryLight

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsState()
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

    if (showClearDataDialog) {
        ClearDataDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                viewModel.clearLocalData()
                showClearDataDialog = false
            },
        )
    }

    Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                    title = "Hatırlatmalar",
                    subtitle = "Bildirim sıklığını ve gün sonu saatini ayarla.",
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
                    title = "Gizlilik",
                    subtitle = "Hangi kullanım bilgisinin paylaşılacağını belirle.",
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
                    title = "Uygulama verisi",
                    subtitle = "Verilerin cihazda tutulur ve buradan yönetilir.",
                )
            }
            item {
                LocalDataCard(
                    preferences = preferences,
                    onClearData = { showClearDataDialog = true },
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
                        text = "Ayarlar",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "BenimGünlerim'in nasıl çalışacağını buradan yönet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeaderStatusPill(
                        label = "Bildirim",
                        value = notificationModeLabel(preferences.notificationMode),
                        modifier = Modifier.weight(1f),
                    )
                    HeaderStatusPill(
                        label = "Gün sonu",
                        value = preferences.dailySummaryTime,
                        modifier = Modifier.weight(1f),
                    )
                    HeaderStatusPill(
                        label = "Veri",
                        value = "Yerel",
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
            text = "Bildirim modu",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = notificationModeDescription(mode),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceChips(
            options = listOf(
                "off" to "Kapalı",
                "light" to "Hafif",
                "normal" to "Normal",
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
            label = { Text("Gün sonu özeti saati") },
            placeholder = { Text("21:00") },
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
            Text("Saati kaydet")
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
            text = "Bildirimler kapalı. Rutin ve gün sonu hatırlatmaları planlanmaz.",
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    if (notificationsGranted) {
        StatusMessage(
            text = "Bildirim izni aktif.",
            container = CandyPrimaryLight,
            content = CandyPrimary,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusMessage(
                text = "Hatırlatmaları göstermek için bildirim izni gerekiyor.",
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
                Text("Bildirim izni ver")
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
                Text("Sabah planlayıcısı", style = MaterialTheme.typography.titleMedium)
                Text("Her sabah güne hazırlık bildirimi al", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            OutlinedTextField(
                value = time,
                onValueChange = onTimeChange,
                label = { Text("Bildirim saati") },
                placeholder = { Text("08:00") },
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
                Text("Saati kaydet")
            }
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
                Text("Sessiz saatler", style = MaterialTheme.typography.titleMedium)
                Text("Bu aralıkta bildirim gönderilmez", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            OutlinedTextField(
                value = start,
                onValueChange = onStartChange,
                label = { Text("Başlangıç saati") },
                placeholder = { Text("22:00") },
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
                Text("Başlangıç saatini kaydet")
            }
            OutlinedTextField(
                value = end,
                onValueChange = onEndChange,
                label = { Text("Bitiş saati") },
                placeholder = { Text("07:00") },
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
                Text("Bitiş saatini kaydet")
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
                    text = "Anonim kullanım ölçümü",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (analyticsEnabled) {
                        "Uygulamanın hangi alanlarının kullanıldığını anonim olarak ölçer."
                    } else {
                        "Anonim ölçüm kapalı. Temel özellikler aynı şekilde çalışır."
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
            text = "Görev başlığı, rutin adı, not ve gün özeti içeriği gönderilmez.",
            container = AccentPurpleSoft,
            content = AccentPurple,
        )
    }
}

@Composable
private fun LocalDataCard(
    preferences: UserPreferences,
    onClearData: () -> Unit,
) {
    SettingsCard(
        accent = AccentSky,
        icon = { Icon(Icons.Rounded.Storage, contentDescription = null) },
    ) {
        Text(
            text = "Yerel veri yönetimi",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Görevler, rutinler, ilerleme kayıtları ve ayarlar bu cihazda tutulur.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DataMetric(
                label = "XP",
                value = preferences.totalXp.toString(),
                modifier = Modifier.weight(1f),
            )
            DataMetric(
                label = "Görev",
                value = preferences.totalTasksCompleted.toString(),
                modifier = Modifier.weight(1f),
            )
            DataMetric(
                label = "Rutin",
                value = preferences.totalRoutinesCompleted.toString(),
                modifier = Modifier.weight(1f),
            )
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
            Text("Yerel verileri temizle")
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
            text = "BenimGünlerim",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Offline çalışan görev, rutin ve ilerleme merkezi.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusMessage(
            text = "Tema sadeleştirildi: uygulama artık yalnızca açık modda çalışır.",
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
        title = { Text("Yerel veriler temizlensin mi?") },
        text = {
            Text(
                text = "Görevler, rutinler, ilerleme kayıtları ve onboarding durumu silinir. Bu işlem geri alınamaz.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Temizle", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        },
    )
}

private fun notificationModeLabel(mode: String): String = when (mode) {
    "off" -> "Kapalı"
    "normal" -> "Normal"
    else -> "Hafif"
}

private fun notificationModeDescription(mode: String): String = when (mode) {
    "off" -> "Bildirimler kapalı. Uygulama içindeki takip çalışmaya devam eder."
    "normal" -> "Normal mod, rutin ve gün sonu hatırlatmalarını daha belirgin tutar."
    else -> "Hafif mod, gün içinde daha sakin ve az sayıda hatırlatma kullanır."
}

private fun String.sanitizedTimeInput(): String = filter { it.isDigit() || it == ':' }.take(5)
