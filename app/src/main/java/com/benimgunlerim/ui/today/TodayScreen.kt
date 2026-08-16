@file:Suppress("SpellCheckingInspection", "LongMethod")

package com.benimgunlerim.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.components.calm.BrainDumpDialog
import com.benimgunlerim.ui.components.calm.ResetDialog
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.AlertBanner
import com.benimgunlerim.ui.components.molecules.AlertBannerSeverity
import com.benimgunlerim.ui.components.molecules.EmptyState
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.AddRoutineSheet
import com.benimgunlerim.ui.components.organisms.AddTaskSheet
import com.benimgunlerim.ui.components.organisms.CloseDayCard
import com.benimgunlerim.ui.components.organisms.HeaderProgressCard
import com.benimgunlerim.ui.components.organisms.RoutineActionsSheet
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft
import com.benimgunlerim.ui.theme.Info
import com.benimgunlerim.ui.theme.InfoSoft
import com.benimgunlerim.ui.theme.Warning
import com.benimgunlerim.ui.theme.WarningSoft
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onOpenRoutineDetail: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val today = viewModel.today()
    val context = LocalContext.current

    var showFabMenu by remember { mutableStateOf(false) }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBrainDumpDialog by remember { mutableStateOf(false) }
    var showCloseSheet by remember { mutableStateOf(false) }
    var showMissedDaySheet by remember { mutableStateOf(false) }
    var dismissContextualReset by remember { mutableStateOf(false) }

    var levelUpEvent by remember { mutableStateOf<com.benimgunlerim.domain.model.GameEvent.LevelUp?>(null) }
    var achievementEvent by remember { mutableStateOf<com.benimgunlerim.domain.model.GameEvent.AchievementUnlocked?>(null) }
    var blockCompletionData by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var miniBannerData by remember { mutableStateOf<Pair<String, String>?>(null) }

    var menuRoutineId by remember { mutableStateOf<String?>(null) }
    var editingRoutine by remember { mutableStateOf<TodayRoutineUi?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val dayIsClosed = state.todayState?.closedAt != null
    val isEmptyToday = state.tasks.isEmpty() && state.routines.isEmpty()
    val totalCount = state.tasks.size + state.routines.size
    val activeRoutineIds = remember(state.routines) { state.routines.mapTo(mutableSetOf()) { it.id } }
    val completedCount = state.tasks.count { it.isCompleted } +
        state.completedRoutineIds.count { it in activeRoutineIds }
    val userLevel = (state.gameState.totalXp / 100) + 1

    val undoLabel = stringResource(R.string.action_undo)
    val taskDeletedMsg = stringResource(R.string.today_task_deleted_undo)

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is TodayUiEffect.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = taskDeletedMsg,
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask(effect.taskId)
                    }
                }
                is TodayUiEffect.TaskCompletedUndo -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "✓ Görev tamamlandı · +10 XP",
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoTaskToggle(effect.taskId)
                    }
                }
                is TodayUiEffect.ActionFailed -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(effect.messageRes),
                        duration = SnackbarDuration.Short,
                    )
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.gameEvents.collect { event ->
            when (event) {
                is com.benimgunlerim.domain.model.GameEvent.LevelUp -> {
                    levelUpEvent = event
                }
                is com.benimgunlerim.domain.model.GameEvent.AchievementUnlocked -> {
                    achievementEvent = event
                }
                is com.benimgunlerim.domain.model.GameEvent.AllTasksCompleted -> {
                    blockCompletionData = Triple(
                        "Bugünün görevleri tamamlandı 🎉",
                        "${event.totalCount} / ${event.totalCount} görev tamamlandı",
                        "+${event.xpBonus} XP Bonusu",
                    )
                }
                is com.benimgunlerim.domain.model.GameEvent.AllRoutinesCompleted -> {
                    blockCompletionData = Triple(
                        "Bugünün rutinleri tamamlandı",
                        "Ritmini korudun. Seri: ${event.streak} gün",
                        "+${event.xpBonus} XP Bonusu",
                    )
                }
                is com.benimgunlerim.domain.model.GameEvent.MiniBanner -> {
                    miniBannerData = event.message to event.icon
                    kotlinx.coroutines.delay(2800)
                    miniBannerData = null
                }
                is com.benimgunlerim.domain.model.GameEvent.RewardEarned -> Unit
            }
        }
    }

    ScreenScaffold(
        modifier = Modifier.testTag(TestTags.TodayRoot),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = if (!dayIsClosed) {
            {
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    containerColor = BrandPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag(TestTags.TodayFab),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.today_add_task_fab_cd),
                    )
                }
            }
        } else {
            null
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            miniBannerData?.let { (msg, icon) ->
                com.benimgunlerim.ui.components.gamification.MiniCelebrationBanner(
                    message = msg,
                    icon = icon,
                    visible = true,
                )
            }
            if (state.missedDay != null) {
                val missed = state.missedDay!!
                val dayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale("tr")) }
                AlertBanner(
                    message = stringResource(R.string.today_missed_day_msg, missed.format(dayFormatter)),
                    severity = AlertBannerSeverity.Warning,
                    title = stringResource(R.string.today_missed_day_title),
                    actionLabel = stringResource(R.string.today_missed_day_action),
                    action = { showMissedDaySheet = true },
                    modifier = Modifier.testTag(TestTags.TodayMissedDayBanner),
                )
            }

            // ── Hafif Gün Modu Banner'ı (Aktifse) ──
            if (state.isLightDayMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BrandPrimarySoft)
                        .border(1.5.dp, BrandPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BrandPrimary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Spa,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.light_day_active_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandPrimary,
                                )
                            }

                            TextButton(
                                onClick = { viewModel.toggleLightDayMode(false) },
                            ) {
                                Text(
                                    text = stringResource(R.string.light_day_disable_btn),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrandPrimary,
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.light_day_active_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = "“" + stringResource(R.string.light_day_quote) + "”",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = BrandPrimary,
                        )
                    }
                }
            } else if (!dismissContextualReset && totalCount >= 4 && completedCount == 0) {
                // ── Bağlamsal Sakinleşme / Reset Öneri Kartı ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BrandPrimarySoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SelfImprovement,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.reset_contextual_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (state.isLightDayMode) {
                                    stringResource(R.string.reset_contextual_light_body)
                                } else {
                                    stringResource(R.string.reset_contextual_body)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Button(
                            onClick = { showResetDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.reset_action_btn),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }

                        IconButton(
                            onClick = { dismissContextualReset = true },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.action_cancel),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            HeaderProgressCard(
                streakCount = state.currentStreak,
                completedTasks = completedCount,
                totalTasks = totalCount,
                level = userLevel,
                xp = state.gameState.totalXp,
                isLightDayMode = state.isLightDayMode,
            )

            if (state.overdueTasks.isNotEmpty()) {
                SectionBlock(
                    title = stringResource(R.string.today_overdue_tasks_title),
                    trailingContent = {
                        TextButton(
                            onClick = { viewModel.moveAllOverdueTo(today) },
                            modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min),
                        ) {
                            Text(stringResource(R.string.today_move_all_overdue_to_today))
                        }
                    },
                ) {
                    TaskListContainer(
                        tasks = state.overdueTasks,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                    )
                }
            }

            if (isEmptyToday) {
                EmptyState(
                    emoji = "✨",
                    title = stringResource(R.string.today_empty_title),
                    description = stringResource(R.string.today_empty_desc),
                    action = {
                        AppButton(
                            text = stringResource(R.string.today_empty_cta),
                            onClick = { showAddTaskSheet = true },
                        )
                    },
                )
            } else {
                if (state.tasks.isNotEmpty()) {
                    SectionBlock(title = stringResource(R.string.today_tasks_title)) {
                        TaskListContainer(
                            tasks = state.tasks,
                            onToggleTask = { viewModel.toggleTask(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                        )
                    }
                }

                if (state.routines.isNotEmpty()) {
                    SectionBlock(
                        title = stringResource(R.string.today_routines_title),
                        trailingContent = {
                            TextButton(
                                onClick = onNavigateToRoutines,
                                modifier = Modifier.heightIn(min = AppTokens.TouchTarget.min),
                            ) {
                                Text(
                                    text = stringResource(R.string.today_routines_view_all),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrandPrimary,
                                )
                            }
                        },
                    ) {
                        RoutineListContainer(
                            routines = state.routines,
                            completedRoutineIds = state.completedRoutineIds,
                            onToggleRoutine = { id, done -> viewModel.toggleRoutine(id, done) },
                            onOpenRoutineMenu = { menuRoutineId = it },
                        )
                    }
                }
            }

            CloseDayCard(
                isDayClosed = dayIsClosed,
                onCloseDayClick = { showCloseSheet = true },
            )
        }
    }

    // ── FAB Hızlı Eylemler Menüsü ──
    if (showFabMenu) {
        ModalBottomSheet(
            onDismissRequest = { showFabMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.quick_actions_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // 1. Görev Ekle
                QuickActionRow(
                    title = "Görev Ekle",
                    subtitle = "Bugün için tek seferlik yapılacak iş",
                    icon = Icons.Rounded.Add,
                    iconTint = BrandPrimary,
                    iconBg = BrandPrimarySoft,
                    onClick = {
                        showFabMenu = false
                        showAddTaskSheet = true
                    },
                )

                // 2. Kafam Dolu (Zihin Boşaltma)
                QuickActionRow(
                    title = stringResource(R.string.braindump_title),
                    subtitle = "Aklındakileri dök ve göreve dönüştür",
                    icon = Icons.Rounded.Psychology,
                    iconTint = Info,
                    iconBg = InfoSoft,
                    onClick = {
                        showFabMenu = false
                        showBrainDumpDialog = true
                    },
                )

                // 3. 1 Dakikalık Reset & Nefes
                QuickActionRow(
                    title = stringResource(R.string.reset_title),
                    subtitle = "Zihnini boşalt, nefes al, toparlan",
                    icon = Icons.Rounded.SelfImprovement,
                    iconTint = BrandPrimary,
                    iconBg = BrandPrimarySoft,
                    onClick = {
                        showFabMenu = false
                        showResetDialog = true
                    },
                )

                // 4. Hafif Gün Modu
                QuickActionRow(
                    title = if (state.isLightDayMode) stringResource(R.string.light_day_disable_btn) else stringResource(R.string.light_day_title),
                    subtitle = if (state.isLightDayMode) "Normal tempoya geri dön" else "Bugünü iptal etmiyoruz, hafifletiyoruz",
                    icon = Icons.Rounded.Spa,
                    badge = if (state.isLightDayMode) "Aktif" else null,
                    iconTint = if (state.isLightDayMode) BrandPrimary else Warning,
                    iconBg = if (state.isLightDayMode) BrandPrimarySoft else WarningSoft,
                    onClick = {
                        showFabMenu = false
                        viewModel.toggleLightDayMode(!state.isLightDayMode)
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // ── 1 Dakikalık Reset Diyaloğu ──
    if (showResetDialog) {
        ResetDialog(
            onDismiss = { showResetDialog = false },
            onEnableLightDay = { viewModel.toggleLightDayMode(true) },
            onPickTask = { /* user continues to task list */ },
        )
    }

    // ── Kafam Dolu (Brain Dump) Diyaloğu ──
    if (showBrainDumpDialog) {
        BrainDumpDialog(
            onDismiss = { showBrainDumpDialog = false },
            onAddTasks = { titles ->
                viewModel.addTasksFromBrainDump(titles)
            },
        )
    }

    if (showAddTaskSheet) {
        AddTaskSheet(
            onDismiss = { showAddTaskSheet = false },
            onSave = { title, date, startTime, category, priority, reminderTime ->
                viewModel.addTask(title, null, today, startTime, category, priority, reminderTime)
            },
            initialDate = today.toString(),
        )
    }

    if (showCloseSheet) {
        ModalBottomSheet(onDismissRequest = { showCloseSheet = false }) {
            CloseDaySheet(
                completedCount = completedCount,
                totalCount = totalCount,
                progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
                overdueCount = state.overdueTasks.size,
                onSave = { mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks ->
                    viewModel.saveDailySummaryWithOptionalCarry(
                        note = note,
                        mood = mood,
                        energy = energy,
                        bestMoment = bestMoment,
                        challenge = challenge,
                        tomorrowIntention = tomorrowIntention,
                        carryOverdueToTomorrow = carryTasks,
                    )
                    showCloseSheet = false
                },
            )
        }
    }

    if (showMissedDaySheet && state.missedDay != null) {
        val missed = state.missedDay!!
        MissedDayReviewSheet(
            date = missed,
            completedCount = state.missedDayCompletedCount,
            totalCount = state.missedDayTotalCount,
            pendingTaskCount = state.missedDayPendingTaskCount,
            onDismiss = { showMissedDaySheet = false },
            onCloseAndStart = { mood, carryOver ->
                viewModel.closeMissedDayWithReview(
                    date = missed,
                    mood = mood,
                    carryOverPendingTasks = carryOver,
                )
                showMissedDaySheet = false
            },
            onArchiveAsIs = {
                viewModel.autoSaveMissedDay(missed)
                showMissedDaySheet = false
            },
        )
    }

    menuRoutineId?.let { routineId ->
        RoutineActionsSheet(
            onDismiss = { menuRoutineId = null },
            onEdit = {
                menuRoutineId = null
                editingRoutine = state.routines.firstOrNull { it.id == routineId }
            },
            onSkip = {
                viewModel.skipRoutine(routineId)
                menuRoutineId = null
            },
            onDelete = {
                viewModel.archiveRoutine(routineId)
                menuRoutineId = null
            },
        )
    }

    editingRoutine?.let { routine ->
        AddRoutineSheet(
            onDismiss = { editingRoutine = null },
            onSave = { title, targetDays, reminderTime, _, _, _ ->
                viewModel.updateRoutine(routine.id, title, targetDays, reminderTime)
            },
            isEditMode = true,
            initialTitle = routine.name,
            initialDays = routine.targetDays,
            initialReminderTime = routine.preferredTime,
        )
    }

    // ── 4. Kademe Büyük Kutlamalar & Modallar ──
    levelUpEvent?.let { ev ->
        com.benimgunlerim.ui.components.gamification.LevelUpDialog(
            level = ev.level,
            title = ev.title,
            xpBonus = ev.xpBonus,
            onDismiss = { levelUpEvent = null },
        )
    }

    achievementEvent?.let { ev ->
        com.benimgunlerim.ui.components.gamification.AchievementDialog(
            emoji = ev.emoji,
            title = ev.title,
            description = ev.description,
            xpReward = ev.xpReward,
            onDismiss = { achievementEvent = null },
        )
    }

    blockCompletionData?.let { (title, subtitle, badge) ->
        com.benimgunlerim.ui.components.gamification.BlockCompletionSheet(
            title = title,
            subtitle = subtitle,
            badgeText = badge,
            isLightDayMode = state.isLightDayMode,
            onCloseDayClick = {
                blockCompletionData = null
                showCloseSheet = true
            },
            onContinueClick = {
                blockCompletionData = null
            },
            onDismiss = {
                blockCompletionData = null
            },
        )
    }
}

@Composable
private fun QuickActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    iconTint: Color = BrandPrimary,
    iconBg: Color = BrandPrimarySoft,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BrandPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimary,
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
