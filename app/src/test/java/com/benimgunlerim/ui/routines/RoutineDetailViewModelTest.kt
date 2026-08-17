package com.benimgunlerim.ui.routines

import androidx.lifecycle.SavedStateHandle
import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.usecase.ArchiveRoutineUseCase
import com.benimgunlerim.domain.usecase.SkipRoutineUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val routineRepository: RoutineRepository = mockk(relaxed = true)
    private val completionLogRepository: CompletionLogRepository = mockk(relaxed = true)
    private val dateTimeProvider: DateTimeProvider = mockk(relaxed = true)
    private val archiveRoutineUseCase: ArchiveRoutineUseCase = mockk(relaxed = true)
    private val skipRoutineUseCase: SkipRoutineUseCase = mockk(relaxed = true)
    private val routineId = "r1"
    private val savedStateHandle = SavedStateHandle(mapOf("routineId" to routineId))

    private lateinit var viewModel: RoutineDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { dateTimeProvider.today() } returns LocalDate.of(2025, 6, 9)
        every { routineRepository.observeActive() } returns flowOf(emptyList())
        every { completionLogRepository.observeBetween(any(), any()) } returns flowOf(emptyList())
        every { completionLogRepository.observeAll() } returns flowOf(emptyList())
        viewModel = RoutineDetailViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            savedStateHandle,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun uiState_whenRoutineNotFound_hasNullRoutine() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.routine)
        assertFalse(viewModel.uiState.value.isLoading)
        job.cancel()
    }

    @Test
    fun uiState_whenRoutineFound_populatesRoutine() = runTest {
        val routine = makeRoutine(routineId, "Morning Run")
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        val vm = RoutineDetailViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            savedStateHandle,
        )

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.routine)
        assertEquals("Morning Run", vm.uiState.value.routine!!.name)
        assertFalse(vm.uiState.value.isLoading)
        job.cancel()
    }

    @Test
    fun uiState_computesCurrentStreak_fromCompletionLogs() = runTest {
        val routine = makeRoutine(routineId, "Morning Run")
        val today = LocalDate.of(2025, 6, 9)
        val log = CompletionLogEntity(
            id = "log1",
            entityType = "routine",
            entityId = routineId,
            date = today.toString(),
            completedAt = System.currentTimeMillis(),
            status = "completed",
            note = null,
        )
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        every { completionLogRepository.observeBetween(any(), any()) } returns flowOf(listOf(log))
        every { completionLogRepository.observeAll() } returns flowOf(listOf(log))
        val vm = RoutineDetailViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            savedStateHandle,
        )

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.currentStreak)
        job.cancel()
    }

    @Test
    fun uiState_successRate_isWithinBounds() = runTest {
        val routine = makeRoutine(routineId, "Morning Run")
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        val vm = RoutineDetailViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            savedStateHandle,
        )

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        val rate = vm.uiState.value.successRate
        assert(rate in 0..100) { "Expected successRate 0-100, got $rate" }
        job.cancel()
    }

    @Test
    fun uiState_last7Days_hasSeven() = runTest {
        val routine = makeRoutine(routineId, "Morning Run")
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        val vm = RoutineDetailViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            savedStateHandle,
        )

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(7, vm.uiState.value.last7Days.size)
        job.cancel()
    }

    // ── archiveRoutine ────────────────────────────────────────────────────────

    @Test
    fun archiveRoutine_whenRoutineLoaded_callsRepository() = runTest {
        val routine = makeRoutine(routineId, "Morning Run")
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        val vm = RoutineDetailViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            savedStateHandle,
        )

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.archiveRoutine()
        advanceUntilIdle()

        coVerify { archiveRoutineUseCase(routine) }
        job.cancel()
    }

    @Test
    fun archiveRoutine_whenRoutineNotLoaded_doesNotCallRepository() = runTest {
        viewModel.archiveRoutine()
        advanceUntilIdle()

        coVerify(exactly = 0) { archiveRoutineUseCase(any()) }
    }

    // ── skipToday ─────────────────────────────────────────────────────────────

    @Test
    fun skipToday_whenRoutineLoaded_callsRepository() = runTest {
        val routine = makeRoutine(routineId, "Morning Run")
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        val vm = RoutineDetailViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            savedStateHandle,
        )

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.skipToday()
        advanceUntilIdle()

        coVerify { skipRoutineUseCase(routine, any()) }
        job.cancel()
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeRoutine(id: String, name: String) = RoutineEntity(
        id = id,
        name = name,
        description = null,
        targetDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY",
        preferredTime = null,
        color = null,
        isArchived = false,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
