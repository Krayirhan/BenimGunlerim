package com.benimgunlerim.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.benimgunlerim.ui.components.VerticalSpacer
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandyPrimaryLight
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CandyTertiary
import com.benimgunlerim.ui.theme.CompletedGreen

// ── Data models ───────────────────────────────────────────────────────────────

private data class NeedOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>,
)

private data class IntensityOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val recommended: Boolean = false,
)

private data class SuggestedRoutine(val name: String, val defaultSelected: Boolean = true)

private val needOptions = listOf(
    NeedOption("duzen", "Günümü toparlamak istiyorum", "Sabah-akşam dengesi, küçük adımlar", listOf(CandyPrimary, CandyTertiary)),
    NeedOption("duzenli", "Daha düzenli olmak istiyorum", "Plan, liste, takip — kontrol sende", listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
    NeedOption("saglik", "Sağlıklı rutin kurmak istiyorum", "Su, hareket, nefes — her gün biraz", listOf(Color(0xFF10B981), CandySecondary)),
    NeedOption("odak", "Odaklanmak istiyorum", "Az ama kaliteli. Dikkat dağılmasını azalt", listOf(Color(0xFFF59E0B), Color(0xFFEA580C))),
    NeedOption("basit", "Basit bir planlayıcı istiyorum", "Fazlasına gerek yok, sade ve hızlı", listOf(Color(0xFF64748B), Color(0xFF475569))),
)

private val intensityOptions = listOf(
    IntensityOption("hafif", "Hafif başlangıç", "2-3 küçük adım. Kolay sürer.", recommended = true),
    IntensityOption("dengeli", "Dengeli başlangıç", "4-5 rutin ve görev. Orta tempo."),
    IntensityOption("yogun", "Yoğun başlangıç", "6+ adım. Hızlı başla, çok yap."),
)

private fun suggestedRoutines(needId: String, intensityId: String): List<SuggestedRoutine> =
    when (needId) {
        "duzen" -> listOf(
            SuggestedRoutine("Sabah rutini yap"),
            SuggestedRoutine("Günlük plan yap"),
            SuggestedRoutine("Akşam günü kapat", intensityId != "hafif"),
        )
        "duzenli" -> listOf(
            SuggestedRoutine("Bugün önceliklerimi belirle"),
            SuggestedRoutine("Görev listemi güncelle"),
            SuggestedRoutine("Haftalık özet", intensityId == "yogun"),
        )
        "saglik" -> listOf(
            SuggestedRoutine("Su iç (2L hedefi)"),
            SuggestedRoutine("5 dk yürüyüş"),
            SuggestedRoutine("Nefes egzersizi", intensityId != "hafif"),
        )
        "odak" -> listOf(
            SuggestedRoutine("Pomodoro seansı (25 dk)"),
            SuggestedRoutine("Dikkat dağıtıcıları kapat"),
            SuggestedRoutine("Günlük öğrenme notu", intensityId != "hafif"),
        )
        "basit" -> listOf(
            SuggestedRoutine("Sabah listemi hazırla"),
            SuggestedRoutine("Akşam günü kapat", intensityId != "hafif"),
        )
        else -> emptyList()
    }

private fun suggestedTaskTitle(needId: String): String = when (needId) {
    "duzen" -> "Günümü planla"
    "duzenli" -> "Bugünkü 3 önceliğimi belirle"
    "saglik" -> "1 bardak su iç"
    "odak" -> "Bugünün odak konusunu seç"
    "basit" -> "Yapılacaklar listemi hazırla"
    else -> "İlk görev"
}

// ── Main Composable ───────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onComplete: (String, String, List<String>, String?) -> Unit) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }
    var selectedNeed by remember { mutableStateOf(needOptions[0]) }
    var selectedIntensity by remember { mutableStateOf(intensityOptions[0]) }

    val routineSuggestions = remember(selectedNeed, selectedIntensity) {
        suggestedRoutines(selectedNeed.id, selectedIntensity.id)
    }
    var selectedRoutineNames by remember(selectedNeed, selectedIntensity) {
        mutableStateOf(routineSuggestions.filter { it.defaultSelected }.map { it.name }.toSet())
    }
    var taskTitle by remember(selectedNeed) {
        mutableStateOf(suggestedTaskTitle(selectedNeed.id))
    }
    var taskSelected by remember(selectedNeed) { mutableStateOf(true) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* permission result is handled by re-checking on recompose */ }
    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Page indicator (6 dots)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            repeat(6) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == page) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (i < page) CompletedGreen
                            else if (i == page) CandyPrimary
                            else CandyPrimary.copy(alpha = 0.2f),
                        ),
                )
            }
        }

        when (page) {
            // ═══════════════════════════════════════════════════════════════
            // Page 0: Karşılama
            // ═══════════════════════════════════════════════════════════════
            0 -> {
                VerticalSpacer(40)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Brush.linearGradient(listOf(CandyPrimary, CandyTertiary))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
                }
                VerticalSpacer(28)
                Text(
                    text = "Gününü daha sakin\nve düzenli yaşa",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(16)
                Text(
                    text = "Küçük ama sürdürülebilir bir başlangıç hazırlayalım.\nSadece birkaç adım.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(48)
                Button(
                    onClick = { page = 1 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
                ) {
                    Text("Başlayalım", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // Page 1: Hedef Seçimi
            // ═══════════════════════════════════════════════════════════════
            1 -> {
                Text(
                    text = "Şu an neye ihtiyacın var?",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(8)
                Text(
                    text = "Seçimin önerilen rutinleri ve görevleri belirler.\nSonra istediğin gibi değiştirebilirsin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(20)
                needOptions.forEach { need ->
                    NeedCard(
                        need = need,
                        selected = need == selectedNeed,
                        onClick = { selectedNeed = need },
                    )
                    VerticalSpacer(10)
                }
                VerticalSpacer(16)
                Button(
                    onClick = { page = 2 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
                ) {
                    Text("İleri", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // Page 2: Tempo Seçimi
            // ═══════════════════════════════════════════════════════════════
            2 -> {
                Text(
                    text = "Nasıl bir tempoyla başlamak istersin?",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(8)
                Text(
                    text = "Hafif başlangıç daha kolay sürer.\nİstediğin zaman artırabilirsin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(24)
                intensityOptions.forEach { intensity ->
                    IntensityCard(
                        intensity = intensity,
                        selected = intensity == selectedIntensity,
                        onClick = { selectedIntensity = intensity },
                    )
                    VerticalSpacer(12)
                }
                VerticalSpacer(20)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { page = 1 }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(20.dp)) {
                        Text("Geri")
                    }
                    Button(
                        onClick = { page = 3 },
                        modifier = Modifier.weight(2f).height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
                    ) {
                        Text("İleri", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // Page 3: Rutin ve Görev Farkı
            // ═══════════════════════════════════════════════════════════════
            3 -> {
                Text(
                    text = "İki kavramı tanı",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(8)
                Text(
                    text = "Bu uygulama görevleri ve rutinleri birbirinden ayırır.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(28)
                ExplainerCard(
                    icon = Icons.Rounded.DateRange,
                    color = CandySecondary,
                    title = "Rutin",
                    description = "Her gün tekrar eden alışkanlık. Su iç, yürüyüş yap, ilaç al. Seri takibi yapar.",
                )
                VerticalSpacer(14)
                ExplainerCard(
                    icon = Icons.Rounded.Refresh,
                    color = CandyPrimary,
                    title = "Görev",
                    description = "Tek seferlik yapılacak iş. Toplantı, alışveriş, fatura ödeme. Tamamlandı, bitti.",
                )
                VerticalSpacer(28)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { page = 2 }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(20.dp)) {
                        Text("Geri")
                    }
                    Button(
                        onClick = { page = 4 },
                        modifier = Modifier.weight(2f).height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
                    ) {
                        Text("Anladım", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // Page 4: Önerilen Rutinleri Seç
            // ═══════════════════════════════════════════════════════════════
            4 -> {
                Text(
                    text = "Hangi rutinleri ekleyelim?",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(8)
                Text(
                    text = "Seçtiğin rutinler her gün listene eklenir.\nİstemeyen kutucukları kaldırabilirsin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(24)
                routineSuggestions.forEach { suggestion ->
                    val isSelected = suggestion.name in selectedRoutineNames
                    SelectableRoutineRow(
                        name = suggestion.name,
                        selected = isSelected,
                        onToggle = {
                            selectedRoutineNames = if (isSelected) {
                                selectedRoutineNames - suggestion.name
                            } else {
                                selectedRoutineNames + suggestion.name
                            }
                        },
                    )
                    VerticalSpacer(10)
                }
                VerticalSpacer(16)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { page = 3 }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(20.dp)) {
                        Text("Geri")
                    }
                    Button(
                        onClick = { page = 5 },
                        modifier = Modifier.weight(2f).height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CandyPrimary),
                    ) {
                        Text("İleri", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // Page 5: İlk Görev + Bildirim
            // ═══════════════════════════════════════════════════════════════
            5 -> {
                Text(
                    text = "Son birkaç adım",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(8)
                Text(
                    text = "Başlangıç görevi eklemek ister misin?\nEklemeden de devam edebilirsin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                VerticalSpacer(24)
                SelectableRoutineRow(
                    name = taskTitle,
                    selected = taskSelected,
                    onToggle = { taskSelected = !taskSelected },
                    badge = "Bugünkü görev",
                    badgeColor = CandyPrimary,
                )
                VerticalSpacer(24)
                if (!notifGranted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CandySecondary.copy(0.08f))
                            .border(1.dp, CandySecondary.copy(0.18f), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Hatırlatmalar açık olsun mu?",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            )
                            Text(
                                "Görev ve rutin hatırlatmaları için bildirim iznine ihtiyacımız var.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            )
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CandySecondary),
                            ) {
                                Text("Bildirimlere İzin Ver", color = Color.White)
                            }
                        }
                    }
                    VerticalSpacer(8)
                }
                VerticalSpacer(16)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { page = 4 }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(20.dp)) {
                        Text("Geri")
                    }
                    Button(
                        onClick = {
                            onComplete(
                                selectedNeed.id,
                                selectedIntensity.id,
                                selectedRoutineNames.toList(),
                                if (taskSelected) taskTitle else null,
                            )
                        },
                        modifier = Modifier.weight(2f).height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CompletedGreen),
                    ) {
                        Text("Başlayalım", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
        }

        VerticalSpacer(24)
    }
}

// ── Need Card ─────────────────────────────────────────────────────────────────

@Composable
private fun NeedCard(need: NeedOption, selected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) CandyPrimary else Color.Transparent,
        animationSpec = tween(200), label = "border",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) CandyPrimaryLight else MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(need.gradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(need.id.first().uppercaseChar().toString(), style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = need.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = need.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(CandyPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ── Intensity Card ────────────────────────────────────────────────────────────

@Composable
private fun IntensityCard(intensity: IntensityOption, selected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) CandyPrimary else Color.Transparent,
        animationSpec = tween(200), label = "border",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) CandyPrimaryLight else MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(if (selected) CandyPrimary else CandyPrimary.copy(0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (intensity.id) { "hafif" -> "H"; "dengeli" -> "D"; else -> "Y" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (selected) Color.White else CandyPrimary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = intensity.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    if (intensity.recommended) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(CompletedGreen.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text("Önerilen", style = MaterialTheme.typography.labelSmall, color = CompletedGreen)
                        }
                    }
                }
                Text(text = intensity.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (selected) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(CandyPrimary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ── Explainer Card ────────────────────────────────────────────────────────────

@Composable
private fun ExplainerCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(0.07f))
            .border(1.dp, color.copy(0.18f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = color)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

// ── Selectable Routine Row ────────────────────────────────────────────────────

@Composable
private fun SelectableRoutineRow(
    name: String,
    selected: Boolean,
    onToggle: () -> Unit,
    badge: String? = null,
    badgeColor: Color = CandyPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) CandyPrimary.copy(0.08f) else MaterialTheme.colorScheme.surfaceVariant,
            )
            .border(
                1.dp,
                if (selected) CandyPrimary.copy(0.30f) else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = CandyPrimary,
                checkmarkColor = Color.White,
            ),
        )
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (badge != null) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(badgeColor.copy(0.12f)).padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(badge, style = MaterialTheme.typography.labelSmall, color = badgeColor)
            }
        }
    }
}
