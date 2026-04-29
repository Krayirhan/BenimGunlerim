package com.benimgunlerim.ui.plan

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import com.benimgunlerim.domain.usecase.DeleteTaskUseCase
import com.benimgunlerim.domain.usecase.MoveTaskToDateUseCase
import com.benimgunlerim.domain.usecase.ToggleTaskUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val dateTimeProvider: DateTimeProvider = mockk(relaxed = true)
    private val addTaskUseCase: AddTaskUseCase = mockk(relaxed = true)
    private val toggleTaskUseCase: ToggleTaskUseCase = mockk(relaxed = true)
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk(relaxed = true)
    private val moveTaskToDateUseCase: MoveTaskToDateUseCase = mockk(relaxed = true)
    private val fixedDate = LocalDate.of(2025, 6, 9)

    private lateinit var viewModel: PlanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { dateTimeProvider.today() } returns fixedDate
        every { taskRepository.observeRange(any(), any()) } returns flowOf(emptyList())
        every { taskRepository.observeOverdue(any()) } returns flowOf(emptyList())
        coEvery { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) } returns makeTask("added", "x")
        viewModel = PlanViewModel(
            taskRepository = taskRepository,
            dateTimeProvider = dateTimeProvider,
            addTaskUseCase = addTaskUseCase,
            toggleTaskUseCase = toggleTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            moveTaskToDateUseCase = moveTaskToDateUseCase,
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

    // ── toggleTask ────────────────────────────────────────────────────────────

    @Test
    fun toggleTask_callsRepository() = runTest {
        val task = makeTask("t1", "Buy milk")
        viewModel.toggleTask(task)
        advanceUntilIdle()

        coVerify { toggleTaskUseCase(task) }
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    fun deleteTask_callsRepository() = runTest {
        val task = makeTask("t1", "Buy milk")
        viewModel.deleteTask(task)
        advanceUntilIdle()

        coVerify { deleteTaskUseCase(task) }
    }

    // ── moveTaskToDate ────────────────────────────────────────────────────────

    @Test
    fun moveTaskToDate_callsRepository() = runTest {
        val task = makeTask("t1", "Buy milk")
        val newDate = fixedDate.plusDays(1)
        viewModel.moveTaskToDate(task, newDate)
        advanceUntilIdle()

        coVerify { moveTaskToDateUseCase(task, newDate) }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeTask(id: String, title: String) = TaskEntity(
        id = id,
        title = title,
        note = null,
        plannedDate = fixedDate.toString(),
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
