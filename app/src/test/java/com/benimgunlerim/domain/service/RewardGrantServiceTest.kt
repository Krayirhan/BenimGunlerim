package com.benimgunlerim.domain.service

import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.FixedDateTimeProvider
import com.benimgunlerim.domain.GameEngine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RewardGrantServiceTest {

    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val fixedDtp = FixedDateTimeProvider(fixedDate = fixedDate, fixedMillis = 1_000L)

    private lateinit var prefsRepo: UserPreferencesRepository
    private lateinit var achievementTracker: AchievementTracker
    private lateinit var service: RewardGrantService

    @Before
    fun setUp() {
        prefsRepo = mockk(relaxed = true)
        // Default: reward not yet granted (grantRewardOnce returns true)
        coEvery { prefsRepo.grantRewardOnce(any(), any(), any(), any()) } returns true
        every { prefsRepo.preferences } returns flowOf(UserPreferences(totalXp = 0))

        achievementTracker = AchievementTracker(FakeAchievementDao(), fixedDtp)
        service = RewardGrantService(prefsRepo, achievementTracker, fixedDtp)
    }

    // ── grantOnce ────────────────────────────────────────────────────────────

    @Test
    fun grantOnce_firstTime_returnsXpAndGold() = runTest {
        val result = service.grantOnce(
            eventKey = "task:abc:2025-01-15",
            xp = 12,
            gold = 5,
            currentXp = 0,
        )

        assertFalse(result.alreadyGranted)
        assertEquals(12, result.xpGranted)
        assertEquals(5, result.goldGranted)
    }

    @Test
    fun grantOnce_alreadyGranted_returnsAlreadyGranted() = runTest {
        coEvery { prefsRepo.grantRewardOnce(any(), any(), any(), any()) } returns false

        val result = service.grantOnce(
            eventKey = "task:abc:2025-01-15",
            xp = 12,
            gold = 5,
            currentXp = 0,
        )

        assertTrue(result.alreadyGranted)
        assertEquals(0, result.xpGranted)
        assertEquals(0, result.goldGranted)
    }

    @Test
    fun grantOnce_levelUpDetected_whenXpCrossesThreshold() = runTest {
        // GameEngine.calculateLevel(90).level = 1, calculateLevel(90+12) = still 1
        // Need to cross a threshold: level 1→2 at 100 XP
        val result = service.grantOnce(
            eventKey = "evt:levelup",
            xp = 15,
            currentXp = 90, // 90 + 15 = 105 → level 2
        )

        assertNotNull(result.leveledUp)
        assertEquals(2, result.leveledUp!!.level)
    }

    @Test
    fun grantOnce_noLevelUp_whenXpStaysInSameLevel() = runTest {
        val result = service.grantOnce(
            eventKey = "evt:nolevel",
            xp = 5,
            currentXp = 0, // 0 + 5 = 5 → still level 1
        )

        assertNull(result.leveledUp)
        assertFalse(result.alreadyGranted)
    }

    @Test
    fun grantOnce_callsPrefsRepository() = runTest {
        service.grantOnce(
            eventKey = "task:xyz:2025-01-15",
            xp = 18,
            gold = 5,
            happinessDelta = 5,
            currentXp = 0,
        )

        coVerify { prefsRepo.grantRewardOnce("task:xyz:2025-01-15", 18, 5, 5) }
    }

    // ── grantAllTasksBonusIfEligible ─────────────────────────────────────────

    @Test
    fun allTasksBonus_emptyTaskList_returnsAlreadyGranted() = runTest {
        val result = service.grantAllTasksBonusIfEligible(
            taskIds = emptyList(),
            completedIds = emptySet(),
            justToggledId = "t1",
            currentXp = 0,
        )

        assertTrue(result.alreadyGranted)
    }

    @Test
    fun allTasksBonus_notAllCompleted_returnsAlreadyGranted() = runTest {
        val result = service.grantAllTasksBonusIfEligible(
            taskIds = listOf("t1", "t2", "t3"),
            completedIds = setOf("t1"),
            justToggledId = "t2",
            currentXp = 0,
        )

        assertTrue(result.alreadyGranted)
    }

    @Test
    fun allTasksBonus_allCompleted_grantsBonus() = runTest {
        val result = service.grantAllTasksBonusIfEligible(
            taskIds = listOf("t1", "t2"),
            completedIds = setOf("t1"),     // t1 was already completed
            justToggledId = "t2",           // t2 just completed → all done
            currentXp = 0,
        )

        assertFalse(result.alreadyGranted)
        assertEquals(GameEngine.XP_ALL_TASKS_BONUS, result.xpGranted)
    }

    @Test
    fun allTasksBonus_usesDateFromProvider() = runTest {
        service.grantAllTasksBonusIfEligible(
            taskIds = listOf("t1"),
            completedIds = emptySet(),
            justToggledId = "t1",
            currentXp = 0,
        )

        coVerify { prefsRepo.grantRewardOnce("allTasks:$fixedDate", any(), any(), any()) }
    }

    // ── grantAllRoutinesBonusIfEligible ──────────────────────────────────────

    @Test
    fun allRoutinesBonus_notAllCompleted_returnsAlreadyGranted() = runTest {
        val result = service.grantAllRoutinesBonusIfEligible(
            routineIds = listOf("r1", "r2", "r3"),
            completedIds = setOf("r1"),
            justToggledId = "r2",
            currentXp = 0,
        )

        assertTrue(result.alreadyGranted)
    }

    @Test
    fun allRoutinesBonus_justOneRoutine_allCompleted_grantsBonus() = runTest {
        val result = service.grantAllRoutinesBonusIfEligible(
            routineIds = listOf("r1"),
            completedIds = emptySet(),
            justToggledId = "r1",
            currentXp = 0,
        )

        assertFalse(result.alreadyGranted)
        assertEquals(GameEngine.XP_ALL_ROUTINES_BONUS, result.xpGranted)
    }

    @Test
    fun allRoutinesBonus_emptyList_returnsAlreadyGranted() = runTest {
        val result = service.grantAllRoutinesBonusIfEligible(
            routineIds = emptyList(),
            completedIds = emptySet(),
            justToggledId = "r1",
            currentXp = 0,
        )

        assertTrue(result.alreadyGranted)
    }

    // ── FakeAchievementDao ────────────────────────────────────────────────────

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
