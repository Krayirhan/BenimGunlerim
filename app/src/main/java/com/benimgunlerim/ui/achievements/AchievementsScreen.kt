package com.benimgunlerim.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.ui.components.ScreenHeader
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.XpGold

@Composable
fun AchievementsScreen(
    onBack: () -> Unit = {},
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "← Geri",
                    modifier = Modifier.clickable(onClick = onBack),
                    style = MaterialTheme.typography.labelLarge,
                    color = CandyPrimary,
                )
                ScreenHeader(
                    title = "🏆 Başarımlar",
                    subtitle = "Açılan rozetleri ve kalan hedefleri takip et",
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryPill(
                    modifier = Modifier.weight(1f),
                    title = "Açılan",
                    value = state.unlockedCount.toString(),
                    color = CompletedGreen,
                )
                SummaryPill(
                    modifier = Modifier.weight(1f),
                    title = "Toplam",
                    value = state.totalCount.toString(),
                    color = XpGold,
                )
                SummaryPill(
                    modifier = Modifier.weight(1f),
                    title = "Oran",
                    value = if (state.totalCount == 0) "%0" else "%${(state.unlockedCount * 100 / state.totalCount)}",
                    color = CandyPrimary,
                )
            }
        }

        items(state.achievements) { item ->
            AchievementRow(item)
        }
    }
}

@Composable
private fun SummaryPill(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color,
) {
    Column(
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
    }
}

@Composable
private fun AchievementRow(item: AchievementWithStatus) {
    val accent = if (item.isUnlocked) CompletedGreen else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .background(
                if (item.isUnlocked) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(18.dp),
            )
            .padding(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(item.def.emoji, style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.def.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(item.def.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "+${item.def.xpReward} XP • +${item.def.goldReward} altın",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpGold,
                )
            }
            Text(
                if (item.isUnlocked) "Açıldı" else "Kilitli",
                style = MaterialTheme.typography.labelMedium,
                color = if (item.isUnlocked) CompletedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
