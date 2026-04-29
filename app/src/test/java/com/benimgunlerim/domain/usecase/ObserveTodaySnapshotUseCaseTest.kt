package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObserveTodaySnapshotUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val routineRepository: RoutineRepository = mockk()
    private val completionLogRepository: CompletionLogRepository = mockk()
    private val dailyStateRepository: DailyStateRepository = mockk()
    private val prefsRepository: UserPreferencesRepository = mockk()
    private val dateTimeProvider: DateTimeProvider = mockk()

    private val fixedDate = LocalDate.of(2025, 1, 15)

    private val useCase = ObserveTodaySnapshotUseCase(
        taskRepository = taskRepository,
        routineRepository = routineRepository,
        completionLogRepository = completionLogRepository,
        dailyStateRepository = dailyStateRepository,
        prefsRepository = prefsRepository,
        dateTimeProvider = dateTimeProvider,
    )

    @Before
    fun setUp() {
        every { taskRepository.observeByDate(fixedDate) } returns flowOf(emptyList())
        every { routineRepository.observeActive() } returns flowOf(emptyList())
        every { completionLogRepository.observeByDate(fixedDate) } returns flowOf(emptyList())
        every { dailyStateRepository.observeToday() } returns flowOf(null)
        every { taskRepository.observeOverdue(any()) } returns flowOf(emptyList())
        every { prefsRepository.preferences } returns flowOf(UserPreferences())
        every { dateTimeProvider.today() } returns fixedDate
    }

    @Test
    fun invoke_withNoTasksOrRoutines_returnsZeroProgress() = runTest {
        val snapshot = useCase(fixedDate).first()

        assertEquals(0f, snapshot.progress, 0.001f)
        assertTrue(snapshot.tasks.isEmpty())
        assertTrue(snapshot.routines.isEmpty())
    }

    @Test
    fun invoke_completedTask_progressIsOne() = runTest {
        val task = TaskEntity(
            id = "t1", title = "Test", note = null,
            plannedDate = fixedDate.toString(), startTime = null, endTime = null,
            category = null, color = null, completionState = "completed",
            completedAt = 1_000L, sourceTemplateId = null,
            createdAt = 1_000L, updatedAt = 1_000L,
        )
        every { taskRepository.observeByDate(fixedDate) } returns flowOf(listOf(task))

        val snapshot = useCase(fixedDate).first()

        assertEquals(1f, snapshot.progress, 0.001f)
    }

    @Test
    fun invoke_passesPrefsToGameState() = runTest {
        val prefs = UserPreferences(totalXp = 500, gold = 100)
        every { prefsRepository.preferences } returns flowOf(prefs)

        val snapshot = useCase(fixedDate).first()

        assertEquals(500, snapshot.gameState.totalXp)
        assertEquals(100, snapshot.gameState.gold)
    }

    @Test
    fun invoke_overdueTasks_appearsInSnapshot() = runTest {
        val overdueTask = TaskEntity(
            id = "ov1", title = "Overdue", note = null,
            plannedDate = fixedDate.minusDays(1).toString(), startTime = null, endTime = null,
            category = null, color = null, completionState = "pending",
            completedAt = null, sourceTemplateId = null,
            createdAt = 1_000L, updatedAt = 1_000L,
        )
        every { taskRepository.observeOverdue(any()) } returns flowOf(listOf(overdueTask))

        val snapshot = useCase(fixedDate).first()

        assertEquals(1, snapshot.overdueTasks.size)
        assertEquals("ov1", snapshot.overdueTasks[0].id)
    }

    @Test
    fun invoke_completedRoutineLog_appearsInCompletedRoutineIds() = runTest {
        val routine = RoutineEntity(
            id = "r1", name = "Egzersiz", description = null,
            targetDays = "WEDNESDAY", preferredTime = null, color = null,
            isArchived = false, createdAt = 1_000L, updatedAt = 1_000L,
        )
        val log = CompletionLogEntity(
            id = "log1",
            entityType = "routine",
            entityId = "r1",
            date = fixedDate.toString(),
            completedAt = 1_000L,
            status = "completed",
            note = null,
            value = null,
        )
        every { routineRepository.observeActive() } returns flowOf(listOf(routine))
        every { completionLogRepository.observeByDate(fixedDate) } returns flowOf(listOf(log))

        val snapshot = useCase(fixedDate).first()

        assertTrue("r1" in snapshot.completedRoutineIds)
    }

    @Test
    fun invoke_todayState_appearsInSnapshot() = runTest {
        val state = DailyStateEntity(
            date = fixedDate.toString(),
            mood = "good",
            energyLevel = 4,
            completionRate = 0.75f,
            note = null,
            reflection = null,
            dailyScore = 80,
            closedAt = 1_000L,
        )
        every { dailyStateRepository.observeToday() } returns flowOf(state)

        val snapshot = useCase(fixedDate).first()

        assertEquals(state, snapshot.todayState)
    }
}
