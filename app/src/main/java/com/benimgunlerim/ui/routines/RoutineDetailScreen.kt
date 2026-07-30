@file:Suppress("SpellCheckingInspection")

package com.benimgunlerim.ui.routines

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: String,
    onBack: () -> Unit,
    viewModel: RoutineDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.routine?.name ?: stringResource(R.string.routine_detail_title_fallback), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.routine_detail_back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.archiveRoutine(); onBack() }) {
                        Icon(Icons.Rounded.Archive, contentDescription = stringResource(R.string.routine_detail_archive_cd), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CandyPrimary)
            }
        } else if (state.routine == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.routine_detail_not_found), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            val routine = state.routine!!
            val goalTypeLabel = when (routine.targetType) {
                "count" -> stringResource(R.string.routine_detail_goal_count)
                "amount" -> stringResource(R.string.routine_detail_goal_amount)
                "duration" -> stringResource(R.string.routine_detail_goal_duration)
                "limit" -> stringResource(R.string.routine_detail_goal_limit)
                "negative" -> stringResource(R.string.routine_detail_goal_negative)
                else -> stringResource(R.string.routine_detail_goal_checkbox)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Hero card
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(CandyPrimary.copy(.12f), CandyPrimary.copy(.05f)),
                                ),
                            )
                            .border(1.dp, CandyPrimary.copy(.18f), RoundedCornerShape(28.dp))
                            .padding(20.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                routine.name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailPill(goalTypeLabel, CandyPrimary)
                                if (routine.targetValue != null) {
                                    val unitDisplay = routine.targetUnit ?: ""
                                    val targetText = stringResource(R.string.routine_detail_target_prefix) +
                                        " ${routine.targetValue} $unitDisplay".trim()
                                    DetailPill(targetText, LevelSky)
                                }
                                if (!routine.preferredTime.isNullOrBlank()) {
                                    DetailPill(routine.preferredTime!!, CandySecondary)
                                }
                            }
                        }
                    }
                }

                // Stats row
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(
                            label = stringResource(R.string.routine_detail_stat_streak),
                            value = stringResource(R.string.routine_detail_stat_days, state.currentStreak),
                            color = CandyPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = stringResource(R.string.routine_detail_stat_best),
                            value = stringResource(R.string.routine_detail_stat_days, state.bestStreak),
                            color = LevelSky,
                            modifier = Modifier.weight(1f),
                        )
                        val successRateText = String.format(
                            stringResource(R.string.routine_detail_success_rate_format),
                            state.successRate,
                        )
                        StatCard(
                            label = stringResource(R.string.routine_detail_stat_success),
                            value = successRateText,
                            color = CompletedGreen,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Last 7 days
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(stringResource(R.string.routine_detail_last7), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                        val labels = listOf(
                            stringResource(R.string.routine_detail_day_mon),
                            stringResource(R.string.routine_detail_day_tue),
                            stringResource(R.string.routine_detail_day_wed),
                            stringResource(R.string.routine_detail_day_thu),
                            stringResource(R.string.routine_detail_day_fri),
                            stringResource(R.string.routine_detail_day_sat),
                            stringResource(R.string.routine_detail_day_sun),
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.last7Days.take(7).forEachIndexed { index, completed ->
                                Column(
                                    Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .height(if (completed) 32.dp else 16.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(50))
                                            .background(if (completed) CandyPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                    )
                                    Text(
                                        labels.getOrElse(index) { "" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                // Scheduled days
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(stringResource(R.string.routine_detail_repeat_days), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                        val dayNames = mapOf(
                            java.time.DayOfWeek.MONDAY to stringResource(R.string.routine_detail_day_mon),
                            java.time.DayOfWeek.TUESDAY to stringResource(R.string.routine_detail_day_tue),
                            java.time.DayOfWeek.WEDNESDAY to stringResource(R.string.routine_detail_day_wed),
                            java.time.DayOfWeek.THURSDAY to stringResource(R.string.routine_detail_day_thu),
                            java.time.DayOfWeek.FRIDAY to stringResource(R.string.routine_detail_day_fri),
                            java.time.DayOfWeek.SATURDAY to stringResource(R.string.routine_detail_day_sat),
                            java.time.DayOfWeek.SUNDAY to stringResource(R.string.routine_detail_day_sun),
                        )
                        val scheduledDays = routine.targetDays
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            dayNames.forEach { (dow, label) ->
                                val active = dow in scheduledDays
                                Box(
                                    Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (active) CandyPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, if (active) CandyPrimary else MaterialTheme.colorScheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                // Actions
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.skipToday() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text(stringResource(R.string.routine_detail_skip_today))
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(.10f))
            .border(1.dp, color.copy(.16f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true, name = "Routine Detail Stat")
@Composable
private fun RoutineDetailStatPreview() {
    com.benimgunlerim.ui.theme.BenimGunlerimTheme {
        StatCard(label = "Bu hafta", value = "6/7", color = com.benimgunlerim.ui.theme.CandyPrimary)
    }
}

@Composable
private fun DetailPill(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(.12f))
            .border(1.dp, color.copy(.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}
