package com.benimgunlerim.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.usecase.ObserveTodaySnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val routines: List<RoutineEntity> = emptyList(),
    val completionLogs: List<CompletionLogEntity> = emptyList(),
    val completedRoutineIds: Set<String> = emptySet(),
    val progress: Float = 0f,
    val currentStreak: Int = 0,
    val gameState: UserPreferences = UserPreferences(),
    val todayState: DailyStateEntity? = null,
    val overdueTasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = true,
)

sealed class GameEvent {
    data class RewardEarned(val xp: Int, val gold: Int) : GameEvent()
    data class LevelUp(val level: Int, val title: String) : GameEvent()
    data class AchievementUnlocked(val emoji: String, val title: String) : GameEvent()
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: BenimGunlerimRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val achievementTracker: AchievementTracker,
    private val feedbackManager: FeedbackManager,
    observeTodaySnapshot: ObserveTodaySnapshotUseCase,
) : ViewModel() {
    // Re-emits current date every midnight so uiState auto-switches to the new day.
    private val currentDateFlow = flow {
        while (true) {
            val now = LocalDate.now()
            emit(now)
            val midnight = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            val delayMs = Duration.between(ZonedDateTime.now(), midnight).toMillis()
            delay(delayMs.coerceAtLeast(0L) + 500L)
        }
    }

    private val _gameEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 5)
    val gameEvents = _gameEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            achievementTracker.newUnlock.collect { def ->
                _gameEvents.tryEmit(GameEvent.AchievementUnlocked(def.emoji, def.title))
            }
        }
    }

    val uiState: StateFlow<TodayUiState> = currentDateFlow
        .flatMapLatest { date -> observeTodaySnapshot(date) }
        .map { snapshot ->
            TodayUiState(
                tasks = snapshot.tasks,
                routines = snapshot.routines,
                completionLogs = snapshot.completionLogs,
                completedRoutineIds = snapshot.completedRoutineIds,
                progress = snapshot.progress,
                currentStreak = snapshot.currentStreak,
                gameState = snapshot.gameState,
                todayState = snapshot.todayState,
                overdueTasks = snapshot.overdueTasks,
                isLoading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun addTask(
        title: String,
        note: String? = null,
        date: LocalDate = LocalDate.now(),
        startTime: String? = null,
        category: String? = null,
        priority: Int = 2,
        reminderTime: String? = null,
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTask(title, date, note, startTime, category, priority, reminderTime)
            analyticsTracker.track(AnalyticsEvent("task_created"))
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
            if (task.completionState != "completed") {
                analyticsTracker.track(AnalyticsEvent("task_completed"))
                feedbackManager.tapMedium()
                val oldXp = uiState.value.gameState.totalXp
                val taskXp = GameEngine.xpForTask(task.priority)
                val granted = prefsRepository.grantRewardOnce(
                    eventKey = "task:${task.id}:${task.plannedDate}",
                    xp = taskXp,
                    gold = GameEngine.GOLD_TASK_COMPLETE,
                    happinessDelta = GameEngine.HAPPINESS_TASK,
                )
                if (granted) {
                    prefsRepository.incrementTasksCompleted()
                    _gameEvents.tryEmit(GameEvent.RewardEarned(taskXp, GameEngine.GOLD_TASK_COMPLETE))
                    checkLevelUp(oldXp, oldXp + taskXp)
                    // Achievement checks
                    val prefs = uiState.value.gameState
                    achievementTracker.checkTaskCount(prefs.totalTasksCompleted + 1)
                    achievementTracker.checkGold(prefs.gold + GameEngine.GOLD_TASK_COMPLETE)
                    achievementTracker.checkHappiness(prefs.happiness + GameEngine.HAPPINESS_TASK)
                }
                checkAllTasksCompleted(
                    toggledTaskId = task.id,
                    baseXp = oldXp + if (granted) taskXp else 0,
                )
            }
        }
    }

    fun updateTaskTitle(task: TaskEntity, title: String) {
        viewModelScope.launch { repository.updateTaskTitle(task, title) }
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
            repository.updateTask(task, title, note, date, startTime, category, priority, reminderTime)
        }
    }

    fun moveTaskToTomorrow(task: TaskEntity) {
        viewModelScope.launch { repository.moveTaskToDate(task, LocalDate.now().plusDays(1)) }
    }

    fun moveTaskToDate(task: TaskEntity, date: LocalDate) {
        viewModelScope.launch { repository.moveTaskToDate(task, date) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch { repository.restoreTask(task) }
    }

    fun undoTaskToggle(taskId: String) {
        viewModelScope.launch { repository.setTaskPending(taskId) }
    }

    // ── SubTask ──────────────────────────────────────────────────────────────

    fun subTasksFlow(taskId: String) = repository.observeSubTasks(taskId)

    fun addSubTask(taskId: String, title: String) {
        viewModelScope.launch { repository.addSubTask(taskId, title) }
    }

    fun toggleSubTask(subTask: com.benimgunlerim.data.local.entity.SubTaskEntity) {
        viewModelScope.launch { repository.toggleSubTask(subTask) }
    }

    fun deleteSubTask(subTask: com.benimgunlerim.data.local.entity.SubTaskEntity) {
        viewModelScope.launch { repository.deleteSubTask(subTask) }
    }

    fun toggleRoutine(routine: RoutineEntity, completedToday: Boolean) {
        viewModelScope.launch {
            repository.toggleRoutine(routine, completedToday = completedToday)
            if (!completedToday) {
                analyticsTracker.track(AnalyticsEvent("routine_completed"))
                feedbackManager.tapMedium()
                val oldXp = uiState.value.gameState.totalXp
                val routineXp = GameEngine.xpForRoutine(routine.targetType ?: "check")
                val granted = prefsRepository.grantRewardOnce(
                    eventKey = "routine:${routine.id}:${LocalDate.now()}",
                    xp = routineXp,
                    gold = GameEngine.GOLD_ROUTINE_COMPLETE,
                    happinessDelta = GameEngine.HAPPINESS_ROUTINE,
                )
                if (granted) {
                    prefsRepository.incrementRoutinesCompleted()
                    _gameEvents.tryEmit(GameEvent.RewardEarned(routineXp, GameEngine.GOLD_ROUTINE_COMPLETE))
                    checkLevelUp(oldXp, oldXp + routineXp)
                    // Achievement checks
                    val prefs = uiState.value.gameState
                    achievementTracker.checkRoutineCount(prefs.totalRoutinesCompleted + 1)
                }
                checkAllRoutinesCompleted(
                    toggledRoutineId = routine.id,
                    baseXp = oldXp + if (granted) routineXp else 0,
                )
            }
        }
    }

    fun updateRoutineProgress(routine: RoutineEntity, value: Float, wasCompleted: Boolean) {
        viewModelScope.launch {
            repository.setRoutineProgress(routine, value)
            val target = routine.targetValue?.toFloat()?.takeIf { it > 0f } ?: 1f
            if (!wasCompleted && value >= target) {
                analyticsTracker.track(AnalyticsEvent("routine_completed"))
                feedbackManager.tapMedium()
                val oldXp = uiState.value.gameState.totalXp
                val routineXp = GameEngine.xpForRoutine(routine.targetType ?: "check")
                val granted = prefsRepository.grantRewardOnce(
                    eventKey = "routine:${routine.id}:${LocalDate.now()}",
                    xp = routineXp,
                    gold = GameEngine.GOLD_ROUTINE_COMPLETE,
                    happinessDelta = GameEngine.HAPPINESS_ROUTINE,
                )
                if (granted) {
                    prefsRepository.incrementRoutinesCompleted()
                    _gameEvents.tryEmit(GameEvent.RewardEarned(routineXp, GameEngine.GOLD_ROUTINE_COMPLETE))
                    checkLevelUp(oldXp, oldXp + routineXp)
                    val prefs = uiState.value.gameState
                    achievementTracker.checkRoutineCount(prefs.totalRoutinesCompleted + 1)
                }
                checkAllRoutinesCompleted(
                    toggledRoutineId = routine.id,
                    baseXp = oldXp + if (granted) routineXp else 0,
                )
            }
        }
    }

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
            val moodLabels = listOf("cok_kotu", "kotu", "normal", "iyi", "harika")
            val moodLabel = moodLabels.getOrElse(mood) { "normal" }
            repository.saveDailySummary(
                mood = moodLabel,
                note = note,
                completionRate = state.progress,
                energyLevel = energy.coerceIn(1, 5),
                bestMoment = bestMoment,
                challenge = challenge,
                tomorrowIntention = tomorrowIntention,
                carriedTaskCount = carriedCount,
            )
            analyticsTracker.track(AnalyticsEvent("daily_summary_completed"))
            feedbackManager.celebrationBurst()
            val oldXp = state.gameState.totalXp
            val dayCloseGranted = prefsRepository.grantRewardOnce(
                eventKey = "dayClose:${LocalDate.now()}",
                xp = GameEngine.XP_DAY_CLOSE,
            )
            if (dayCloseGranted) {
                prefsRepository.incrementDaysClosed()
                _gameEvents.tryEmit(GameEvent.RewardEarned(GameEngine.XP_DAY_CLOSE, 0))
                checkLevelUp(oldXp, oldXp + GameEngine.XP_DAY_CLOSE)
                if (mood == 4) prefsRepository.incrementHappyMoodCount()
                achievementTracker.checkDayClose(state.gameState.totalDaysClosed + 1)
            }
            if (state.progress >= 1f) {
                val xpBeforePerfect = oldXp + if (dayCloseGranted) GameEngine.XP_DAY_CLOSE else 0
                val perfectGranted = prefsRepository.grantRewardOnce(
                    eventKey = "perfectDay:${LocalDate.now()}",
                    xp = GameEngine.XP_ALL_TASKS_BONUS + GameEngine.XP_ALL_ROUTINES_BONUS,
                    gold = GameEngine.GOLD_PERFECT_DAY,
                    happinessDelta = GameEngine.HAPPINESS_STREAK,
                )
                if (perfectGranted) {
                    val perfectXp = GameEngine.XP_ALL_TASKS_BONUS + GameEngine.XP_ALL_ROUTINES_BONUS
                    _gameEvents.tryEmit(GameEvent.RewardEarned(perfectXp, GameEngine.GOLD_PERFECT_DAY))
                    checkLevelUp(xpBeforePerfect, xpBeforePerfect + perfectXp)
                    prefsRepository.incrementPerfectDays()
                    achievementTracker.checkPerfectDay(state.gameState.totalPerfectDays + 1)
                }
            }
            // Achievement checks
            achievementTracker.checkStreak(state.currentStreak)
            if (mood == 4) achievementTracker.checkHappiness(100)
        }
    }

    fun carryTasksToTomorrow() {
        viewModelScope.launch { repository.carryPendingTasksToTomorrow() }
    }

    private fun checkLevelUp(oldXp: Int, newXp: Int) {
        val oldLevel = GameEngine.calculateLevel(oldXp)
        val newLevel = GameEngine.calculateLevel(newXp)
        if (newLevel.level > oldLevel.level) {
            feedbackManager.levelUpVibration()
            _gameEvents.tryEmit(GameEvent.LevelUp(newLevel.level, newLevel.title))
            viewModelScope.launch { achievementTracker.checkLevel(newLevel.level) }
        }
    }

    private suspend fun checkAllTasksCompleted(toggledTaskId: String, baseXp: Int) {
        val state = uiState.value
        val allDone = state.tasks.all {
            it.completionState == "completed" || it.id == toggledTaskId
        } && state.tasks.isNotEmpty()
        if (allDone) {
            val granted = prefsRepository.grantRewardOnce(
                eventKey = "allTasks:${LocalDate.now()}",
                xp = GameEngine.XP_ALL_TASKS_BONUS,
            )
            if (granted) {
                _gameEvents.tryEmit(GameEvent.RewardEarned(GameEngine.XP_ALL_TASKS_BONUS, 0))
                checkLevelUp(baseXp, baseXp + GameEngine.XP_ALL_TASKS_BONUS)
            }
        }
    }

    private suspend fun checkAllRoutinesCompleted(toggledRoutineId: String, baseXp: Int) {
        val state = uiState.value
        val allDone = state.routines.isNotEmpty() &&
            state.routines.all { it.id in state.completedRoutineIds || it.id == toggledRoutineId }
        if (allDone) {
            val granted = prefsRepository.grantRewardOnce(
                eventKey = "allRoutines:${LocalDate.now()}",
                xp = GameEngine.XP_ALL_ROUTINES_BONUS,
            )
            if (granted) {
                _gameEvents.tryEmit(GameEvent.RewardEarned(GameEngine.XP_ALL_ROUTINES_BONUS, 0))
                checkLevelUp(baseXp, baseXp + GameEngine.XP_ALL_ROUTINES_BONUS)
            }
        }
    }
}
