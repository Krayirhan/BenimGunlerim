package com.benimgunlerim.ui.routines

import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.usecase.AddRoutineUseCase
import com.benimgunlerim.domain.usecase.ArchiveRoutineUseCase
import com.benimgunlerim.domain.usecase.SkipRoutineUseCase
import com.benimgunlerim.domain.usecase.UpdateRoutineUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.DayOfWeek
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
class RoutinesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val routineRepository: RoutineRepository = mockk(relaxed = true)
    private val completionLogRepository: CompletionLogRepository = mockk(relaxed = true)
    private val dateTimeProvider: DateTimeProvider = mockk(relaxed = true)
    private val addRoutineUseCase: AddRoutineUseCase = mockk(relaxed = true)
    private val updateRoutineUseCase: UpdateRoutineUseCase = mockk(relaxed = true)
    private val archiveRoutineUseCase: ArchiveRoutineUseCase = mockk(relaxed = true)
    private val skipRoutineUseCase: SkipRoutineUseCase = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private lateinit var viewModel: RoutinesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { dateTimeProvider.today() } returns java.time.LocalDate.of(2025, 6, 9)
        every { routineRepository.observeActive() } returns flowOf(emptyList())
        every { completionLogRepository.observeAll() } returns flowOf(emptyList())
        viewModel = RoutinesViewModel(
            routineRepository = routineRepository,
            completionLogRepository = completionLogRepository,
            dateTimeProvider = dateTimeProvider,
            addRoutineUseCase = addRoutineUseCase,
            updateRoutineUseCase = updateRoutineUseCase,
            archiveRoutineUseCase = archiveRoutineUseCase,
            skipRoutineUseCase = skipRoutineUseCase,
            analyticsTracker = analyticsTracker,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── routines state ────────────────────────────────────────────────────────

    @Test
    fun routines_emitsEmptyList_whenRepositoryIsEmpty() = runTest {
        val job = launch { viewModel.routines.collect {} }
        advanceUntilIdle()

        assertEquals(emptyList<RoutineListItem>(), viewModel.routines.value)
        job.cancel()
    }

    @Test
    fun routines_mapsToRoutineListItems() = runTest {
        val routine = makeRoutine("r1", "Morning Run")
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        every { completionLogRepository.observeAll() } returns flowOf(emptyList())
        val vm = RoutinesViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            addRoutineUseCase,
            updateRoutineUseCase,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            analyticsTracker,
        )

        val job = launch { vm.routines.collect {} }
        advanceUntilIdle()

        assertEquals(1, vm.routines.value.size)
        assertEquals("Morning Run", vm.routines.value.first().routine.name)
        job.cancel()
    }

    @Test
    fun routines_computesCurrentStreak_fromCompletionLogs() = runTest {
        val routine = makeRoutine("r1", "Run")
        val today = java.time.LocalDate.of(2025, 6, 9)
        val log = CompletionLogEntity(
            id = "log1",
            entityType = "routine",
            entityId = "r1",
            date = today.toString(),
            completedAt = System.currentTimeMillis(),
            status = "completed",
            note = null,
        )
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        every { completionLogRepository.observeAll() } returns flowOf(listOf(log))
        val vm = RoutinesViewModel(
            routineRepository,
            completionLogRepository,
            dateTimeProvider,
            addRoutineUseCase,
            updateRoutineUseCase,
            archiveRoutineUseCase,
            skipRoutineUseCase,
            analyticsTracker,
        )

        val job = launch { vm.routines.collect {} }
        advanceUntilIdle()

        assertEquals(1, vm.routines.value.first().currentStreak)
        job.cancel()
    }

    // ── addRoutine ────────────────────────────────────────────────────────────

    @Test
    fun addRoutine_withBlankName_doesNotCallRepository() = runTest {
        viewModel.addRoutine("   ", setOf(DayOfWeek.MONDAY), null)
        advanceUntilIdle()

        coVerify(exactly = 0) { addRoutineUseCase(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun addRoutine_withValidName_callsRepository() = runTest {
        viewModel.addRoutine("Yoga", setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), "07:00")
        advanceUntilIdle()

        coVerify { addRoutineUseCase("Yoga", setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), "07:00", any(), any(), any()) }
    }

    @Test
    fun addRoutine_withValidName_tracksAnalytics() = runTest {
        viewModel.addRoutine("Yoga", setOf(DayOfWeek.MONDAY), "07:00")
        advanceUntilIdle()

        coVerify { analyticsTracker.track(any()) }
    }

    @Test
    fun addRoutine_withNoReminder_tracksHasReminderFalse() = runTest {
        viewModel.addRoutine("Reading", setOf(DayOfWeek.FRIDAY), null)
        advanceUntilIdle()

        coVerify {
            analyticsTracker.track(
                match { event -> event.properties["has_reminder"] == "false" },
            )
        }
    }

    // ── archiveRoutine ────────────────────────────────────────────────────────

    @Test
    fun archiveRoutine_callsRepository() = runTest {
        val routine = makeRoutine("r1", "Run")
        viewModel.archiveRoutine(routine)
        advanceUntilIdle()

        coVerify { archiveRoutineUseCase(routine) }
    }

    // ── skipRoutine ───────────────────────────────────────────────────────────

    @Test
    fun skipRoutine_callsRepository() = runTest {
        val routine = makeRoutine("r1", "Run")
        viewModel.skipRoutine(routine)
        advanceUntilIdle()

        coVerify { skipRoutineUseCase(routine, any()) }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeRoutine(id: String, name: String) = RoutineEntity(
        id = id,
        name = name,
        description = null,
        targetDays = "MONDAY,WEDNESDAY",
        preferredTime = null,
        color = null,
        isArchived = false,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
