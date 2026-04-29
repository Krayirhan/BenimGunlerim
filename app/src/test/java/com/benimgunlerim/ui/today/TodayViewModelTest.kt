package com.benimgunlerim.ui.today

import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.TickerProvider
import com.benimgunlerim.domain.model.GameEvent
import com.benimgunlerim.domain.model.TaskCompletionState
import com.benimgunlerim.domain.service.GrantResult
import com.benimgunlerim.domain.service.RewardDisplayService
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.AddSubTaskUseCase
import com.benimgunlerim.domain.usecase.AutoCloseMissedDayUseCase
import com.benimgunlerim.domain.usecase.CarryPendingTasksUseCase
import com.benimgunlerim.domain.usecase.CloseDayUseCase
import com.benimgunlerim.domain.usecase.DeleteTaskUseCase
import com.benimgunlerim.domain.usecase.DeleteSubTaskUseCase
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.domain.usecase.MoveTaskToDateUseCase
import com.benimgunlerim.domain.usecase.ObserveDailyStateUseCase
import com.benimgunlerim.domain.usecase.ObserveSubTasksUseCase
import com.benimgunlerim.domain.usecase.ObserveTodaySnapshotUseCase
import com.benimgunlerim.domain.usecase.RestoreTaskUseCase
import com.benimgunlerim.domain.usecase.SaveMissedDaySummaryUseCase
import com.benimgunlerim.domain.usecase.SetTaskPendingUseCase
import com.benimgunlerim.domain.usecase.TodaySnapshot
import com.benimgunlerim.domain.usecase.ToggleSubTaskUseCase
import com.benimgunlerim.domain.usecase.ToggleRoutineUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineProgressUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskTitleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val fixedTime = LocalTime.of(22, 0)

    // ── Mocks ─────────────────────────────────────────────────────────────────

    private val prefsRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val achievementTracker: AchievementTracker = mockk(relaxed = true)
    private val feedbackManager: FeedbackManager = mockk(relaxed = true)
    private val dateTimeProvider: DateTimeProvider = mockk()
    private val addTaskUseCase: AddTaskUseCase = mockk(relaxed = true)
    private val updateTaskTitleUseCase: UpdateTaskTitleUseCase = mockk(relaxed = true)
    private val moveTaskToDateUseCase: MoveTaskToDateUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val restoreTaskUseCase: RestoreTaskUseCase = mockk(relaxed = true)
    private val setTaskPendingUseCase: SetTaskPendingUseCase = mockk(relaxed = true)
    private val observeSubTasksUseCase: ObserveSubTasksUseCase = mockk(relaxed = true)
    private val addSubTaskUseCase: AddSubTaskUseCase = mockk(relaxed = true)
    private val toggleSubTaskUseCase: ToggleSubTaskUseCase = mockk(relaxed = true)
    private val deleteSubTaskUseCase: DeleteSubTaskUseCase = mockk(relaxed = true)
    private val toggleTaskUseCase: ToggleTaskUseCase = mockk(relaxed = true)
    private val toggleRoutineUseCase: ToggleRoutineUseCase = mockk(relaxed = true)
    private val updateRoutineProgressUseCase: UpdateRoutineProgressUseCase = mockk(relaxed = true)
    private val closeDayUseCase: CloseDayUseCase = mockk(relaxed = true)
    private val carryPendingTasksUseCase: CarryPendingTasksUseCase = mockk(relaxed = true)
    private val observeDailyStateUseCase: ObserveDailyStateUseCase = mockk(relaxed = true)
    private val autoCloseMissedDayUseCase: AutoCloseMissedDayUseCase = mockk(relaxed = true)
    private val saveMissedDaySummaryUseCase: SaveMissedDaySummaryUseCase = mockk(relaxed = true)
    private val tickerProvider: TickerProvider = mockk()
    private val observeTodaySnapshotUseCase: ObserveTodaySnapshotUseCase = mockk()
    private val rewardDisplayService: RewardDisplayService = mockk(relaxed = true)

    private lateinit var viewModel: TodayViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { dateTimeProvider.today() } returns fixedDate
        every { dateTimeProvider.currentTimeMillis() } returns 1_000L
        every { dateTimeProvider.currentTime() } returns fixedTime

        every { achievementTracker.newUnlock } returns MutableSharedFlow()
        
        // Setup rewardDisplayService to emit events on demand  
        val gameEventFlow = MutableSharedFlow<GameEvent>(extraBufferCapacity = 5)
        every { rewardDisplayService.gameEvents } returns gameEventFlow
        coEvery { rewardDisplayService.onTaskCompleted(any(), any(), any()) } coAnswers {
            val taskReward = arg<GrantResult>(1)
            if (taskReward is GrantResult.Granted) {
                gameEventFlow.tryEmit(GameEvent.RewardEarned(taskReward.xpGranted, taskReward.goldGranted))
                taskReward.leveledUp?.let {
                    gameEventFlow.tryEmit(GameEvent.LevelUp(it.level, it.title))
                }
            }
        }
        coEvery { rewardDisplayService.onAchievementUnlocked(any(), any()) } coAnswers {
            gameEventFlow.tryEmit(GameEvent.AchievementUnlocked(firstArg(), secondArg()))
        }

        every { tickerProvider.minuteTicker() } returns flowOf(Unit)
        every { observeTodaySnapshotUseCase(any()) } returns flowOf(emptySnapshot())
        every { observeDailyStateUseCase(any()) } returns flowOf(null)

        viewModel = TodayViewModel(
            prefsRepository,
            analyticsTracker,
            achievementTracker,
            feedbackManager,
            rewardDisplayService,
            dateTimeProvider,
            addTaskUseCase,
            updateTaskTitleUseCase,
            moveTaskToDateUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            restoreTaskUseCase,
            setTaskPendingUseCase,
            observeSubTasksUseCase,
            addSubTaskUseCase,
            toggleSubTaskUseCase,
            deleteSubTaskUseCase,
            toggleTaskUseCase,
            toggleRoutineUseCase,
            updateRoutineProgressUseCase,
            closeDayUseCase,
            carryPendingTasksUseCase,
            observeDailyStateUseCase,
            autoCloseMissedDayUseCase,
            saveMissedDaySummaryUseCase,
            tickerProvider,
            observeTodaySnapshotUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── addTask ───────────────────────────────────────────────────────────────

    @Test
    fun addTask_blankTitle_doesNotCallUseCase() = runTest {
        viewModel.addTask("  ")
        advanceUntilIdle()

        coVerify(exactly = 0) { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun addTask_validTitle_callsUseCase() = runTest {
        viewModel.addTask("Alışveriş yap", date = fixedDate)
        advanceUntilIdle()

        coVerify { addTaskUseCase("Alışveriş yap", fixedDate, any(), any(), any(), any(), any()) }
    }

    // ── toggleTask ────────────────────────────────────────────────────────────

    @Test
    fun toggleTask_emitsRewardEarned_whenGranted() = runTest {
        val task = pendingTask("t1")
        coEvery { toggleTaskUseCase(task) } returns ToggleTaskUseCase.Result(
            taskReward = GrantResult.Granted(xpGranted = 10, goldGranted = 5),
            allTasksBonus = GrantResult.AlreadyGranted,
        )

        val events = mutableListOf<GameEvent>()
        val collectJob = launch { viewModel.gameEvents.toList(events) }

        viewModel.toggleTask(task)
        advanceUntilIdle()

        assertTrue(events.any { it is GameEvent.RewardEarned && (it as GameEvent.RewardEarned).xp == 10 })
        collectJob.cancel()
    }

    @Test
    fun toggleTask_emitsLevelUp_whenLeveledUp() = runTest {
        val task = pendingTask("t2")
        coEvery { toggleTaskUseCase(task) } returns ToggleTaskUseCase.Result(
            taskReward = GrantResult.Granted(
                xpGranted = 20,
                goldGranted = 5,
                leveledUp = GameEngine.LevelInfo(level = 3, title = "Kahraman", currentXp = 70, xpForNextLevel = 150, totalXp = 70),
            ),
            allTasksBonus = GrantResult.AlreadyGranted,
        )

        val events = mutableListOf<GameEvent>()
        val collectJob = launch { viewModel.gameEvents.toList(events) }

        viewModel.toggleTask(task)
        advanceUntilIdle()

        assertTrue(events.any { it is GameEvent.LevelUp && (it as GameEvent.LevelUp).level == 3 })
        collectJob.cancel()
    }

    @Test
    fun toggleTask_noEvent_whenAlreadyGranted() = runTest {
        val task = pendingTask("t3")
        coEvery { toggleTaskUseCase(task) } returns ToggleTaskUseCase.Result(
            taskReward = GrantResult.AlreadyGranted,
            allTasksBonus = GrantResult.AlreadyGranted,
        )

        val events = mutableListOf<GameEvent>()
        val collectJob = launch { viewModel.gameEvents.toList(events) }

        viewModel.toggleTask(task)
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        collectJob.cancel()
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    fun deleteTask_callsDeleteUseCase() = runTest {
        val task = pendingTask("t4")

        viewModel.deleteTask(task)
        advanceUntilIdle()

        coVerify { deleteTaskUseCase(task) }
    }

    // ── AchievementUnlocked ───────────────────────────────────────────────────

    @Test
    fun achievementUnlocked_emitsGameEvent() = runTest {
        val achievementFlow = MutableSharedFlow<AchievementDef>(extraBufferCapacity = 5)
        every { achievementTracker.newUnlock } returns achievementFlow

        // Re-create viewModel so it subscribes to the new flow
        val testViewModel = TodayViewModel(
            prefsRepository,
            analyticsTracker,
            achievementTracker,
            feedbackManager,
            rewardDisplayService,
            dateTimeProvider,
            addTaskUseCase,
            updateTaskTitleUseCase,
            moveTaskToDateUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            restoreTaskUseCase,
            setTaskPendingUseCase,
            observeSubTasksUseCase,
            addSubTaskUseCase,
            toggleSubTaskUseCase,
            deleteSubTaskUseCase,
            toggleTaskUseCase,
            toggleRoutineUseCase,
            updateRoutineProgressUseCase,
            closeDayUseCase,
            carryPendingTasksUseCase,
            observeDailyStateUseCase,
            autoCloseMissedDayUseCase,
            saveMissedDaySummaryUseCase,
            tickerProvider,
            observeTodaySnapshotUseCase,
        )
        advanceUntilIdle() // let init block subscribe

        val events = mutableListOf<GameEvent>()
        val collectJob = launch { testViewModel.gameEvents.toList(events) }

        achievementFlow.emit(AchievementDef("streak_3", "🔥", "Ateş Başladı", "3 gün", 30, 15))
        advanceUntilIdle()

        assertTrue(events.any { it is GameEvent.AchievementUnlocked && (it as GameEvent.AchievementUnlocked).emoji == "🔥" })
        collectJob.cancel()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun pendingTask(id: String) = TaskEntity(
        id = id,
        title = "Test görevi $id",
        note = null,
        plannedDate = fixedDate.toString(),
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        priority = 2,
        completionState = TaskCompletionState.PENDING.value,
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    private fun emptySnapshot() = TodaySnapshot(
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
}
