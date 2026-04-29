package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveProgressSnapshotUseCaseTest {
    private val dailyStateRepository: DailyStateRepository = mockk()
    private val completionLogRepository: CompletionLogRepository = mockk()
    private val prefsRepository: UserPreferencesRepository = mockk()
    private val achievementTracker: AchievementTracker = mockk()
    private val dateTimeProvider: DateTimeProvider = mockk()

    private val fixedDate = LocalDate.of(2026, 4, 29)

    private val useCase = ObserveProgressSnapshotUseCase(
        dailyStateRepository = dailyStateRepository,
        completionLogRepository = completionLogRepository,
        prefsRepository = prefsRepository,
        achievementTracker = achievementTracker,
        dateTimeProvider = dateTimeProvider,
    )

    @Before
    fun setUp() {
        every { dailyStateRepository.observeRecent(any()) } returns flowOf(emptyList())
        every { completionLogRepository.observeAll() } returns flowOf(emptyList())
        every { prefsRepository.preferences } returns flowOf(UserPreferences())
        every { achievementTracker.unlockedAchievements } returns flowOf(emptyList())
        every { dateTimeProvider.today() } returns fixedDate
    }

    @Test
    fun invoke_usesDateTimeProviderForCurrentStreak() = runTest {
        val logs = listOf(
            CompletionLogEntity(
                id = "l1",
                entityType = "task",
                entityId = "t1",
                date = fixedDate.toString(),
                completedAt = 10L,
                status = "completed",
                note = null,
                value = null,
            ),
            CompletionLogEntity(
                id = "l2",
                entityType = "routine",
                entityId = "r1",
                date = fixedDate.minusDays(1).toString(),
                completedAt = 11L,
                status = "completed",
                note = null,
                value = null,
            ),
        )
        every { completionLogRepository.observeAll() } returns flowOf(logs)

        val snapshot = useCase().first()

        assertEquals(2, snapshot.currentStreak)
    }

    @Test
    fun invoke_calculatesHitRatesFromLogs() = runTest {
        val logs = listOf(
            CompletionLogEntity("a", "task", "t1", fixedDate.toString(), 1L, "completed", null, null),
            CompletionLogEntity("b", "task", "t2", fixedDate.toString(), 2L, "skipped", null, null),
            CompletionLogEntity("c", "routine", "r1", fixedDate.toString(), 3L, "completed", null, null),
        )
        every { completionLogRepository.observeAll() } returns flowOf(logs)

        val snapshot = useCase().first()

        assertEquals(0.5f, snapshot.taskHitRate)
        assertEquals(1.0f, snapshot.routineHitRate)
    }
}
