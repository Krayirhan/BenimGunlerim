package com.benimgunlerim.domain

import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTrackerTest {

    private class FakeAchievementDao : AchievementDao {
        private val store = linkedMapOf<String, AchievementEntity>()
        private val allFlow = MutableStateFlow<List<AchievementEntity>>(emptyList())
        private val unlockedFlow = MutableStateFlow<List<AchievementEntity>>(emptyList())

        override fun observeUnlocked(): Flow<List<AchievementEntity>> =
            unlockedFlow

        override fun observeAll(): Flow<List<AchievementEntity>> = allFlow

        override suspend fun getAll(): List<AchievementEntity> = store.values.toList()

        override suspend fun getById(id: String): AchievementEntity? = store[id]

        override suspend fun insert(achievement: AchievementEntity) {
            if (store[achievement.id] == null) {
                store[achievement.id] = achievement
                emitAll()
            }
        }

        override suspend fun insertAll(achievements: List<AchievementEntity>) {
            achievements.forEach { store[it.id] = it }
            emitAll()
        }

        override suspend fun unlock(id: String, time: Long): Int {
            val current = store[id] ?: return 0
            if (current.unlockedAt != null) return 0
            store[id] = current.copy(unlockedAt = time)
            emitAll()
            return 1
        }

        override suspend fun deleteAll() {
            store.clear()
            emitAll()
        }

        private fun emitAll() {
            val all = store.values.toList()
            allFlow.value = all
            unlockedFlow.value = all.filter { it.unlockedAt != null }
        }
    }

    @Test
    fun tryUnlock_returnsNull_whenIdUnknown() = runTest {
        val tracker = AchievementTracker(FakeAchievementDao())

        val result = tracker.tryUnlock("missing_id")

        assertNull(result)
    }

    @Test
    fun tryUnlock_unlocksOnce_andSecondAttemptReturnsNull() = runTest {
        val tracker = AchievementTracker(FakeAchievementDao())

        val first = tracker.tryUnlock("streak_3")
        val second = tracker.tryUnlock("streak_3")

        assertNotNull(first)
        assertEquals("streak_3", first!!.id)
        assertNull(second)
    }

    @Test
    fun checkStreak_unlocksAllExpectedMilestones() = runTest {
        val tracker = AchievementTracker(FakeAchievementDao())

        tracker.checkStreak(30)

        val unlocked = tracker.unlockedAchievements.first().map { it.id }.toSet()
        assertTrue("streak_3" in unlocked)
        assertTrue("streak_7" in unlocked)
        assertTrue("streak_14" in unlocked)
        assertTrue("streak_30" in unlocked)
    }

    @Test
    fun checkTaskCount_unlocksTaskMilestonesProgressively() = runTest {
        val tracker = AchievementTracker(FakeAchievementDao())

        tracker.checkTaskCount(50)

        val unlocked = tracker.unlockedAchievements.first().map { it.id }.toSet()
        assertTrue("tasks_10" in unlocked)
        assertTrue("tasks_50" in unlocked)
        assertTrue("tasks_100" !in unlocked)
    }

    @Test
    fun checkRoutineCount_andPerfectDay_unlockExpectedIds() = runTest {
        val tracker = AchievementTracker(FakeAchievementDao())

        tracker.checkRoutineCount(100)
        tracker.checkPerfectDay(5)

        val unlocked = tracker.unlockedAchievements.first().map { it.id }.toSet()
        assertTrue("routines_10" in unlocked)
        assertTrue("routines_50" in unlocked)
        assertTrue("routines_100" in unlocked)
        assertTrue("perfect_1" in unlocked)
        assertTrue("perfect_5" in unlocked)
        assertTrue("perfect_20" !in unlocked)
    }

    @Test
    fun checkLevel_checkGold_checkDayClose_andHappiness_unlockExpected() = runTest {
        val tracker = AchievementTracker(FakeAchievementDao())

        tracker.checkLevel(20)
        tracker.checkGold(1000)
        tracker.checkDayClose(30)
        tracker.checkHappiness(90)

        val unlocked = tracker.unlockedAchievements.first().map { it.id }.toSet()
        assertTrue("level_5" in unlocked)
        assertTrue("level_10" in unlocked)
        assertTrue("level_20" in unlocked)
        assertTrue("gold_100" in unlocked)
        assertTrue("gold_500" in unlocked)
        assertTrue("gold_1000" in unlocked)
        assertTrue("close_1" in unlocked)
        assertTrue("close_10" in unlocked)
        assertTrue("close_30" in unlocked)
        assertTrue("companion_happy" in unlocked)
    }

    @Test
    fun allProgress_containsAllDefinitions_andMarksUnlockedOnes() = runTest {
        val tracker = AchievementTracker(FakeAchievementDao())

        tracker.tryUnlock("streak_3")
        val progress = tracker.allProgress.first()

        assertEquals(ALL_ACHIEVEMENTS.size, progress.size)
        assertTrue(progress["streak_3"] == true)
        assertTrue(progress["streak_7"] == false)
    }
}