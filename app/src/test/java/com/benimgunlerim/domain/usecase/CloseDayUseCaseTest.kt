package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.entity.AchievementEntity
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CloseDayUseCaseTest {

    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val fixedDtp = FixedDateTimeProvider(fixedDate = fixedDate, fixedMillis = 1_000L)

    private lateinit var dailyStateRepo: DailyStateRepository
    private lateinit var prefsRepo: UserPreferencesRepository
    private lateinit var useCase: CloseDayUseCase

    @Before
    fun setUp() {
        dailyStateRepo = mockk(relaxed = true)
        prefsRepo = mockk(relaxed = true)

        coEvery { prefsRepo.grantRewardOnce(any(), any(), any(), any()) } returns true
        every { prefsRepo.preferences } returns flowOf(UserPreferences(totalXp = 0))

        val achievementTracker = AchievementTracker(FakeAchievementDao(), fixedDtp)
        val rewardService = RewardGrantService(prefsRepo, achievementTracker, fixedDtp)
        useCase = CloseDayUseCase(dailyStateRepo, prefsRepo, achievementTracker, rewardService, fixedDtp)
    }

    // ── Day-close reward ──────────────────────────────────────────────────────

    @Test
    fun invoke_savesSummaryToRepo() = runTest {
        useCase(mood = 2, note = "Good day", completionRate = 0.8f)

        coVerify { dailyStateRepo.saveSummary(fixedDate, any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_grantsDayCloseXp() = runTest {
        val result = useCase(mood = 2, note = "Ok", completionRate = 0.8f)

        assertFalse(result.dayCloseReward.alreadyGranted)
        assertEquals(GameEngine.XP_DAY_CLOSE, result.dayCloseReward.xpGranted)
    }

    @Test
    fun invoke_dayCloseEventKeyContainsDate() = runTest {
        useCase(mood = 2, note = "Ok", completionRate = 0.8f)

        coVerify { prefsRepo.grantRewardOnce("dayClose:$fixedDate", GameEngine.XP_DAY_CLOSE, any(), any()) }
    }

    @Test
    fun invoke_alreadyClosed_dayCloseRewardAlreadyGranted() = runTest {
        coEvery { prefsRepo.grantRewardOnce("dayClose:$fixedDate", any(), any(), any()) } returns false

        val result = useCase(mood = 2, note = "Ok", completionRate = 0.8f)

        assertTrue(result.dayCloseReward.alreadyGranted)
    }

    // ── Perfect day reward ────────────────────────────────────────────────────

    @Test
    fun invoke_fullCompletionRate_grantsPerfectDayReward() = runTest {
        val result = useCase(mood = 2, note = "Perfect!", completionRate = 1f)

        assertFalse(result.perfectDayReward.alreadyGranted)
        assertTrue(result.perfectDayReward.xpGranted > 0)
    }

    @Test
    fun invoke_partialCompletionRate_noPerfectDayReward() = runTest {
        val result = useCase(mood = 2, note = "Ok", completionRate = 0.9f)

        assertTrue(result.perfectDayReward.alreadyGranted)
        assertEquals(0, result.perfectDayReward.xpGranted)
    }

    @Test
    fun invoke_zeroCompletionRate_noPerfectDayReward() = runTest {
        val result = useCase(mood = 0, note = "", completionRate = 0f)

        assertTrue(result.perfectDayReward.alreadyGranted)
    }

    @Test
    fun invoke_perfectDay_grantsPerfectDayGold() = runTest {
        val result = useCase(mood = 4, note = "Amazing!", completionRate = 1f)

        assertEquals(GameEngine.GOLD_PERFECT_DAY, result.perfectDayReward.goldGranted)
    }

    // ── Mood labels ───────────────────────────────────────────────────────────

    @Test
    fun invoke_moodIndex4_savesHaraka() = runTest {
        useCase(mood = 4, note = "", completionRate = 0f)

        coVerify { dailyStateRepo.saveSummary(any(), "harika", any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_moodIndex0_savesCokKotu() = runTest {
        useCase(mood = 0, note = "", completionRate = 0f)

        coVerify { dailyStateRepo.saveSummary(any(), "cok_kotu", any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_moodOutOfRange_savesNormal() = runTest {
        useCase(mood = 99, note = "", completionRate = 0f)

        coVerify { dailyStateRepo.saveSummary(any(), "normal", any(), any(), any(), any(), any(), any(), any()) }
    }

    // ── Custom date ───────────────────────────────────────────────────────────

    @Test
    fun invoke_customDate_usesGivenDate() = runTest {
        val yesterday = LocalDate.of(2025, 1, 14)

        useCase(date = yesterday, mood = 2, note = "Catch-up", completionRate = 0.5f)

        coVerify { dailyStateRepo.saveSummary(yesterday, any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_customDate_dayCloseKeyUsesGivenDate() = runTest {
        val yesterday = LocalDate.of(2025, 1, 14)

        useCase(date = yesterday, mood = 2, note = "Catch-up", completionRate = 0.5f)

        coVerify { prefsRepo.grantRewardOnce("dayClose:$yesterday", any(), any(), any()) }
    }

    // ── Level-up detection ────────────────────────────────────────────────────

    @Test
    fun invoke_dayCloseXpCrossesThreshold_detectsLevelUp() = runTest {
        every { prefsRepo.preferences } returns flowOf(UserPreferences(totalXp = 95))

        val result = useCase(mood = 2, note = "", completionRate = 0f)

        // 95 + XP_DAY_CLOSE (20) = 115 → crosses 100 XP threshold → level 2
        assertNotNull(result.dayCloseReward.leveledUp)
        assertEquals(2, result.dayCloseReward.leveledUp!!.level)
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
