package com.benimgunlerim.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
)

sealed class TodayUiEffect {
    data class TaskMovedTomorrow(val message: String) : TodayUiEffect()
    data class TaskDeleted(val taskId: String) : TodayUiEffect()
    data class TaskCompletedUndo(val taskId: String) : TodayUiEffect()
    data class DaySaved(val message: String) : TodayUiEffect()
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
    observeTodaySnapshot: ObserveTodaySnapshotUseCase,
) : ViewModel() {
    private var taskEntitiesById: Map<String, TaskEntity> = emptyMap()
    private var routineEntitiesById: Map<String, RoutineEntity> = emptyMap()
    private val deletedTasksById = mutableMapOf<String, TaskEntity>()
    private val _uiEffects = MutableSharedFlow<TodayUiEffect>(extraBufferCapacity = 16)
    val uiEffects = _uiEffects.asSharedFlow()

    fun today(): LocalDate = dateTimeProvider.today()

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
        currentDateFlow.flatMapLatest { date -> observeTodaySnapshot(date) },
        missedDayFlow,
        minuteTickerFlow,
    ) { snapshot, missedDay, _ ->
        taskEntitiesById = (snapshot.tasks + snapshot.overdueTasks).associateBy { it.id }
        routineEntitiesById = snapshot.routines.associateBy { it.id }
        val summaryTime = snapshot.gameState.dailySummaryTime
        val canClose = runCatching { dateTimeProvider.currentTime() >= java.time.LocalTime.parse(summaryTime) }.getOrDefault(false)
        TodayUiState(
            tasks = snapshot.tasks.map { it.toUiModel() },
            routines = snapshot.routines.map { it.toUiModel() },
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
        if (title.isBlank()) return
        viewModelScope.launch {
            addTaskUseCase(title, date, note, startTime, category, priority, reminderTime)
            analyticsTracker.track(AnalyticsEvent("task_created"))
        }
    }

    fun toggleTask(task: TaskEntity) {
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
        viewModelScope.launch {
            updateTaskUseCase(task, title, note, date, startTime, category, priority, reminderTime)
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
        viewModelScope.launch { moveTaskToDateUseCase(task, date) }
    }

    fun moveTaskToDate(taskId: String, date: LocalDate) {
        val task = taskEntitiesById[taskId] ?: return
        moveTaskToDate(task, date)
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            deleteTaskUseCase(task)
            deletedTasksById[task.id] = task
            _uiEffects.tryEmit(TodayUiEffect.TaskDeleted(task.id))
        }
    }

    fun deleteTask(taskId: String) {
        val task = taskEntitiesById[taskId] ?: return
        deleteTask(task)
    }

    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch { restoreTaskUseCase(task) }
    }

    fun restoreDeletedTask(taskId: String) {
        val deleted = deletedTasksById.remove(taskId) ?: return
        restoreTask(deleted)
    }

    fun undoTaskToggle(taskId: String) {
        viewModelScope.launch { setTaskPendingUseCase(taskId) }
    }

    // ── SubTask actions ─────────────────────────────────────────────────────

    fun subTasksFlow(taskId: String) = observeSubTasksUseCase(taskId)

    fun addSubTask(taskId: String, title: String) {
        viewModelScope.launch { addSubTaskUseCase(taskId, title) }
    }

    fun toggleSubTask(subTask: SubTaskEntity) {
        viewModelScope.launch { toggleSubTaskUseCase(subTask) }
    }

    fun deleteSubTask(subTask: SubTaskEntity) {
        viewModelScope.launch { deleteSubTaskUseCase(subTask) }
    }

    // ── Routine actions ──────────────────────────────────────────────────────

    fun toggleRoutine(routine: RoutineEntity, completedToday: Boolean) {
        toggleRoutine(routine.id, completedToday)
    }

    fun toggleRoutine(routineId: String, completedToday: Boolean) {
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

    fun saveDailySummary(
        note: String,
        mood: Int,
        energy: Int = 3,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
        carriedCount: Int = 0,
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val result = closeDayUseCase(
                mood = mood,
                note = note,
                completionRate = state.progress,
                energy = energy,
                bestMoment = bestMoment,
                challenge = challenge,
                tomorrowIntention = tomorrowIntention,
                carriedCount = carriedCount,
                streak = state.currentStreak,
            )
            analyticsTracker.track(AnalyticsEvent("daily_summary_completed"))
            feedbackManager.celebrationBurst()
            rewardDisplayService.onDailyBonusEarned(
                xp = result.dayCloseReward.xpGranted,
                gold = result.dayCloseReward.goldGranted,
            )
            if (!result.perfectDayReward.alreadyGranted) {
                rewardDisplayService.onDailyBonusEarned(
                    xp = result.perfectDayReward.xpGranted,
                    gold = result.perfectDayReward.goldGranted,
                )
            }
            _uiEffects.tryEmit(TodayUiEffect.DaySaved("day_saved"))
        }
    }

    fun carryTasksToTomorrow() {
        viewModelScope.launch { carryPendingTasksUseCase() }
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
            saveMissedDaySummaryUseCase(date, note, mood, energy, bestMoment, challenge, tomorrowIntention)
            _uiEffects.tryEmit(TodayUiEffect.DaySaved("day_saved"))
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

    private fun RoutineEntity.toUiModel(): TodayRoutineUi = TodayRoutineUi(
        id = id,
        name = name,
        preferredTime = preferredTime,
        color = color,
        targetType = targetType,
        targetValue = targetValue,
        targetUnit = targetUnit,
    )
}
