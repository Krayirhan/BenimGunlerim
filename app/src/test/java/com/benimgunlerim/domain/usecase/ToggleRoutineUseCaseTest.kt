package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.FixedDateTimeProvider
import com.benimgunlerim.domain.GameEngine
import com.benimgunlerim.domain.service.RewardGrantService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleRoutineUseCaseTest {

    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val fixedDtp = FixedDateTimeProvider(fixedDate = fixedDate, fixedMillis = 1_000L)

    private lateinit var routineRepo: RoutineRepository
    private lateinit var prefsRepo: UserPreferencesRepository
    private lateinit var useCase: ToggleRoutineUseCase

    @Before
    fun setUp() {
        routineRepo = mockk(relaxed = true)
        prefsRepo = mockk(relaxed = true)

        coEvery { prefsRepo.grantRewardOnce(any(), any(), any(), any()) } returns true
        every { prefsRepo.preferences } returns flowOf(UserPreferences(totalXp = 0))

        val achievementTracker = AchievementTracker(FakeAchievementDao(), fixedDtp)
        val rewardService = RewardGrantService(prefsRepo, achievementTracker, fixedDtp)
        useCase = ToggleRoutineUseCase(routineRepo, prefsRepo, achievementTracker, rewardService, fixedDtp)
    }

    // ── Completing a routine ──────────────────────────────────────────────────

    @Test
    fun invoke_notCompleted_returnsResult() = runTest {
        val result = useCase(checkRoutine(), completedToday = false)

        assertNotNull(result)
    }

    @Test
    fun invoke_notCompleted_writesCompletionLog() = runTest {
        useCase(checkRoutine(), completedToday = false)

        coVerify { routineRepo.writeCompletionLog(any(), fixedDate) }
    }

    @Test
    fun invoke_notCompleted_grantsPositiveXp() = runTest {
        val result = useCase(checkRoutine(), completedToday = false)

        assertTrue(result!!.routineReward.xpGranted > 0)
    }

    @Test
    fun invoke_goalRoutine_grantsGoalXp() = runTest {
        val result = useCase(goalRoutine(), completedToday = false)

        assertTrue(result!!.routineReward.xpGranted >= GameEngine.XP_ROUTINE_GOAL)
    }

    // ── Un-completing a routine ───────────────────────────────────────────────

    @Test
    fun invoke_alreadyCompleted_returnsNull() = runTest {
        val result = useCase(checkRoutine(), completedToday = true)

        assertNull(result)
    }

    @Test
    fun invoke_alreadyCompleted_deletesCompletionLog() = runTest {
        useCase(checkRoutine(), completedToday = true)

        coVerify { routineRepo.deleteCompletionLog("routine-1", fixedDate) }
    }

    // ── All-routines bonus ────────────────────────────────────────────────────

    @Test
    fun invoke_lastUncompletedRoutine_grantsAllRoutinesBonus() = runTest {
        val result = useCase(
            routine = checkRoutine(),
            completedToday = false,
            completedRoutineIds = emptySet(),            // none were completed before
            allTodayRoutineIds = listOf("routine-1"),    // only this routine today
        )

        assertFalse(result!!.allRoutinesBonus.alreadyGranted)
        coVerify { prefsRepo.grantRewardOnce(match { it.startsWith("allRoutines:") }, any(), any(), any()) }
    }

    @Test
    fun invoke_otherRoutinesStillPending_noAllRoutinesBonus() = runTest {
        val result = useCase(
            routine = checkRoutine(),
            completedToday = false,
            completedRoutineIds = emptySet(),
            allTodayRoutineIds = listOf("routine-1", "routine-2"),  // 2nd not done
        )

        assertTrue(result!!.allRoutinesBonus.alreadyGranted)
    }

    @Test
    fun invoke_customDate_usedForLog() = runTest {
        val customDate = LocalDate.of(2025, 2, 1)

        useCase(checkRoutine(), completedToday = false, date = customDate)

        coVerify { routineRepo.writeCompletionLog(any(), customDate) }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun checkRoutine() = RoutineEntity(
        id = "routine-1",
        name = "Morning Run",
        description = null,
        targetDays = "1,2,3,4,5,6,7",
        preferredTime = "07:00",
        color = null,
        isArchived = false,
        createdAt = 1_000L,
        updatedAt = 1_000L,
        targetType = "check",
        targetValue = null,
    )

    private fun goalRoutine() = checkRoutine().copy(
        id = "routine-2",
        targetType = "goal",
        targetValue = 5,
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
            if (store[achievement.id] == null) { store[achievement.id] = achievement; emit() }
        }

        override suspend fun insertAll(achievements: List<AchievementEntity>) {
            achievements.forEach { store[it.id] = it }; emit()
        }

        override suspend fun unlock(id: String, time: Long): Int {
            val current = store[id] ?: return 0
            if (current.unlockedAt != null) return 0
            store[id] = current.copy(unlockedAt = time); emit()
            return 1
        }

        override suspend fun deleteAll() { store.clear(); emit() }

        private fun emit() {
            val all = store.values.toList()
            allFlow.value = all
            unlockedFlow.value = all.filter { it.unlockedAt != null }
        }
    }
}
