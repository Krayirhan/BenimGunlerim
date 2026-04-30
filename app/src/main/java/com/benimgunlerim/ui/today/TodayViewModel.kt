package com.benimgunlerim.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.R
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.TickerProvider
import com.benimgunlerim.domain.model.GameEvent
import com.benimgunlerim.domain.service.RewardDisplayService
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.AddSubTaskUseCase
import com.benimgunlerim.domain.usecase.AutoCloseMissedDayUseCase
import com.benimgunlerim.domain.usecase.CarryPendingTasksUseCase
import com.benimgunlerim.domain.usecase.CloseDayUseCase
import com.benimgunlerim.domain.usecase.DeleteTaskUseCase
import com.benimgunlerim.domain.usecase.DeleteSubTaskUseCase
import com.benimgunlerim.domain.usecase.MoveTaskToDateUseCase
import com.benimgunlerim.domain.usecase.ObserveDailyStateUseCase
import com.benimgunlerim.domain.usecase.ObserveSubTasksUseCase
import com.benimgunlerim.domain.usecase.ObserveTodaySnapshotUseCase
import com.benimgunlerim.domain.usecase.TodaySnapshot
import com.benimgunlerim.domain.usecase.RestoreTaskUseCase
import com.benimgunlerim.domain.usecase.SaveMissedDaySummaryUseCase
import com.benimgunlerim.domain.usecase.SetTaskPendingUseCase
import com.benimgunlerim.domain.usecase.ToggleSubTaskUseCase
import com.benimgunlerim.domain.usecase.ToggleRoutineUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineProgressUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskTitleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayUiState(
    val tasks: List<TodayTaskUi> = emptyList(),
    val routines: List<TodayRoutineUi> = emptyList(),
    val completionLogs: List<CompletionLogEntity> = emptyList(),
    val completedRoutineIds: Set<String> = emptySet(),
    val progress: Float = 0f,
    val currentStreak: Int = 0,
    val gameState: UserPreferences = UserPreferences(),
    val todayState: DailyStateEntity? = null,
    val overdueTasks: List<TodayTaskUi> = emptyList(),
    val isLoading: Boolean = true,
    val missedDay: LocalDate? = null,
    val canCloseDay: Boolean = false,
    val dailySummaryTime: String = "21:00",
    /** Room/Flow hata verdiğinde — kullanıcı Tekrar dene ile yeniden abone olur. */
    val snapshotLoadError: Boolean = false,
)

sealed class TodayUiEffect {
    data class TaskMovedTomorrow(val message: String) : TodayUiEffect()
    data class OverdueTasksMoved(val count: Int) : TodayUiEffect()
    data class TaskDeleted(val taskId: String) : TodayUiEffect()
    data class TaskCompletedUndo(val taskId: String) : TodayUiEffect()
    data class DaySaved(val message: String) : TodayUiEffect()
    /** Tek seferlik snackbar — [messageRes] string kaynağı */
    data class ActionFailed(val messageRes: Int) : TodayUiEffect()
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val achievementTracker: AchievementTracker,
    private val feedbackManager: FeedbackManager,
    private val rewardDisplayService: RewardDisplayService,
    private val dateTimeProvider: DateTimeProvider,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskTitleUseCase: UpdateTaskTitleUseCase,
    private val moveTaskToDateUseCase: MoveTaskToDateUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase,
    private val setTaskPendingUseCase: SetTaskPendingUseCase,
    private val observeSubTasksUseCase: ObserveSubTasksUseCase,
    private val addSubTaskUseCase: AddSubTaskUseCase,
    private val toggleSubTaskUseCase: ToggleSubTaskUseCase,
    private val deleteSubTaskUseCase: DeleteSubTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val toggleRoutineUseCase: ToggleRoutineUseCase,
    private val updateRoutineProgressUseCase: UpdateRoutineProgressUseCase,
    private val closeDayUseCase: CloseDayUseCase,
    private val carryPendingTasksUseCase: CarryPendingTasksUseCase,
    private val observeDailyStateUseCase: ObserveDailyStateUseCase,
    private val autoCloseMissedDayUseCase: AutoCloseMissedDayUseCase,
    private val saveMissedDaySummaryUseCase: SaveMissedDaySummaryUseCase,
    private val tickerProvider: TickerProvider,
    private val observeTodaySnapshot: ObserveTodaySnapshotUseCase,
) : ViewModel() {
    private var taskEntitiesById: Map<String, TaskEntity> = emptyMap()
    private var routineEntitiesById: Map<String, RoutineEntity> = emptyMap()
    private val deletedTasksById = mutableMapOf<String, TaskEntity>()
    private val _uiEffects = MutableSharedFlow<TodayUiEffect>(extraBufferCapacity = 16)
    val uiEffects = _uiEffects.asSharedFlow()

    /** Tekrar dene — snapshot Flow’unu yeniden bağlar (aynı takvim gününde). */
    private val retrySnapshotTrigger = MutableStateFlow(0)

    fun today(): LocalDate = dateTimeProvider.today()

    fun retrySnapshotLoad() {
        retrySnapshotTrigger.value++
    }

    private fun emptyTodaySnapshot(): TodaySnapshot = TodaySnapshot(
        tasks = emptyList(),
        routines = emptyList(),
        completionLogs = emptyList(),
        completedRoutineIds = emptySet(),
        progress = 0f,
        currentStreak = 0,
        gameState = UserPreferences(),
        todayState = null,
        overdueTasks = emptyList(),
    )

    private fun observeTodaySnapshotSafe(date: LocalDate): Flow<Pair<TodaySnapshot, Boolean>> =
        observeTodaySnapshot(date)
            .map { it to false }
            .catch { emit(emptyTodaySnapshot() to true) }

    // Emits current date, advances at midnight.
    private val currentDateFlow = flow {
        while (true) {
            val now = dateTimeProvider.today()
            emit(now)
            val midnight = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            val nowDateTime = Instant.ofEpochMilli(dateTimeProvider.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
            val delayMs = Duration.between(nowDateTime, midnight).toMillis()
            delay(delayMs.coerceAtLeast(0L) + 500L)
        }
    }

    // Fires every minute to re-evaluate canCloseDay.
    private val minuteTickerFlow = tickerProvider.minuteTicker()

    private val missedDayFlow: Flow<LocalDate?> = currentDateFlow.flatMapLatest { today ->
        observeDailyStateUseCase(today.minusDays(1)).map { state ->
            if (state?.closedAt == null) today.minusDays(1) else null
        }
    }

    val gameEvents: Flow<GameEvent> = rewardDisplayService.gameEvents

    init {
        viewModelScope.launch {
            achievementTracker.newUnlock.collect { def ->
                rewardDisplayService.onAchievementUnlocked(def.emoji, def.title)
            }
        }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        combine(currentDateFlow, retrySnapshotTrigger) { date, _ -> date }
            .flatMapLatest { date -> observeTodaySnapshotSafe(date) },
        missedDayFlow,
        minuteTickerFlow,
    ) { snapshotPair, missedDay, _ ->
        val (snapshot, loadErr) = snapshotPair
        taskEntitiesById = (snapshot.tasks + snapshot.overdueTasks).associateBy { it.id }
        routineEntitiesById = snapshot.routines.associateBy { it.id }
        val summaryTime = snapshot.gameState.dailySummaryTime
        val canClose = runCatching { dateTimeProvider.currentTime() >= java.time.LocalTime.parse(summaryTime) }.getOrDefault(false)
        TodayUiState(
            tasks = snapshot.tasks.map { it.toUiModel() },
            routines = snapshot.routines.map { it.toUiModel(snapshot.routineStreaks[it.id] ?: 0) },
            completionLogs = snapshot.completionLogs,
            completedRoutineIds = snapshot.completedRoutineIds,
            progress = snapshot.progress,
            currentStreak = snapshot.currentStreak,
            gameState = snapshot.gameState,
            todayState = snapshot.todayState,
            overdueTasks = snapshot.overdueTasks.map { it.toUiModel() },
            isLoading = false,
            missedDay = missedDay,
            canCloseDay = canClose && snapshot.todayState?.closedAt == null,
            dailySummaryTime = summaryTime,
            snapshotLoadError = loadErr,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    // ── Task actions ─────────────────────────────────────────────────────────

    fun addTask(
        title: String,
        note: String? = null,
        date: LocalDate = dateTimeProvider.today(),
        startTime: String? = null,
        category: String? = null,
        priority: Int = 2,
        reminderTime: String? = null,
    ) {
        if (isTodayClosed()) return
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        viewModelScope.launch {
            runCatching {
                addTaskUseCase(
                    title = cleanTitle,
                    date = date,
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                    startTime = startTime?.trim()?.takeIf { it.isNotBlank() },
                    category = category?.trim()?.takeIf { it.isNotBlank() },
                    priority = priority,
                    reminderTime = reminderTime?.trim()?.takeIf { it.isNotBlank() },
                )
            }.onSuccess {
                analyticsTracker.track(AnalyticsEvent("task_created"))
            }.onFailure {
                _uiEffects.tryEmit(TodayUiEffect.ActionFailed(R.string.today_error_generic))
            }
        }
    }

    private fun isTodayClosed(): Boolean = uiState.value.todayState?.closedAt != null

    fun toggleTask(task: TaskEntity) {
        if (isTodayClosed()) return
        viewModelScope.launch {
            val wasPending = task.completionState != com.benimgunlerim.domain.model.TaskCompletionState.COMPLETED.value
            analyticsTracker.track(AnalyticsEvent("task_completed"))
            feedbackManager.tapMedium()
            val result = toggleTaskUseCase(task) ?: return@launch
            rewardDisplayService.onTaskCompleted(
                taskId = task.id,
                taskReward = result.taskReward,
                allTasksBonus = result.allTasksBonus,
            )
            if (wasPending) {
                _uiEffects.tryEmit(TodayUiEffect.TaskCompletedUndo(task.id))
            }
        }
    }

    fun toggleTask(taskId: String) {
        val task = taskEntitiesById[taskId] ?: return
        toggleTask(task)
    }

    fun updateTaskTitle(task: TaskEntity, title: String) {
        if (isTodayClosed()) return
        viewModelScope.launch { updateTaskTitleUseCase(task, title) }
    }

    fun updateTaskTitle(taskId: String, title: String) {
        val task = taskEntitiesById[taskId] ?: return
        updateTaskTitle(task, title)
    }

    fun updateTask(
        task: TaskEntity,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) {
        if (isTodayClosed()) return
        viewModelScope.launch {
            updateTaskUseCase(
                task,
                title.trim(),
                note?.trim()?.takeIf { it.isNotBlank() },
                date,
                startTime?.trim()?.takeIf { it.isNotBlank() },
                category?.trim()?.takeIf { it.isNotBlank() },
                priority,
                reminderTime?.trim()?.takeIf { it.isNotBlank() },
            )
        }
    }

    fun updateTask(
        taskId: String,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) {
        val task = taskEntitiesById[taskId] ?: return
        updateTask(task, title, note, date, startTime, category, priority, reminderTime)
    }

    fun moveTaskToTomorrow(task: TaskEntity) {
        if (isTodayClosed()) return
        viewModelScope.launch {
            moveTaskToDateUseCase(task, dateTimeProvider.today().plusDays(1))
            _uiEffects.tryEmit(TodayUiEffect.TaskMovedTomorrow("task_moved_tomorrow"))
        }
    }

    fun moveTaskToTomorrow(taskId: String) {
        val task = taskEntitiesById[taskId] ?: return
        moveTaskToTomorrow(task)
    }

    fun moveTaskToDate(task: TaskEntity, date: LocalDate) {
        if (isTodayClosed()) return
        viewModelScope.launch { moveTaskToDateUseCase(task, date) }
    }

    fun moveTaskToDate(taskId: String, date: LocalDate) {
        val task = taskEntitiesById[taskId] ?: return
        moveTaskToDate(task, date)
    }

    fun deleteTask(task: TaskEntity) {
        if (isTodayClosed()) return
        viewModelScope.launch {
            runCatching { deleteTaskUseCase(task) }
                .onSuccess {
                    deletedTasksById[task.id] = task
                    _uiEffects.tryEmit(TodayUiEffect.TaskDeleted(task.id))
                }
                .onFailure {
                    _uiEffects.tryEmit(TodayUiEffect.ActionFailed(R.string.today_error_delete_task))
                }
        }
    }

    fun deleteTask(taskId: String) {
        val task = taskEntitiesById[taskId] ?: return
        deleteTask(task)
    }

    fun restoreTask(task: TaskEntity) {
        if (isTodayClosed()) return
        viewModelScope.launch { restoreTaskUseCase(task) }
    }

    fun restoreDeletedTask(taskId: String) {
        val deleted = deletedTasksById.remove(taskId) ?: return
        restoreTask(deleted)
    }

    fun undoTaskToggle(taskId: String) {
        if (isTodayClosed()) return
        viewModelScope.launch { setTaskPendingUseCase(taskId) }
    }

    // ── SubTask actions ─────────────────────────────────────────────────────

    fun subTasksFlow(taskId: String) = observeSubTasksUseCase(taskId)

    fun addSubTask(taskId: String, title: String) {
        if (isTodayClosed()) return
        val t = title.trim()
        if (t.isBlank()) return
        viewModelScope.launch { addSubTaskUseCase(taskId, t) }
    }

    fun toggleSubTask(subTask: SubTaskEntity) {
        if (isTodayClosed()) return
        viewModelScope.launch { toggleSubTaskUseCase(subTask) }
    }

    fun deleteSubTask(subTask: SubTaskEntity) {
        if (isTodayClosed()) return
        viewModelScope.launch { deleteSubTaskUseCase(subTask) }
    }

    // ── Routine actions ──────────────────────────────────────────────────────

    fun toggleRoutine(routine: RoutineEntity, completedToday: Boolean) {
        toggleRoutine(routine.id, completedToday)
    }

    fun toggleRoutine(routineId: String, completedToday: Boolean) {
        if (isTodayClosed()) return
        val routine = routineEntitiesById[routineId] ?: return
        viewModelScope.launch {
            val state = uiState.value
            val result = toggleRoutineUseCase(
                routine = routine,
                completedToday = completedToday,
                completedRoutineIds = state.completedRoutineIds,
                allTodayRoutineIds = state.routines.map { it.id },
            ) ?: return@launch

            analyticsTracker.track(AnalyticsEvent("routine_completed"))
            feedbackManager.tapMedium()
            rewardDisplayService.onRoutineCompleted(
                routineId = routine.id,
                grantResult = result.routineReward,
            )
            // all-routines bonus
            if (!result.allRoutinesBonus.alreadyGranted) {
                rewardDisplayService.onDailyBonusEarned(
                    xp = result.allRoutinesBonus.xpGranted,
                    gold = result.allRoutinesBonus.goldGranted,
                )
            }
        }
    }

    fun updateRoutineProgress(routine: RoutineEntity, value: Float, wasCompleted: Boolean) {
        updateRoutineProgress(routine.id, value, wasCompleted)
    }

    fun updateRoutineProgress(routineId: String, value: Float, wasCompleted: Boolean) {
        if (isTodayClosed()) return
        val routine = routineEntitiesById[routineId] ?: return
        viewModelScope.launch {
            val state = uiState.value
            val result = updateRoutineProgressUseCase(
                routine = routine,
                value = value,
                wasCompleted = wasCompleted,
                allTodayRoutineIds = state.routines.map { it.id },
                completedRoutineIds = state.completedRoutineIds,
            ) ?: return@launch

            analyticsTracker.track(AnalyticsEvent("routine_completed"))
            feedbackManager.tapMedium()
            rewardDisplayService.onRoutineCompleted(
                routineId = routine.id,
                grantResult = result.routineReward,
            )
            // all-routines bonus
            if (!result.allRoutinesBonus.alreadyGranted) {
                rewardDisplayService.onDailyBonusEarned(
                    xp = result.allRoutinesBonus.xpGranted,
                    gold = result.allRoutinesBonus.goldGranted,
                )
            }
        }
    }

    // ── Day close ────────────────────────────────────────────────────────────

    /**
     * Optionally moves overdue pending tasks to tomorrow first, then persists the daily summary with
     * [carriedTaskCount]. Order is fixed so the summary row always matches what was carried.
     */
    fun saveDailySummaryWithOptionalCarry(
        note: String,
        mood: Int,
        energy: Int = 3,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
        carryOverdueToTomorrow: Boolean,
    ) {
        viewModelScope.launch {
            runCatching {
                val carried = if (carryOverdueToTomorrow) carryPendingTasksUseCase() else 0
                val state = uiState.value
                val result = closeDayUseCase(
                    date = dateTimeProvider.today(),
                    mood = mood,
                    note = note.trim(),
                    completionRate = state.progress,
                    energy = energy,
                    bestMoment = bestMoment.trim(),
                    challenge = challenge.trim(),
                    tomorrowIntention = tomorrowIntention.trim(),
                    carriedCount = carried,
                    streak = state.currentStreak,
                )
                analyticsTracker.track(AnalyticsEvent("daily_summary_completed"))
                feedbackManager.celebrationBurst()
                if (!result.dayCloseReward.alreadyGranted) {
                    rewardDisplayService.onDailyBonusEarned(
                        xp = result.dayCloseReward.xpGranted,
                        gold = result.dayCloseReward.goldGranted,
                    )
                }
                if (!result.perfectDayReward.alreadyGranted) {
                    rewardDisplayService.onDailyBonusEarned(
                        xp = result.perfectDayReward.xpGranted,
                        gold = result.perfectDayReward.goldGranted,
                    )
                }
                _uiEffects.tryEmit(TodayUiEffect.DaySaved("day_saved"))
            }.onFailure {
                _uiEffects.tryEmit(TodayUiEffect.ActionFailed(R.string.today_error_save_day))
            }
        }
    }

    fun moveAllOverdueTo(date: LocalDate) {
        if (isTodayClosed()) return
        viewModelScope.launch {
            val ids = uiState.value.overdueTasks.map { it.id }
            var moved = 0
            ids.forEach { id ->
                val entity = taskEntitiesById[id] ?: return@forEach
                moveTaskToDateUseCase(entity, date)
                moved++
            }
            if (moved > 0) {
                _uiEffects.tryEmit(TodayUiEffect.OverdueTasksMoved(moved))
            }
        }
    }

    fun autoSaveMissedDay(date: LocalDate) {
        viewModelScope.launch { autoCloseMissedDayUseCase(date) }
    }

    fun saveMissedDaySummary(
        date: LocalDate,
        note: String,
        mood: Int,
        energy: Int,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
    ) {
        viewModelScope.launch {
            runCatching {
                saveMissedDaySummaryUseCase(date, note, mood, energy, bestMoment, challenge, tomorrowIntention)
                _uiEffects.tryEmit(TodayUiEffect.DaySaved("day_saved"))
            }.onFailure {
                _uiEffects.tryEmit(TodayUiEffect.ActionFailed(R.string.today_error_save_day))
            }
        }
    }

    private fun TaskEntity.toUiModel(): TodayTaskUi = TodayTaskUi(
        id = id,
        title = title,
        note = note,
        plannedDate = plannedDate,
        startTime = startTime,
        category = category,
        color = color,
        priority = priority,
        completionState = completionState,
        reminderTime = reminderTime,
    )

    private fun RoutineEntity.toUiModel(currentStreak: Int): TodayRoutineUi = TodayRoutineUi(
        id = id,
        name = name,
        preferredTime = preferredTime,
        color = color,
        targetType = targetType,
        targetValue = targetValue,
        targetUnit = targetUnit,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
    )
}
