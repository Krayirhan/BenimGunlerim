package com.benimgunlerim.ui.today

import com.benimgunlerim.R
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.TickerProvider
import com.benimgunlerim.domain.model.GameEvent
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.domain.model.TaskCompletionState
import com.benimgunlerim.domain.service.GrantResult
import com.benimgunlerim.domain.service.RewardDisplayService
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.AddTasksBatchUseCase
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
import com.benimgunlerim.domain.usecase.UpdateRoutineUseCase
import com.benimgunlerim.domain.usecase.SkipRoutineUseCase
import com.benimgunlerim.domain.usecase.ArchiveRoutineUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskTitleUseCase
import androidx.compose.ui.graphics.Color
import com.benimgunlerim.ui.theme.Info
import com.benimgunlerim.ui.theme.Streak
import com.benimgunlerim.ui.theme.Success
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.toList
import io.mockk.coVerifyOrder
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
    private val addTasksBatchUseCase: AddTasksBatchUseCase = mockk(relaxed = true)
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
    private val updateRoutineUseCase: UpdateRoutineUseCase = mockk(relaxed = true)
    private val skipRoutineUseCase: SkipRoutineUseCase = mockk(relaxed = true)
    private val archiveRoutineUseCase: ArchiveRoutineUseCase = mockk(relaxed = true)
    private val closeDayUseCase: CloseDayUseCase = mockk(relaxed = true)
    private val carryPendingTasksUseCase: CarryPendingTasksUseCase = mockk(relaxed = true)
    private val observeDailyStateUseCase: ObserveDailyStateUseCase = mockk(relaxed = true)
    private val autoCloseMissedDayUseCase: AutoCloseMissedDayUseCase = mockk(relaxed = true)
    private val saveMissedDaySummaryUseCase: SaveMissedDaySummaryUseCase = mockk(relaxed = true)
    private val taskRepository: com.benimgunlerim.data.TaskRepository = mockk(relaxed = true)
    private val routineRepository: com.benimgunlerim.data.RoutineRepository = mockk(relaxed = true)
    private val completionLogRepository: com.benimgunlerim.data.CompletionLogRepository = mockk(relaxed = true)
    private val tickerProvider: TickerProvider = mockk()
    private val observeTodaySnapshotUseCase: ObserveTodaySnapshotUseCase = mockk()
    private val rewardDisplayService: RewardDisplayService = mockk(relaxed = true)

    private val snapshotFlow = kotlinx.coroutines.flow.MutableStateFlow(emptySnapshot())
    private lateinit var viewModel: TodayViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { dateTimeProvider.today() } returns fixedDate
        every { dateTimeProvider.currentTimeMillis() } returns 1_000L
        every { dateTimeProvider.currentTime() } returns fixedTime
        
        // Setup rewardDisplayService to emit events on demand  
        val gameEventFlow = MutableSharedFlow<GameEvent>(extraBufferCapacity = 5)
        every { rewardDisplayService.gameEvents } returns gameEventFlow
        coEvery { rewardDisplayService.onTaskCompleted(any(), any(), any()) } coAnswers {
            val taskReward = arg<GrantResult>(1)
            if (taskReward is GrantResult.Granted) {
                gameEventFlow.tryEmit(GameEvent.RewardEarned(taskReward.xpGranted, taskReward.goldGranted))
                taskReward.leveledUp?.let {
                    gameEventFlow.tryEmit(GameEvent.LevelUp(it.level, it.titleRes))
                }
            }
        }
        every { tickerProvider.minuteTicker() } returns kotlinx.coroutines.flow.MutableStateFlow(Unit)
        every { tickerProvider.dateTicker(any()) } returns kotlinx.coroutines.flow.MutableStateFlow(fixedDate)
        every { observeTodaySnapshotUseCase(any()) } returns snapshotFlow
        every { observeDailyStateUseCase(any()) } returns flowOf(null)
        every { taskRepository.observeByDate(any()) } returns flowOf(emptyList())
        every { routineRepository.observeActive() } returns flowOf(emptyList())
        every { completionLogRepository.observeByDate(any()) } returns flowOf(emptyList())

        snapshotFlow.value = emptySnapshot()
        viewModel = createViewModel()
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

    // ── addTasksFromBrainDump ────────────────────────────────────────────────

    @Test
    fun addTasksFromBrainDump_delegatesToBatchUseCase() = runTest {
        val titles = listOf("Su iç", "Kitap oku")

        viewModel.addTasksFromBrainDump(titles)
        advanceUntilIdle()

        coVerify { addTasksBatchUseCase(titles = titles, date = fixedDate, priority = 1) }
    }

    @Test
    fun addTasksFromBrainDump_batchFailure_emitsActionFailed() = runTest {
        coEvery { addTasksBatchUseCase(any(), any(), any()) } throws RuntimeException("db error")

        val effects = mutableListOf<TodayUiEffect>()
        val collectJob = launch { viewModel.uiEffects.toList(effects) }

        viewModel.addTasksFromBrainDump(listOf("Su iç"))
        advanceUntilIdle()

        assertTrue(effects.any { it is TodayUiEffect.ActionFailed })
        collectJob.cancel()
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
                leveledUp = GameEngine.LevelInfo(
                    level = 3,
                    titleRes = R.string.game_level_title_3,
                    currentXp = 70,
                    xpForNextLevel = 150,
                    totalXp = 70,
                ),
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
    fun gameEvents_forwardsFromRewardDisplayService() = runTest {
        val gameEventFlow = MutableSharedFlow<GameEvent>(extraBufferCapacity = 5)
        every { rewardDisplayService.gameEvents } returns gameEventFlow

        val testViewModel = createViewModel()
        val events = mutableListOf<GameEvent>()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testViewModel.gameEvents.toList(events)
        }

        gameEventFlow.emit(
            GameEvent.AchievementUnlocked(
                id = "streak_3",
                emoji = "🔥",
                titleRes = R.string.achievement_streak_3_title,
                descriptionRes = R.string.achievement_streak_3_desc,
                xpReward = 30,
            ),
        )
        advanceUntilIdle()

        assertTrue(events.any { it is GameEvent.AchievementUnlocked && (it as GameEvent.AchievementUnlocked).emoji == "🔥" })
        collectJob.cancel()
    }

    @Test
    fun saveDailySummaryWithOptionalCarry_runsCarryBeforeClose_andPassesCount() = runTest {
        coEvery { carryPendingTasksUseCase() } returns 4
        coEvery {
            closeDayUseCase(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns CloseDayUseCase.Result(
            dayCloseReward = GrantResult.Granted(xpGranted = 10, goldGranted = 0),
            perfectDayReward = GrantResult.AlreadyGranted,
        )

        viewModel.saveDailySummaryWithOptionalCarry(
            note = "not",
            mood = 2,
            energy = 3,
            bestMoment = "",
            challenge = "",
            tomorrowIntention = "",
            carryOverdueToTomorrow = true,
        )
        advanceUntilIdle()

        coVerifyOrder {
            carryPendingTasksUseCase()
            closeDayUseCase(
                fixedDate,
                2,
                "not",
                0f,
                3,
                "",
                "",
                "",
                4,
                0,
            )
        }
    }

    @Test
    fun saveDailySummaryWithOptionalCarry_whenRewardsAlreadyGranted_skipsBonusDisplay() = runTest {
        coEvery {
            closeDayUseCase(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns CloseDayUseCase.Result(
            dayCloseReward = GrantResult.AlreadyGranted,
            perfectDayReward = GrantResult.AlreadyGranted,
        )

        viewModel.saveDailySummaryWithOptionalCarry(
            note = "",
            mood = 0,
            carryOverdueToTomorrow = false,
        )
        advanceUntilIdle()

        coVerify(exactly = 0) { rewardDisplayService.onDailyBonusEarned(any(), any()) }
    }

    @Test
    fun retrySnapshotLoad_reconnectsAfterSnapshotError() = runTest {
        var attempt = 0
        every { observeTodaySnapshotUseCase(any()) } answers {
            attempt++
            if (attempt == 1) {
                flow<TodaySnapshot> { throw IllegalStateException("boom") }
            } else {
                flowOf(emptySnapshot())
            }
        }
        val vm = createViewModel()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect()
        }
        vm.uiState.first { it.snapshotLoadError }

        assertTrue(vm.uiState.value.snapshotLoadError)

        vm.retrySnapshotLoad()
        vm.uiState.first { !it.snapshotLoadError }

        assertFalse(vm.uiState.value.snapshotLoadError)
        job.cancel()
    }

    @Test
    fun closedDay_blocksTaskAndSubTaskMutations() = runTest {
        val task = pendingTask("closed-task")
        val routine = routine("closed-routine", targetType = RoutineTargetType.GOAL.value, targetValue = 4)
        val subTask = SubTaskEntity(
            id = "sub-1",
            taskId = task.id,
            title = "Alt görev",
            createdAt = 1_000L,
        )
        snapshotFlow.value = emptySnapshot().copy(
            tasks = listOf(task),
            routines = listOf(routine),
            todayState = closedDailyState(),
        )
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        viewModel.uiState.first { it.todayState?.closedAt != null }

        viewModel.addTask("Kapalı günde görev")
        viewModel.updateTaskTitle(task, "Yeni başlık")
        viewModel.updateTask(task, "Yeni başlık", null, fixedDate, null, null, 2, null)
        viewModel.deleteTask(task)
        viewModel.moveTaskToDate(task, fixedDate.plusDays(1))
        viewModel.addSubTask(task.id, "Alt görev")
        viewModel.toggleSubTask(subTask)
        viewModel.deleteSubTask(subTask)
        viewModel.undoTaskToggle(task.id)
        viewModel.toggleRoutine(routine.id, completedToday = false)
        viewModel.updateRoutineProgress(routine.id, 2f, wasCompleted = false)
        advanceUntilIdle()

        coVerify(exactly = 0) { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateTaskTitleUseCase(any(), any()) }
        coVerify(exactly = 0) { updateTaskUseCase(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { deleteTaskUseCase(any()) }
        coVerify(exactly = 0) { moveTaskToDateUseCase(any(), any()) }
        coVerify(exactly = 0) { addSubTaskUseCase(any(), any()) }
        coVerify(exactly = 0) { toggleSubTaskUseCase(any()) }
        coVerify(exactly = 0) { deleteSubTaskUseCase(any()) }
        coVerify(exactly = 0) { setTaskPendingUseCase(any()) }
        coVerify(exactly = 0) { toggleRoutineUseCase(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateRoutineProgressUseCase(any(), any(), any(), any(), any(), any()) }
        job.cancel()
    }

    @Test
    fun moveAllOverdueTo_emitsMovedCount() = runTest {
        val old1 = pendingTask("old-1").copy(plannedDate = fixedDate.minusDays(2).toString())
        val old2 = pendingTask("old-2").copy(plannedDate = fixedDate.minusDays(1).toString())
        snapshotFlow.value = emptySnapshot().copy(
            overdueTasks = listOf(old1, old2),
        )
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        viewModel.uiState.first { it.overdueTasks.size == 2 }

        val effectDeferred = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffects.first { it is TodayUiEffect.OverdueTasksMoved } as TodayUiEffect.OverdueTasksMoved
        }

        viewModel.moveAllOverdueTo(fixedDate)
        advanceUntilIdle()

        val effect = effectDeferred.await()
        assertEquals(2, effect.count)
        coVerify { moveTaskToDateUseCase(old1, fixedDate) }
        coVerify { moveTaskToDateUseCase(old2, fixedDate) }
        job.cancel()
    }

    @Test
    fun categoryPalette_matchesTurkishKeywords() {
        assertEquals(Info, CategoryPalette.colorFor("İş toplantısı"))
        assertEquals(Success, CategoryPalette.colorFor("Sağlık yürüyüş"))
        assertEquals(Color(0xFF64748B), CategoryPalette.colorFor("Market listesi"))
        assertEquals(Streak, CategoryPalette.colorFor("Çocuk parkı"))
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

    private fun routine(
        id: String,
        targetType: String = RoutineTargetType.CHECK.value,
        targetValue: Int? = null,
    ) = RoutineEntity(
        id = id,
        name = "Test rutini $id",
        description = null,
        targetDays = "1,2,3,4,5,6,7",
        preferredTime = "08:00",
        color = null,
        isArchived = false,
        createdAt = 1_000L,
        updatedAt = 1_000L,
        targetType = targetType,
        targetValue = targetValue,
        targetUnit = "kez",
        bestStreak = 5,
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

    private fun createViewModel() = TodayViewModel(
        prefsRepository,
        analyticsTracker,
        achievementTracker,
        feedbackManager,
        rewardDisplayService,
        dateTimeProvider,
        addTaskUseCase,
        addTasksBatchUseCase,
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
        updateRoutineUseCase,
        skipRoutineUseCase,
        archiveRoutineUseCase,
        closeDayUseCase,
        carryPendingTasksUseCase,
        observeDailyStateUseCase,
        autoCloseMissedDayUseCase,
        saveMissedDaySummaryUseCase,
        taskRepository,
        routineRepository,
        completionLogRepository,
        tickerProvider,
        observeTodaySnapshotUseCase,
    )

    private fun closedDailyState() = DailyStateEntity(
        date = fixedDate.toString(),
        mood = "iyi",
        energyLevel = 3,
        completionRate = 0f,
        note = null,
        reflection = null,
        dailyScore = 0,
        closedAt = 1_000L,
    )
}
