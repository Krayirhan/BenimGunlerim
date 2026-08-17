package com.benimgunlerim.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import com.benimgunlerim.ui.theme.CompletedGreen

// ── Data Models ───────────────────────────────────────────────────────────────

private data class NeedOption(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
)

private data class IntensityOption(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val recommended: Boolean = false,
)

// ── Main Composable ───────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onComplete: (String, String, List<String>, String?) -> Unit) {
    val context = LocalContext.current

    val needOptions = remember {
        listOf(
            NeedOption("duzen", R.string.onboarding_need_organize, R.string.onboarding_need_organize_sub, Icons.Rounded.WbSunny),
            NeedOption("duzenli", R.string.onboarding_need_regular, R.string.onboarding_need_regular_sub, Icons.Rounded.CalendarMonth),
            NeedOption("saglik", R.string.onboarding_need_health, R.string.onboarding_need_health_sub, Icons.Rounded.WaterDrop),
            NeedOption("odak", R.string.onboarding_need_focus, R.string.onboarding_need_focus_sub, Icons.Rounded.CenterFocusStrong),
            NeedOption("basit", R.string.onboarding_need_simple, R.string.onboarding_need_simple_sub, Icons.Rounded.EditNote),
        )
    }

    val intensityOptions = remember {
        listOf(
            IntensityOption("hafif", R.string.onboarding_intensity_light, R.string.onboarding_intensity_light_sub, Icons.Rounded.Spa, recommended = true),
            IntensityOption("dengeli", R.string.onboarding_intensity_balanced, R.string.onboarding_intensity_balanced_sub, Icons.Rounded.SelfImprovement),
            IntensityOption("yogun", R.string.onboarding_intensity_intense, R.string.onboarding_intensity_intense_sub, Icons.Rounded.LocalFireDepartment),
        )
    }

    var page by remember { mutableIntStateOf(0) }
    var selectedNeed by remember { mutableStateOf(needOptions[0]) }
    var selectedIntensity by remember { mutableStateOf(intensityOptions[0]) }
    var notifSkipped by remember { mutableStateOf(false) }

    val routineSuggestions = remember(selectedNeed, selectedIntensity) {
        OnboardingSuggestions.suggestedRoutines(selectedNeed.id, selectedIntensity.id)
    }
    val localizedRoutineSuggestions = routineSuggestions.map { suggestion ->
        suggestion to stringResource(suggestion.nameRes)
    }
    var selectedRoutineNames by remember(selectedNeed, selectedIntensity) {
        mutableStateOf(localizedRoutineSuggestions.filter { it.first.defaultSelected }.map { it.second }.toSet())
    }

    val suggestedTaskTitleText = stringResource(OnboardingSuggestions.suggestedTaskTitle(selectedNeed.id))
    var taskTitle by remember(selectedNeed) {
        mutableStateOf(suggestedTaskTitleText)
    }
    var taskSelected by remember(selectedNeed) { mutableStateOf(true) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* state re-checks on recompose */ }
    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else true

    val handleFinish = {
        onComplete(
            selectedNeed.id,
            selectedIntensity.id,
            selectedRoutineNames.toList(),
            if (taskSelected) taskTitle else null,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        // ── Top Bar: Step Indicator + Skip Button ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_step_format, page + 1, 4),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandPrimary,
                )
                // 4-step progress pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == page) 24.dp else 8.dp, 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i < page) CompletedGreen
                                    else if (i == page) BrandPrimary
                                    else BrandPrimary.copy(alpha = 0.20f),
                                ),
                        )
                    }
                }
            }

            TextButton(
                onClick = handleFinish,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                ),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Page Content Container with Animated Transition ──
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "onboarding_step_transition",
            ) { targetPage ->
                when (targetPage) {
                    // ═══════════════════════════════════════════════════════════════
                    // 1. Ekran: Şu an neye ihtiyacın var?
                    // ═══════════════════════════════════════════════════════════════
                    0 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_p1_title),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.onboarding_p1_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            needOptions.forEach { need ->
                                NeedCard(
                                    need = need,
                                    selected = need == selectedNeed,
                                    onClick = { selectedNeed = need },
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // 2. Ekran: Nasıl bir tempoyla başlamak istersin?
                    // ═══════════════════════════════════════════════════════════════
                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.onboarding_p2_title),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.onboarding_p2_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            intensityOptions.forEach { intensity ->
                                IntensityCard(
                                    intensity = intensity,
                                    selected = intensity == selectedIntensity,
                                    onClick = { selectedIntensity = intensity },
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // 3. Ekran: Sana önerdiğimiz rutinler + Mini Konsept Notu
                    // ═══════════════════════════════════════════════════════════════
                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_routines_title),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.onboarding_routines_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            localizedRoutineSuggestions.forEach { (suggestion, suggestionName) ->
                                val isSelected = suggestionName in selectedRoutineNames
                                SelectableRoutineRow(
                                    name = suggestionName,
                                    icon = suggestion.icon,
                                    selected = isSelected,
                                    onToggle = {
                                        selectedRoutineNames = if (isSelected) {
                                            selectedRoutineNames - suggestionName
                                        } else {
                                            selectedRoutineNames + suggestionName
                                        }
                                    },
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Mini Konsept Notu (Zarif Bilgi Kutucuğu)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BrandPrimarySoft)
                                    .border(1.dp, BrandPrimary.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = stringResource(R.string.onboarding_concept_mini_tip),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = BrandPrimary,
                                )
                            }
                        }
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // 4. Ekran: Hazırsın (Özet + İlk Görev + Bildirim + Başla)
                    // ═══════════════════════════════════════════════════════════════
                    3 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_ready_title),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.onboarding_ready_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Başlangıç Planı Özeti Rozeti
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BrandPrimarySoft)
                                    .border(1.dp, BrandPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text = stringResource(R.string.onboarding_ready_summary_title),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = BrandPrimary,
                                    )
                                }
                                Text(
                                    text = stringResource(
                                        R.string.onboarding_ready_summary_format,
                                        selectedRoutineNames.size,
                                        if (taskSelected) 1 else 0,
                                    ),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrandPrimary,
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // İlk Görev Kartı
                            SelectableRoutineRow(
                                name = taskTitle,
                                icon = Icons.Rounded.EditNote,
                                selected = taskSelected,
                                onToggle = { taskSelected = !taskSelected },
                                badge = stringResource(R.string.onboarding_today_task_badge),
                                badgeColor = BrandPrimary,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Zarif Hatırlatma / Bildirim Kartı
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                    .padding(16.dp),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BrandPrimarySoft),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Notifications,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = stringResource(R.string.onboarding_notif_soft_title),
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Text(
                                                text = stringResource(R.string.onboarding_notif_soft_body),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    if (notifGranted) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(BrandPrimarySoft)
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = CompletedGreen,
                                                modifier = Modifier.size(18.dp),
                                            )
                                            Text(
                                                text = stringResource(R.string.onboarding_notif_granted_badge),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = BrandPrimary,
                                            )
                                        }
                                    } else if (!notifSkipped) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            TextButton(
                                                onClick = { notifSkipped = true },
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.onboarding_notif_later),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = BrandPrimary,
                                                ),
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.onboarding_notif_grant),
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Color.White,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Bottom Navigation Buttons ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (page > 0) {
                TextButton(
                    onClick = { page-- },
                    modifier = Modifier.height(54.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_back),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = {
                    if (page < 3) {
                        page++
                    } else {
                        handleFinish()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                ),
            ) {
                Text(
                    text = if (page < 3) stringResource(R.string.action_next) else stringResource(R.string.onboarding_finish_btn),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}

// ── Need Card ─────────────────────────────────────────────────────────────────

@Composable
private fun NeedCard(need: NeedOption, selected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) BrandPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        animationSpec = tween(180),
        label = "need_border",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "need_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) BrandPrimarySoft else MaterialTheme.colorScheme.surface)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (selected) BrandPrimary else BrandPrimarySoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = need.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else BrandPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(need.titleRes),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(need.subtitleRes),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.90f),
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

// ── Intensity Card ────────────────────────────────────────────────────────────

@Composable
private fun IntensityCard(intensity: IntensityOption, selected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) BrandPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        animationSpec = tween(180),
        label = "intensity_border",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) BrandPrimarySoft else MaterialTheme.colorScheme.surface)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (selected) BrandPrimary else BrandPrimarySoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = intensity.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else BrandPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(intensity.titleRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (intensity.recommended) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BrandPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_recommended),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(intensity.subtitleRes),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.90f),
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

// ── Selectable Routine Row ────────────────────────────────────────────────────

@Composable
private fun SelectableRoutineRow(
    name: String,
    icon: ImageVector? = null,
    selected: Boolean,
    onToggle: () -> Unit,
    badge: String? = null,
    badgeColor: Color = BrandPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) BrandPrimarySoft else MaterialTheme.colorScheme.surface,
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) BrandPrimary.copy(alpha = 0.40f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = BrandPrimary,
                checkmarkColor = Color.White,
            ),
        )
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (selected) BrandPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor,
                )
            }
        }
    }
}
