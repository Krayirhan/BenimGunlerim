@file:Suppress("TooManyFunctions")

package com.benimgunlerim.ui.plan

import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.DeleteTaskUseCase
import com.benimgunlerim.domain.usecase.MoveTaskToDateUseCase
import com.benimgunlerim.domain.usecase.ObservePlanSnapshotUseCase
import com.benimgunlerim.domain.usecase.PlanSnapshot
import com.benimgunlerim.domain.usecase.RestoreTaskUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import com.benimgunlerim.domain.usecase.UpdateTaskUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
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
class PlanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dateTimeProvider: DateTimeProvider = mockk(relaxed = true)
    private val observePlanSnapshotUseCase: ObservePlanSnapshotUseCase = mockk(relaxed = true)
    private val addTaskUseCase: AddTaskUseCase = mockk(relaxed = true)
    private val toggleTaskUseCase: ToggleTaskUseCase = mockk(relaxed = true)
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val moveTaskToDateUseCase: MoveTaskToDateUseCase = mockk(relaxed = true)
    private val updateTaskUseCase: UpdateTaskUseCase = mockk(relaxed = true)
    private val restoreTaskUseCase: RestoreTaskUseCase = mockk(relaxed = true)
    private val fixedDate = LocalDate.of(2025, 6, 9)

    private lateinit var viewModel: PlanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { dateTimeProvider.today() } returns fixedDate
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = emptyList(), overdueTasks = emptyList()),
        )
        coEvery { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) } returns makeTask("added", "x")
        viewModel = PlanViewModel(
            dateTimeProvider = dateTimeProvider,
            observePlanSnapshotUseCase = observePlanSnapshotUseCase,
            addTaskUseCase = addTaskUseCase,
            toggleTaskUseCase = toggleTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            moveTaskToDateUseCase = moveTaskToDateUseCase,
            updateTaskUseCase = updateTaskUseCase,
            restoreTaskUseCase = restoreTaskUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── selectDate ────────────────────────────────────────────────────────────

    @Test
    fun selectDate_updatesSelectedDate() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectDate(fixedDate)
        advanceUntilIdle()

        assertEquals(fixedDate, viewModel.uiState.value.selectedDate)
        job.cancel()
    }

    @Test
    fun selectDate_updatesWeekStart() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val monday = LocalDate.of(2025, 6, 9)
        viewModel.selectDate(monday)
        advanceUntilIdle()

        // Monday's week start should be itself
        assertEquals(monday, viewModel.uiState.value.weekStart)
        job.cancel()
    }

    @Test
    fun selectPreviousWeek_movesSelectedDateBackSevenDays() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectPreviousWeek()
        advanceUntilIdle()

        assertEquals(fixedDate.minusWeeks(1), viewModel.uiState.value.selectedDate)
        assertEquals(fixedDate.minusWeeks(1), viewModel.uiState.value.weekStart)
        job.cancel()
    }

    @Test
    fun selectNextWeek_movesSelectedDateForwardSevenDays() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectNextWeek()
        advanceUntilIdle()

        assertEquals(fixedDate.plusWeeks(1), viewModel.uiState.value.selectedDate)
        assertEquals(fixedDate.plusWeeks(1), viewModel.uiState.value.weekStart)
        job.cancel()
    }

    @Test
    fun selectToday_returnsSelectedDateToProviderToday() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectDate(fixedDate.plusDays(10))
        viewModel.selectToday()
        advanceUntilIdle()

        assertEquals(fixedDate, viewModel.uiState.value.selectedDate)
        job.cancel()
    }

    @Test
    fun uiState_groupsWeekTaskCountsByPlannedDate() = runTest {
        val tuesday = fixedDate.plusDays(1)
        val friday = fixedDate.plusDays(4)
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(
                tasksForDay = emptyList(),
                overdueTasks = emptyList(),
                weekTasks = listOf(
                    makeTask("tue-1", "A", tuesday),
                    makeTask("tue-2", "B", tuesday),
                    makeTask("fri-1", "C", friday),
                ),
            ),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.weeklyTaskCounts[tuesday])
        assertEquals(1, viewModel.uiState.value.weeklyTaskCounts[friday])
        job.cancel()
    }

    @Test
    fun uiState_handlesDensePlanSnapshot() = runTest {
        val dayTasks = (1..120).map { index ->
            makeTask("day-$index", "Task $index", fixedDate)
        }
        val overdueTasks = (1..30).map { index ->
            makeTask("overdue-$index", "Overdue $index", fixedDate.minusDays(1))
        }
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(
                tasksForDay = dayTasks,
                overdueTasks = overdueTasks,
                weekTasks = dayTasks + overdueTasks,
            ),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(120, viewModel.uiState.value.tasksForDay.size)
        assertEquals(30, viewModel.uiState.value.overdueTasks.size)
        assertEquals(120, viewModel.uiState.value.weeklyTaskCounts[fixedDate])
        assertEquals(30, viewModel.uiState.value.weeklyTaskCounts[fixedDate.minusDays(1)])
        job.cancel()
    }

    @Test
    fun retrySnapshotLoad_recoversAfterSnapshotError() = runTest {
        var shouldFail = true
        every { observePlanSnapshotUseCase(any(), any()) } answers {
            if (shouldFail) {
                flow { throw IllegalStateException("snapshot failed") }
            } else {
                flowOf(
                    PlanSnapshot(
                        tasksForDay = listOf(makeTask("t1", "Recovered")),
                        overdueTasks = emptyList(),
                    ),
                )
            }
        }

        val errorState = viewModel.uiState.first { it.snapshotLoadError }
        assertTrue(errorState.snapshotLoadError)

        shouldFail = false
        viewModel.retrySnapshotLoad()
        val recoveredState = viewModel.uiState.first { !it.snapshotLoadError && it.tasksForDay.isNotEmpty() }

        assertFalse(recoveredState.snapshotLoadError)
        assertEquals("Recovered", recoveredState.tasksForDay.first().title)
    }

    // ── addTask ───────────────────────────────────────────────────────────────

    @Test
    fun addTask_withBlankTitle_doesNotCallRepository() = runTest {
        viewModel.addTask("  ", fixedDate)
        advanceUntilIdle()

        coVerify(exactly = 0) { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun addTask_withValidTitle_callsRepository() = runTest {
        viewModel.addTask("Buy milk", fixedDate)
        advanceUntilIdle()

        coVerify { addTaskUseCase("Buy milk", fixedDate, null, null, null, 2, null) }
    }

    @Test
    fun addTask_trimsTitle_beforeCallingRepository() = runTest {
        viewModel.addTask("  Buy milk  ", fixedDate)
        advanceUntilIdle()

        coVerify { addTaskUseCase("Buy milk", fixedDate, null, null, null, 2, null) }
    }

    @Test
    fun addTask_withExtendedFields_callsUseCaseWithPlanMetadata() = runTest {
        viewModel.addTask(
            title = " Dentist ",
            date = fixedDate.plusDays(2),
            note = " Bring forms ",
            startTime = "09:30",
            category = " Health ",
            priority = 1,
            reminderTime = "09:30",
        )
        advanceUntilIdle()

        coVerify {
            addTaskUseCase(
                "Dentist",
                fixedDate.plusDays(2),
                "Bring forms",
                "09:30",
                "Health",
                1,
                "09:30",
            )
        }
    }

    @Test
    fun addTask_withInvalidTime_doesNotCallUseCase() = runTest {
        viewModel.addTask("Buy milk", fixedDate, startTime = "99:99")
        advanceUntilIdle()

        coVerify(exactly = 0) { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun addTask_emitsTaskAddedEffect_onSuccess() = runTest {
        val effect = async { viewModel.uiEffects.first() }

        viewModel.addTask("Buy milk", fixedDate)
        advanceUntilIdle()

        assertEquals(PlanUiEffect.TaskAdded, effect.await())
    }

    // ── toggleTask ────────────────────────────────────────────────────────────

    @Test
    fun toggleTask_callsRepository() = runTest {
        val task = makeTask("t1", "Buy milk")
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = listOf(task), overdueTasks = emptyList()),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.toggleTask(task.id)
        advanceUntilIdle()

        coVerify { toggleTaskUseCase(task) }
        job.cancel()
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    fun deleteTask_callsRepository() = runTest {
        val task = makeTask("t1", "Buy milk")
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = listOf(task), overdueTasks = emptyList()),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.deleteTask(task.id)
        advanceUntilIdle()

        coVerify { deleteTaskUseCase(task) }
        job.cancel()
    }

    // ── moveTaskToDate ────────────────────────────────────────────────────────

    @Test
    fun moveTaskToDate_callsRepository() = runTest {
        val task = makeTask("t1", "Buy milk")
        val newDate = fixedDate.plusDays(1)
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = listOf(task), overdueTasks = emptyList()),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.moveTaskToDate(task.id, newDate)
        advanceUntilIdle()

        coVerify { moveTaskToDateUseCase(task, newDate) }
        job.cancel()
    }

    @Test
    fun moveOverdueTasksToDate_movesEveryOverdueTask() = runTest {
        val overdue1 = makeTask("old-1", "Old one", fixedDate.minusDays(2))
        val overdue2 = makeTask("old-2", "Old two", fixedDate.minusDays(1))
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = emptyList(), overdueTasks = listOf(overdue1, overdue2)),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.moveOverdueTasksToDate(fixedDate.plusDays(1))
        advanceUntilIdle()

        coVerify { moveTaskToDateUseCase(overdue1, fixedDate.plusDays(1)) }
        coVerify { moveTaskToDateUseCase(overdue2, fixedDate.plusDays(1)) }
        job.cancel()
    }

    @Test
    fun updateTask_callsUseCaseWithCleanFields() = runTest {
        val task = makeTask("t1", "Buy milk")
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = listOf(task), overdueTasks = emptyList()),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateTask(
            task.id,
            " New title ",
            " Note ",
            fixedDate.plusDays(1),
            "08:45",
            " Work ",
            1,
            "08:45",
        )
        advanceUntilIdle()

        coVerify {
            updateTaskUseCase(
                task,
                "New title",
                "Note",
                fixedDate.plusDays(1),
                "08:45",
                "Work",
                1,
                "08:45",
            )
        }
        job.cancel()
    }

    @Test
    fun updateTask_withInvalidTime_doesNotCallUseCase() = runTest {
        val task = makeTask("t1", "Buy milk")
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = listOf(task), overdueTasks = emptyList()),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateTask(task.id, "New title", null, fixedDate, "25:00", null, 2, null)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateTaskUseCase(any(), any(), any(), any(), any(), any(), any(), any()) }
        job.cancel()
    }

    @Test
    fun restoreDeletedTask_restoresLastDeletedTask() = runTest {
        val task = makeTask("t1", "Buy milk")
        every { observePlanSnapshotUseCase(any(), any()) } returns flowOf(
            PlanSnapshot(tasksForDay = listOf(task), overdueTasks = emptyList()),
        )
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.deleteTask(task.id)
        advanceUntilIdle()
        viewModel.restoreDeletedTask(task.id)
        advanceUntilIdle()

        coVerify { restoreTaskUseCase(task) }
        job.cancel()
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeTask(id: String, title: String, plannedDate: LocalDate = fixedDate) = TaskEntity(
        id = id,
        title = title,
        note = null,
        plannedDate = plannedDate.toString(),
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = "pending",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
