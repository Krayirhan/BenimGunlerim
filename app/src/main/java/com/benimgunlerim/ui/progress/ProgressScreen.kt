package com.benimgunlerim.ui.progress

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import com.benimgunlerim.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.LevelSky
import com.benimgunlerim.ui.theme.StreakCoral
import com.benimgunlerim.ui.theme.XpGold
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgressScreen(
    onOpenAchievements: () -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val level = GameEngine.calculateLevel(state.gameState.totalXp)
    val weekDays = state.last30Days.take(7).reversed()
    val bestDay = state.last30Days.maxByOrNull { it.dailyScore }
    val achievementsRatio = if (state.totalAchievements == 0) 0f else state.unlockedAchievements.size.toFloat() / state.totalAchievements

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ProgressBackground()),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            LevelOverviewCard(
                totalXp = state.gameState.totalXp,
                level = level.level,
                levelTitle = level.title,
                currentXp = level.currentXp,
                xpForNext = level.xpForNextLevel,
            )
        }
        item {
            MetricGrid(
                streak = state.currentStreak,
                bestStreak = state.bestStreak,
                weeklyScore = state.weeklyScore,
                averageScore = state.averageScore,
                daysClosed = state.gameState.totalDaysClosed,
                totalTasks = state.gameState.totalTasksCompleted,
                totalRoutines = state.gameState.totalRoutinesCompleted,
                totalPerfectDays = state.gameState.totalPerfectDays,
            )
        }
        item {
            WeeklyChartCard(days = weekDays, average = state.weeklyScore)
        }
        if (state.moodTrend.any { it.second != null }) {
            item {
                MoodEnergyTrendCard(
                    moodTrend = state.moodTrend,
                    energyTrend = state.energyTrend,
                )
            }
        }
        item {
            PerformanceCard(
                routineHitRate = state.routineHitRate,
                taskHitRate = state.taskHitRate,
                taskCount = state.gameState.totalTasksCompleted,
                routineCount = state.gameState.totalRoutinesCompleted,
            )
        }
        item {
            ConsistencyCard(
                streak = state.currentStreak,
                bestStreak = state.bestStreak,
                bestDay = bestDay,
                averageScore = state.averageScore,
            )
        }
        item {
            XpEconomyCard()
        }
        item {
            AchievementsCard(
                unlocked = state.unlockedAchievements,
                total = state.totalAchievements,
                ratio = achievementsRatio,
                onOpenAchievements = onOpenAchievements,
            )
        }
        item {
            RecentDaysCard(days = state.last30Days)
        }
    }
}

@Composable
private fun ProgressBackground(): Brush = com.benimgunlerim.ui.components.ScreenBackground()

@Composable
private fun LevelOverviewCard(
    totalXp: Int,
    level: Int,
    levelTitle: String,
    currentXp: Int,
    xpForNext: Int,
) {
    val progress = if (xpForNext == 0) 0f else currentXp.toFloat() / xpForNext
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(800, easing = FastOutSlowInEasing), label = "level")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF19B97A), Color(0xFF0FA46A), Color(0xFF0E8C5C))))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.progress_level, level), style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                Text(levelTitle, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(.86f))
                Text(stringResource(R.string.progress_total_xp, totalXp), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(.76f))
                ProgressLine(progress = animated, color = Color.White, track = Color.White.copy(.20f))
                Text(stringResource(R.string.progress_xp_next, currentXp, xpForNext), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.78f))
            }
            Ring(progress = animated, label = "%${(animated * 100).toInt()}", color = Color.White, track = Color.White.copy(.22f))
        }
    }
}

@Composable
private fun MetricGrid(
    streak: Int,
    bestStreak: Int,
    weeklyScore: Int,
    averageScore: Int,
    daysClosed: Int,
    totalTasks: Int,
    totalRoutines: Int,
    totalPerfectDays: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(stringResource(R.string.progress_metric_active_streak), stringResource(R.string.progress_metric_streak_days, streak), stringResource(R.string.progress_metric_streak_sub), Icons.Rounded.LocalFireDepartment, CandyPrimary, Modifier.weight(1f))
            MetricCard(stringResource(R.string.progress_metric_best_streak), stringResource(R.string.progress_metric_streak_days, bestStreak), stringResource(R.string.progress_metric_best_streak_sub), Icons.Rounded.LocalFireDepartment, StreakCoral, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                stringResource(R.string.progress_metric_weekly),
                "$weeklyScore",
                stringResource(R.string.progress_metric_weekly_sub),
                Icons.AutoMirrored.Rounded.ShowChart,
                CandySecondary,
                Modifier.weight(1f),
            )
            MetricCard(
                stringResource(R.string.progress_metric_overall),
                "$averageScore",
                stringResource(R.string.progress_metric_overall_sub),
                Icons.AutoMirrored.Rounded.ShowChart,
                LevelSky,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(stringResource(R.string.progress_metric_close), stringResource(R.string.progress_metric_streak_days, daysClosed), stringResource(R.string.progress_metric_close_sub), Icons.Rounded.Flag, XpGold, Modifier.weight(1f))
            MetricCard(stringResource(R.string.progress_metric_perfect), stringResource(R.string.progress_metric_streak_days, totalPerfectDays), stringResource(R.string.progress_metric_perfect_sub), Icons.Rounded.TaskAlt, CompletedGreen, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier) {
    SurfaceCard(modifier = modifier, radius = 12.dp, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun WeeklyChartCard(days: List<ProgressDayUi>, average: Int) {
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.progress_chart_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                    Text(stringResource(R.string.progress_chart_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Pill(stringResource(R.string.progress_chart_avg, average), CandyPrimary)
            }
            if (days.isEmpty()) {
                EmptyPanel(stringResource(R.string.progress_chart_empty))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(168.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    days.forEach { day ->
                        val score = day.dailyScore.coerceIn(0, 100)
                        val height = (104 * (score / 100f)).dp.coerceAtLeast(8.dp)
                        val date = runCatching { LocalDate.parse(day.date) }.getOrNull()
                        val label = date?.dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale("tr"))?.take(2).orEmpty()
                        val color = when {
                            score >= 80 -> CandyPrimary
                            score >= 50 -> XpGold
                            else -> LevelSky
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            Text("$score", style = MaterialTheme.typography.labelSmall, color = color)
                            Box(
                                Modifier
                                    .width(22.dp)
                                    .height(height)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(color),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsistencyCard(streak: Int, bestStreak: Int, bestDay: ProgressDayUi?, averageScore: Int) {
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.progress_consistency_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            InsightRow(
                title = stringResource(R.string.progress_streak_label),
                value = if (streak > 0) stringResource(R.string.progress_metric_streak_days, streak) else stringResource(R.string.progress_streak_not_started),
                helper = if (streak > 0) stringResource(R.string.progress_streak_helper) else stringResource(R.string.progress_streak_helper_start),
                color = CandyPrimary,
            )
            InsightRow(
                title = stringResource(R.string.progress_best_streak_label),
                value = if (bestStreak > 0) stringResource(R.string.progress_metric_streak_days, bestStreak) else stringResource(R.string.progress_no_data),
                helper = stringResource(R.string.progress_best_streak_helper),
                color = StreakCoral,
            )
            InsightRow(
                title = stringResource(R.string.progress_best_day_label),
                value = bestDay?.let { stringResource(R.string.progress_best_day_value, it.dailyScore) } ?: stringResource(R.string.progress_no_data),
                helper = bestDay?.date?.let(::formatDate).orEmpty().ifBlank { stringResource(R.string.progress_best_day_helper) },
                color = CandySecondary,
            )
            InsightRow(
                title = stringResource(R.string.progress_tempo_label),
                value = when {
                    averageScore >= 80 -> stringResource(R.string.progress_tempo_strong)
                    averageScore >= 50 -> stringResource(R.string.progress_tempo_balanced)
                    averageScore > 0 -> stringResource(R.string.progress_tempo_starting)
                    else -> stringResource(R.string.progress_no_data)
                },
                helper = stringResource(R.string.progress_tempo_avg, averageScore),
                color = LevelSky,
            )
        }
    }
}

@Composable
private fun InsightRow(title: String, value: String, helper: String, color: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(helper, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}

@Composable
private fun TaskRoutineBalanceCard(taskCount: Int, routineCount: Int) {
    val total = (taskCount + routineCount).coerceAtLeast(1)
    val taskRatio = taskCount.toFloat() / total
    val routineRatio = routineCount.toFloat() / total
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.progress_balance_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(stringResource(R.string.progress_balance_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BalanceBar(taskRatio = taskRatio, routineRatio = routineRatio)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                BalanceStat(stringResource(R.string.progress_balance_task), taskCount, LevelSky, Modifier.weight(1f))
                BalanceStat(stringResource(R.string.progress_balance_routine), routineCount, CandyPrimary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BalanceBar(taskRatio: Float, routineRatio: Float) {
    Row(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(99.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.weight(taskRatio.coerceAtLeast(.001f)).height(12.dp).background(LevelSky))
        Box(Modifier.weight(routineRatio.coerceAtLeast(.001f)).height(12.dp).background(CandyPrimary))
    }
}

@Composable
private fun BalanceStat(label: String, value: Int, color: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(color.copy(.10f)).border(1.dp, color.copy(.16f), RoundedCornerShape(18.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = color)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MoodEnergyTrendCard(
    moodTrend: List<Pair<String, String?>>,
    energyTrend: List<Pair<String, Int?>>,
) {
    val recent = moodTrend.takeLast(7)
    val recentEnergy = energyTrend.takeLast(7)
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val moodColor: (String?) -> Color = { mood ->
        when (mood) {
            "harika" -> CompletedGreen
            "iyi" -> CandyPrimary
            "normal" -> LevelSky
            "kotu" -> XpGold
            "cok_kotu" -> StreakCoral
            else -> surfaceVariantColor
        }
    }
    val moodLabels = mapOf(
        "harika" to stringResource(R.string.progress_mood_great),
        "iyi" to stringResource(R.string.progress_mood_good),
        "normal" to stringResource(R.string.progress_mood_normal),
        "kotu" to stringResource(R.string.progress_mood_bad),
        "cok_kotu" to stringResource(R.string.progress_mood_very_bad),
    )
    val moodUnknown = stringResource(R.string.progress_mood_unknown)
    val moodLabel: (String?) -> String = { mood -> moodLabels[mood] ?: moodUnknown }
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.progress_mood_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(stringResource(R.string.progress_mood_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (recent.any { it.second != null }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.progress_mood_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        recent.forEach { (date, mood) ->
                            val color = moodColor(mood)
                            val dayLabel = runCatching { LocalDate.parse(date) }.getOrNull()
                                ?.dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale("tr"))?.take(2).orEmpty()
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    Modifier.size(32.dp).clip(CircleShape).background(color.copy(if (mood != null) .85f else .15f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(if (mood != null) moodLabel(mood).take(1) else "-", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = if (mood != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(dayLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            if (recentEnergy.any { it.second != null }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.progress_energy_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        recentEnergy.forEach { (_, energy) ->
                            val level = energy ?: 0
                            val barHeight = if (level > 0) (10 + level * 8).dp else 6.dp
                            val barColor = when {
                                level >= 4 -> CompletedGreen
                                level >= 3 -> CandyPrimary
                                level >= 2 -> XpGold
                                level > 0 -> StreakCoral
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Box(
                                Modifier.width(28.dp).height(barHeight).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(barColor),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceCard(
    routineHitRate: Float,
    taskHitRate: Float,
    taskCount: Int,
    routineCount: Int,
) {
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.progress_perf_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(stringResource(R.string.progress_perf_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.progress_routine_completion, (routineHitRate * 100).toInt()), style = MaterialTheme.typography.labelMedium)
                ProgressLine(progress = routineHitRate, color = CandyPrimary, track = CandyPrimary.copy(.12f))
                Spacer(Modifier.height(2.dp))
                Text(stringResource(R.string.progress_task_completion, (taskHitRate * 100).toInt()), style = MaterialTheme.typography.labelMedium)
                ProgressLine(progress = taskHitRate, color = LevelSky, track = LevelSky.copy(.12f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                BalanceStat(stringResource(R.string.progress_balance_task), taskCount, LevelSky, Modifier.weight(1f))
                BalanceStat(stringResource(R.string.progress_balance_routine), routineCount, CandyPrimary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun XpEconomyCard() {
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.progress_xp_economy_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(stringResource(R.string.progress_xp_economy_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                XpRow(stringResource(R.string.progress_xp_simple_task), "${GameEngine.XP_TASK_LOW} XP", LevelSky)
                XpRow(stringResource(R.string.progress_xp_normal_task), "${GameEngine.XP_TASK_NORMAL} XP", CandySecondary)
                XpRow(stringResource(R.string.progress_xp_important_task), "${GameEngine.XP_TASK_HIGH} XP", StreakCoral)
                XpRow(stringResource(R.string.progress_xp_checkbox_routine), "${GameEngine.XP_ROUTINE_CHECKBOX} XP", CandyPrimary)
                XpRow(stringResource(R.string.progress_xp_targeted_routine), "${GameEngine.XP_ROUTINE_GOAL} XP", CompletedGreen)
                XpRow(stringResource(R.string.progress_xp_close_day), "${GameEngine.XP_DAY_CLOSE} XP", XpGold)
                XpRow(stringResource(R.string.progress_xp_perfect_day), "${GameEngine.XP_PERFECT_DAY} XP + altın", XpGold)
            }
        }
    }
}

@Composable
private fun XpRow(label: String, value: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color.copy(.08f)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}

@Composable
private fun AchievementsCard(
    unlocked: List<AchievementDef>,
    total: Int,
    ratio: Float,
    onOpenAchievements: () -> Unit,
) {
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.progress_achievements_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Text(stringResource(R.string.progress_achievements_subtitle, total, unlocked.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Pill("%${(ratio * 100).toInt()}", XpGold)
            }
            ProgressLine(progress = ratio, color = XpGold, track = XpGold.copy(.12f))
            Text(
                text = "Tüm başarımları gör",
                modifier = Modifier.clickable(onClick = onOpenAchievements),
                style = MaterialTheme.typography.labelLarge,
                color = CandyPrimary,
            )
            if (unlocked.isEmpty()) {
                EmptyPanel(stringResource(R.string.progress_achievements_empty))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(unlocked.size) { index ->
                        AchievementItem(unlocked[index])
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementItem(achievement: AchievementDef) {
    Column(
        Modifier
            .width(150.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(XpGold.copy(.10f))
            .border(1.dp, XpGold.copy(.16f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(achievement.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(achievement.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RecentDaysCard(days: List<ProgressDayUi>) {
    SurfaceCard(radius = 12.dp, padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.progress_recent_days_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            if (days.isEmpty()) {
                EmptyPanel(stringResource(R.string.progress_recent_days_empty))
            } else {
                days.take(7).forEach { day ->
                    DayRow(day)
                }
            }
        }
    }
}

@Composable
private fun DayRow(day: ProgressDayUi) {
    val score = day.dailyScore.coerceIn(0, 100)
    val color = when {
        score >= 80 -> CandyPrimary
        score >= 50 -> XpGold
        else -> LevelSky
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.width(58.dp)) {
            Text(formatDate(day.date), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
            Text(formatDayName(day.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ProgressLine(progress = day.completionRate, color = color, track = color.copy(.12f))
            Text(day.note?.takeIf { it.isNotBlank() } ?: stringResource(R.string.progress_day_note_empty), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("$score", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}

@Composable
private fun Ring(progress: Float, label: String, color: Color, track: Color) {
    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            drawArc(track, -90f, 360f, false, style = stroke)
            drawArc(color, -90f, progress.coerceIn(0f, 1f) * 360f, false, style = stroke)
        }
        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}

@Composable
private fun ProgressLine(progress: Float, color: Color, track: Color) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700, easing = FastOutSlowInEasing), label = "line")
    Box(Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(99.dp)).background(track)) {
        Box(Modifier.fillMaxWidth(animated).height(9.dp).clip(RoundedCornerShape(99.dp)).background(color))
    }
}

@Composable
private fun EmptyPanel(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(com.benimgunlerim.ui.theme.AppTokens.Radius.lg))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(com.benimgunlerim.ui.theme.AppTokens.Radius.lg))
            .padding(com.benimgunlerim.ui.theme.AppTokens.Spacing.cardInner),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun Pill(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(.12f)).border(1.dp, color.copy(.20f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}

@Composable
private fun SurfaceCard(
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp,
    padding: androidx.compose.ui.unit.Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.fillMaxWidth().padding(padding), content = content)
    }
}

private fun formatDate(raw: String): String {
    val date = runCatching { LocalDate.parse(raw) }.getOrNull() ?: return raw
    return date.format(DateTimeFormatter.ofPattern("d MMM", Locale("tr")))
}

private fun formatDayName(raw: String): String {
    val date = runCatching { LocalDate.parse(raw) }.getOrNull() ?: return ""
    return date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("tr"))
}
