package com.benimgunlerim.ui.progress

import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.domain.usecase.ObserveProgressSnapshotUseCase
import com.benimgunlerim.domain.usecase.ProgressSnapshot
import io.mockk.every
import io.mockk.mockk
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
class ProgressViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeProgressSnapshot: ObserveProgressSnapshotUseCase = mockk()

    private lateinit var viewModel: ProgressViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeProgressSnapshot() } returns flowOf(emptySnapshot())
        viewModel = ProgressViewModel(observeProgressSnapshot)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initialValue_hasDefaults() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(ProgressUiState(), viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun uiState_mapsCurrentStreak_fromSnapshot() = runTest {
        val snapshot = emptySnapshot().copy(currentStreak = 7)
        every { observeProgressSnapshot() } returns flowOf(snapshot)
        val vm = ProgressViewModel(observeProgressSnapshot)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(7, vm.uiState.value.currentStreak)
        job.cancel()
    }

    @Test
    fun uiState_mapsBestStreak_fromSnapshot() = runTest {
        val snapshot = emptySnapshot().copy(bestStreak = 14)
        every { observeProgressSnapshot() } returns flowOf(snapshot)
        val vm = ProgressViewModel(observeProgressSnapshot)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(14, vm.uiState.value.bestStreak)
        job.cancel()
    }

    @Test
    fun uiState_mapsAverageScore_fromSnapshot() = runTest {
        val snapshot = emptySnapshot().copy(averageScore = 75)
        every { observeProgressSnapshot() } returns flowOf(snapshot)
        val vm = ProgressViewModel(observeProgressSnapshot)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(75, vm.uiState.value.averageScore)
        job.cancel()
    }

    @Test
    fun uiState_mapsRoutineHitRate_fromSnapshot() = runTest {
        val snapshot = emptySnapshot().copy(routineHitRate = 0.8f)
        every { observeProgressSnapshot() } returns flowOf(snapshot)
        val vm = ProgressViewModel(observeProgressSnapshot)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(0.8f, vm.uiState.value.routineHitRate)
        job.cancel()
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun emptySnapshot() = ProgressSnapshot(
        last30Days = emptyList(),
        currentStreak = 0,
        bestStreak = 0,
        averageScore = 0,
        weeklyScore = 0,
        moodTrend = emptyList(),
        energyTrend = emptyList(),
        routineHitRate = 0f,
        taskHitRate = 0f,
        gameState = UserPreferences(),
        unlockedAchievements = emptyList(),
    )
}
