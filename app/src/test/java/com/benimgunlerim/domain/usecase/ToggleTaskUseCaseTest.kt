package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.FixedDateTimeProvider
import com.benimgunlerim.domain.model.TaskCompletionState
import com.benimgunlerim.domain.service.RewardGrantService
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleTaskUseCaseTest {

    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val fixedDtp = FixedDateTimeProvider(fixedDate = fixedDate, fixedMillis = 1_000L)

    private lateinit var taskRepo: TaskRepository
    private lateinit var prefsRepo: UserPreferencesRepository
    private lateinit var scheduler: TaskReminderSchedulerContract
    private lateinit var useCase: ToggleTaskUseCase

    @Before
    fun setUp() {
        taskRepo = mockk(relaxed = true)
        prefsRepo = mockk(relaxed = true)
        scheduler = mockk(relaxed = true)

        coEvery { prefsRepo.grantRewardOnce(any(), any(), any(), any()) } returns true
        every { prefsRepo.preferences } returns flowOf(UserPreferences(totalXp = 0))

        // Default: after completion, only this task exists and is completed
        coEvery { taskRepo.observeByDate(fixedDate) } returns flowOf(listOf(pendingTask()))

        val achievementTracker = AchievementTracker(FakeAchievementDao(), fixedDtp)
        val rewardService = RewardGrantService(prefsRepo, achievementTracker, fixedDtp)
        val txRunner = object : DatabaseTransactionRunner {
            override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
        }
        useCase = ToggleTaskUseCase(taskRepo, prefsRepo, achievementTracker, rewardService, scheduler, fixedDtp, txRunner)
    }

    // ── Completing a pending task ─────────────────────────────────────────────

    @Test
    fun invoke_pendingTask_returnsResult() = runTest {
        val result = useCase(pendingTask())

        assertNotNull(result)
    }

    @Test
    fun invoke_pendingTask_setsCompletedState() = runTest {
        useCase(pendingTask())

        coVerify { taskRepo.setCompletionState(any(), TaskCompletionState.COMPLETED) }
    }

    @Test
    fun invoke_pendingTask_writesCompletionLog() = runTest {
        useCase(pendingTask())

        coVerify { taskRepo.writeCompletionLog(any()) }
    }

    @Test
    fun invoke_pendingTask_cancelsReminder() = runTest {
        useCase(pendingTask())

        coVerify { scheduler.cancel("task-1") }
    }

    @Test
    fun invoke_pendingTask_grantsRewardXp() = runTest {
        val result = useCase(pendingTask())

        assertTrue(result!!.taskReward.xpGranted > 0)
    }

    // ── Un-completing a completed task ───────────────────────────────────────

    @Test
    fun invoke_completedTask_returnsNull() = runTest {
        val result = useCase(completedTask())

        assertNull(result)
    }

    @Test
    fun invoke_completedTask_setsPendingState() = runTest {
        useCase(completedTask())

        coVerify { taskRepo.setCompletionState(any(), TaskCompletionState.PENDING) }
    }

    @Test
    fun invoke_completedTask_deletesCompletionLog() = runTest {
        useCase(completedTask())

        coVerify { taskRepo.deleteCompletionLog(any()) }
    }

    @Test
    fun invoke_completedTask_withReminder_reschedulesReminder() = runTest {
        useCase(completedTask().copy(reminderTime = "09:00"))

        coVerify { scheduler.schedule("task-1", any(), any(), any()) }
    }

    @Test
    fun invoke_completedTask_noReminder_doesNotSchedule() = runTest {
        useCase(completedTask().copy(reminderTime = null))

        verify(exactly = 0) { scheduler.schedule(any(), any(), any(), any()) }
    }

    // ── All-tasks bonus ──────────────────────────────────────────────────────

    @Test
    fun invoke_lastPendingTask_completionTriggersAllTasksBonus() = runTest {
        // After this task is completed, all tasks for the day are done
        coEvery { taskRepo.observeByDate(fixedDate) } returns flowOf(
            listOf(pendingTask()) // Only one task on this day
        )

        val result = useCase(pendingTask())

        coVerify { prefsRepo.grantRewardOnce(match { it.startsWith("allTasks:") }, any(), any(), any()) }
    }

    @Test
    fun invoke_morePendingTasks_noAllTasksBonus() = runTest {
        coEvery { taskRepo.observeByDate(fixedDate) } returns flowOf(
            listOf(
                pendingTask(),
                pendingTask().copy(id = "task-2", completionState = TaskCompletionState.PENDING.value),
            )
        )

        useCase(pendingTask())

        coVerify(exactly = 0) { prefsRepo.grantRewardOnce(match { it.startsWith("allTasks:") }, any(), any(), any()) }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun pendingTask() = TaskEntity(
        id = "task-1",
        title = "Test Task",
        note = null,
        plannedDate = fixedDate.toString(),
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = TaskCompletionState.PENDING.value,
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
        priority = 2,
        reminderTime = null,
    )

    private fun completedTask() = pendingTask().copy(
        completionState = TaskCompletionState.COMPLETED.value,
        completedAt = 1_000L,
    )

    private class FakeAchievementDao : AchievementDao {
        private val store = linkedMapOf<String, AchievementEntity>()
        private val allFlow = MutableStateFlow<List<AchievementEntity>>(emptyList())
        private val unlockedFlow = MutableStateFlow<List<AchievementEntity>>(emptyList())

        override fun observeUnlocked(): Flow<List<AchievementEntity>> = unlockedFlow
        override fun observeAll(): Flow<List<AchievementEntity>> = allFlow
        override suspend fun getAll(): List<AchievementEntity> = store.values.toList()
        override suspend fun getById(id: String): AchievementEntity? = store[id]

        override suspend fun insert(achievement: AchievementEntity) {
            if (store[achievement.id] == null) {
                store[achievement.id] = achievement
                emit()
            }
        }

        override suspend fun insertAll(achievements: List<AchievementEntity>) {
            achievements.forEach { store[it.id] = it }
            emit()
        }

        override suspend fun unlock(id: String, time: Long): Int {
            val current = store[id] ?: return 0
            if (current.unlockedAt != null) return 0
            store[id] = current.copy(unlockedAt = time)
            emit()
            return 1
        }

        override suspend fun deleteAll() {
            store.clear()
            emit()
        }

        private fun emit() {
            val all = store.values.toList()
            allFlow.value = all
            unlockedFlow.value = all.filter { it.unlockedAt != null }
        }
    }
}
