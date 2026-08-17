package com.benimgunlerim.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.benimgunlerim.domain.LightDayMode
import com.benimgunlerim.domain.TickerProvider
import com.benimgunlerim.domain.model.GameEvent
import com.benimgunlerim.domain.service.RewardDisplayService
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.AddTasksBatchUseCase
import com.benimgunlerim.domain.usecase.AddSubTaskUseCase
import com.benimgunlerim.domain.usecase.ArchiveRoutineUseCase
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
import com.benimgunlerim.domain.usecase.SkipRoutineUseCase
import com.benimgunlerim.domain.usecase.ToggleSubTaskUseCase
import com.benimgunlerim.domain.usecase.ToggleRoutineUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineProgressUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskTitleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MissedDayData(
    val date: LocalDate,
    val completedCount: Int,
    val totalCount: Int,
    val pendingTaskCount: Int,
)

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
    val missedDayCompletedCount: Int = 0,
    val missedDayTotalCount: Int = 0,
    val missedDayPendingTaskCount: Int = 0,
    val canCloseDay: Boolean = false,
    val dailySummaryTime: String = "21:00",
    /** Room/Flow hata verdiğinde — kullanıcı Tekrar dene ile yeniden abone olur. */
    val snapshotLoadError: Boolean = false,
    val isLightDayMode: Boolean = false,
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
    analyticsTracker: AnalyticsTracker,
    achievementTracker: AchievementTracker,
    feedbackManager: FeedbackManager,
    rewardDisplayService: RewardDisplayService,
    private val dateTimeProvider: DateTimeProvider,
    addTaskUseCase: AddTaskUseCase,
    addTasksBatchUseCase: AddTasksBatchUseCase,
    updateTaskTitleUseCase: UpdateTaskTitleUseCase,
    moveTaskToDateUseCase: MoveTaskToDateUseCase,
    updateTaskUseCase: UpdateTaskUseCase,
    deleteTaskUseCase: DeleteTaskUseCase,
    restoreTaskUseCase: RestoreTaskUseCase,
    setTaskPendingUseCase: SetTaskPendingUseCase,
    observeSubTasksUseCase: ObserveSubTasksUseCase,
    addSubTaskUseCase: AddSubTaskUseCase,
    toggleSubTaskUseCase: ToggleSubTaskUseCase,
    deleteSubTaskUseCase: DeleteSubTaskUseCase,
    toggleTaskUseCase: ToggleTaskUseCase,
    toggleRoutineUseCase: ToggleRoutineUseCase,
    updateRoutineProgressUseCase: UpdateRoutineProgressUseCase,
    updateRoutineUseCase: UpdateRoutineUseCase,
    skipRoutineUseCase: SkipRoutineUseCase,
    archiveRoutineUseCase: ArchiveRoutineUseCase,
    closeDayUseCase: CloseDayUseCase,
    carryPendingTasksUseCase: CarryPendingTasksUseCase,
    private val observeDailyStateUseCase: ObserveDailyStateUseCase,
    autoCloseMissedDayUseCase: AutoCloseMissedDayUseCase,
    saveMissedDaySummaryUseCase: SaveMissedDaySummaryUseCase,
    private val taskRepository: com.benimgunlerim.data.TaskRepository,
    private val routineRepository: com.benimgunlerim.data.RoutineRepository,
    private val completionLogRepository: com.benimgunlerim.data.CompletionLogRepository,
    private val tickerProvider: TickerProvider,
    private val observeTodaySnapshot: ObserveTodaySnapshotUseCase,
) : ViewModel() {
    private var taskEntitiesById: Map<String, TaskEntity> = emptyMap()
    private var routineEntitiesById: Map<String, RoutineEntity> = emptyMap()
    private val _uiEffects = MutableSharedFlow<TodayUiEffect>(extraBufferCapacity = 16)
    val uiEffects = _uiEffects.asSharedFlow()

    private fun isTodayClosed(): Boolean = uiState.value.todayState?.closedAt != null

    private val taskActions = TodayTaskActions(
        scope = viewModelScope,
        dateTimeProvider = dateTimeProvider,
        analyticsTracker = analyticsTracker,
        feedbackManager = feedbackManager,
        achievementTracker = achievementTracker,
        rewardDisplayService = rewardDisplayService,
        addTaskUseCase = addTaskUseCase,
        addTasksBatchUseCase = addTasksBatchUseCase,
        updateTaskTitleUseCase = updateTaskTitleUseCase,
        moveTaskToDateUseCase = moveTaskToDateUseCase,
        updateTaskUseCase = updateTaskUseCase,
        deleteTaskUseCase = deleteTaskUseCase,
        restoreTaskUseCase = restoreTaskUseCase,
        setTaskPendingUseCase = setTaskPendingUseCase,
        observeSubTasksUseCase = observeSubTasksUseCase,
        addSubTaskUseCase = addSubTaskUseCase,
        toggleSubTaskUseCase = toggleSubTaskUseCase,
        deleteSubTaskUseCase = deleteSubTaskUseCase,
        toggleTaskUseCase = toggleTaskUseCase,
        taskEntitiesById = { taskEntitiesById },
        uiStateValue = { uiState.value },
        isTodayClosed = { isTodayClosed() },
        emitEffect = { _uiEffects.tryEmit(it) },
    )

    private val routineActions = TodayRoutineActions(
        scope = viewModelScope,
        dateTimeProvider = dateTimeProvider,
        analyticsTracker = analyticsTracker,
        feedbackManager = feedbackManager,
        achievementTracker = achievementTracker,
        rewardDisplayService = rewardDisplayService,
        toggleRoutineUseCase = toggleRoutineUseCase,
        updateRoutineProgressUseCase = updateRoutineProgressUseCase,
        updateRoutineUseCase = updateRoutineUseCase,
        skipRoutineUseCase = skipRoutineUseCase,
        archiveRoutineUseCase = archiveRoutineUseCase,
        routineEntitiesById = { routineEntitiesById },
        uiStateValue = { uiState.value },
        isTodayClosed = { isTodayClosed() },
    )

    private val dayCloseActions = TodayDayCloseActions(
        scope = viewModelScope,
        dateTimeProvider = dateTimeProvider,
        analyticsTracker = analyticsTracker,
        feedbackManager = feedbackManager,
        rewardDisplayService = rewardDisplayService,
        closeDayUseCase = closeDayUseCase,
        carryPendingTasksUseCase = carryPendingTasksUseCase,
        autoCloseMissedDayUseCase = autoCloseMissedDayUseCase,
        saveMissedDaySummaryUseCase = saveMissedDaySummaryUseCase,
        uiStateValue = { uiState.value },
        emitEffect = { _uiEffects.tryEmit(it) },
    )

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
    private val currentDateFlow = tickerProvider.dateTicker(dateTimeProvider)

    // Fires every minute to re-evaluate canCloseDay.
    private val minuteTickerFlow = tickerProvider.minuteTicker()

    private val missedDayFlow: Flow<MissedDayData?> = buildMissedDayFlow(
        currentDateFlow = currentDateFlow,
        observeDailyStateUseCase = observeDailyStateUseCase,
        taskRepository = taskRepository,
        routineRepository = routineRepository,
        completionLogRepository = completionLogRepository,
    )

    val gameEvents: Flow<GameEvent> = rewardDisplayService.gameEvents

    val uiState: StateFlow<TodayUiState> = combine(
        combine(currentDateFlow, retrySnapshotTrigger) { date, _ -> date }
            .flatMapLatest { date -> observeTodaySnapshotSafe(date) },
        missedDayFlow,
        minuteTickerFlow,
    ) { snapshotPair, missedDayData, _ ->
        val (snapshot, loadErr) = snapshotPair
        taskEntitiesById = (snapshot.tasks + snapshot.overdueTasks).associateBy { it.id }
        routineEntitiesById = snapshot.routines.associateBy { it.id }
        val summaryTime = snapshot.gameState.dailySummaryTime
        val canClose = runCatching { dateTimeProvider.currentTime() >= java.time.LocalTime.parse(summaryTime) }.getOrDefault(false)
        val isLightDay = LightDayMode.isActiveOn(snapshot.gameState.lightDayModeDate, dateTimeProvider.today())
        TodayUiState(
            tasks = snapshot.tasks.map { it.toTodayUiModel() },
            routines = snapshot.routines.map {
                it.toTodayUiModel(
                    currentStreak = snapshot.routineStreaks[it.id] ?: 0,
                    todayLogs = snapshot.completionLogs,
                )
            },
            completionLogs = snapshot.completionLogs,
            completedRoutineIds = snapshot.completedRoutineIds,
            progress = snapshot.progress,
            currentStreak = snapshot.currentStreak,
            gameState = snapshot.gameState,
            todayState = snapshot.todayState,
            overdueTasks = snapshot.overdueTasks.map { it.toTodayUiModel() },
            isLoading = false,
            missedDay = missedDayData?.date,
            missedDayCompletedCount = missedDayData?.completedCount ?: 0,
            missedDayTotalCount = missedDayData?.totalCount ?: 0,
            missedDayPendingTaskCount = missedDayData?.pendingTaskCount ?: 0,
            canCloseDay = canClose && snapshot.todayState?.closedAt == null,
            dailySummaryTime = summaryTime,
            snapshotLoadError = loadErr,
            isLightDayMode = isLightDay,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun toggleLightDayMode(enabled: Boolean) {
        viewModelScope.launch {
            val todayStr = dateTimeProvider.today().toString()
            prefsRepository.setLightDayMode(enabled, todayStr)
        }
    }

    // ── Task + subtask actions ──────────────────────────────────────────────

    fun addTasksFromBrainDump(taskTitles: List<String>) = taskActions.addTasksFromBrainDump(taskTitles)

    fun addTask(
        title: String,
        note: String? = null,
        date: LocalDate = dateTimeProvider.today(),
        startTime: String? = null,
        category: String? = null,
        priority: Int = 2,
        reminderTime: String? = null,
    ) = taskActions.addTask(title, note, date, startTime, category, priority, reminderTime)

    fun toggleTask(task: TaskEntity) = taskActions.toggleTask(task)
    fun toggleTask(taskId: String) = taskActions.toggleTask(taskId)
    fun updateTaskTitle(task: TaskEntity, title: String) = taskActions.updateTaskTitle(task, title)
    fun updateTaskTitle(taskId: String, title: String) = taskActions.updateTaskTitle(taskId, title)

    fun updateTask(
        task: TaskEntity,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) = taskActions.updateTask(task, title, note, date, startTime, category, priority, reminderTime)

    fun updateTask(
        taskId: String,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) = taskActions.updateTask(taskId, title, note, date, startTime, category, priority, reminderTime)

    fun moveTaskToTomorrow(task: TaskEntity) = taskActions.moveTaskToTomorrow(task)
    fun moveTaskToTomorrow(taskId: String) = taskActions.moveTaskToTomorrow(taskId)
    fun moveTaskToDate(task: TaskEntity, date: LocalDate) = taskActions.moveTaskToDate(task, date)
    fun moveTaskToDate(taskId: String, date: LocalDate) = taskActions.moveTaskToDate(taskId, date)
    fun deleteTask(task: TaskEntity) = taskActions.deleteTask(task)
    fun deleteTask(taskId: String) = taskActions.deleteTask(taskId)
    fun restoreTask(task: TaskEntity, subTasks: List<SubTaskEntity> = emptyList()) = taskActions.restoreTask(task, subTasks)
    fun restoreDeletedTask(taskId: String) = taskActions.restoreDeletedTask(taskId)
    fun undoTaskToggle(taskId: String) = taskActions.undoTaskToggle(taskId)
    fun moveAllOverdueTo(date: LocalDate) = taskActions.moveAllOverdueTo(date)

    fun subTasksFlow(taskId: String) = taskActions.subTasksFlow(taskId)
    fun addSubTask(taskId: String, title: String) = taskActions.addSubTask(taskId, title)
    fun toggleSubTask(subTask: SubTaskEntity) = taskActions.toggleSubTask(subTask)
    fun deleteSubTask(subTask: SubTaskEntity) = taskActions.deleteSubTask(subTask)

    // ── Routine actions ──────────────────────────────────────────────────────

    fun toggleRoutine(routine: RoutineEntity, completedToday: Boolean) = routineActions.toggleRoutine(routine, completedToday)
    fun toggleRoutine(routineId: String, completedToday: Boolean) = routineActions.toggleRoutine(routineId, completedToday)
    fun updateRoutineProgress(routine: RoutineEntity, value: Float, wasCompleted: Boolean) = routineActions.updateRoutineProgress(routine, value, wasCompleted)
    fun updateRoutineProgress(routineId: String, value: Float, wasCompleted: Boolean) = routineActions.updateRoutineProgress(routineId, value, wasCompleted)

    fun updateRoutine(
        routineId: String,
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
    ) = routineActions.updateRoutine(routineId, name, targetDays, preferredTime)

    fun skipRoutine(routineId: String) = routineActions.skipRoutine(routineId)
    fun archiveRoutine(routineId: String) = routineActions.archiveRoutine(routineId)

    // ── Day close ────────────────────────────────────────────────────────────

    fun saveDailySummaryWithOptionalCarry(
        note: String,
        mood: Int,
        energy: Int = 3,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
        carryOverdueToTomorrow: Boolean,
    ) = dayCloseActions.saveDailySummaryWithOptionalCarry(note, mood, energy, bestMoment, challenge, tomorrowIntention, carryOverdueToTomorrow)

    fun autoSaveMissedDay(date: LocalDate) = dayCloseActions.autoSaveMissedDay(date)

    fun closeMissedDayWithReview(
        date: LocalDate,
        mood: Int,
        carryOverPendingTasks: Boolean,
    ) = dayCloseActions.closeMissedDayWithReview(date, mood, carryOverPendingTasks)

    fun saveMissedDaySummary(
        date: LocalDate,
        note: String,
        mood: Int,
        energy: Int,
        bestMoment: String = "",
        challenge: String = "",
        tomorrowIntention: String = "",
    ) = dayCloseActions.saveMissedDaySummary(date, note, mood, energy, bestMoment, challenge, tomorrowIntention)
}
