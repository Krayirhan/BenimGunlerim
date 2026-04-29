package com.benimgunlerim.ui.achievements

import com.benimgunlerim.domain.ALL_ACHIEVEMENTS
import com.benimgunlerim.domain.AchievementTracker
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val achievementTracker: AchievementTracker = mockk()
    private val progressFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private lateinit var viewModel: AchievementsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { achievementTracker.allProgress } returns progressFlow
        viewModel = AchievementsViewModel(achievementTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() {
        // Before any flow emission the initial value has isLoading = true
        val initial = viewModel.uiState.value
        assertTrue(initial.isLoading)
    }

    @Test
    fun emptyProgress_allAchievementsLocked() = runTest {
        val job = launch { viewModel.uiState.collect { } }
        progressFlow.emit(emptyMap())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.unlockedCount)
        assertEquals(ALL_ACHIEVEMENTS.size, state.totalCount)
        assertTrue(state.achievements.none { it.isUnlocked })
        job.cancel()
    }

    @Test
    fun oneUnlockedAchievement_countIsOne() = runTest {
        val job = launch { viewModel.uiState.collect { } }
        progressFlow.emit(mapOf("streak_3" to true))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.unlockedCount)
        assertTrue(state.achievements.first { it.def.id == "streak_3" }.isUnlocked)
        assertFalse(state.achievements.first { it.def.id == "streak_7" }.isUnlocked)
        job.cancel()
    }

    @Test
    fun allAchievementsUnlocked_countEqualsTotal() = runTest {
        val job = launch { viewModel.uiState.collect { } }
        val allUnlocked = ALL_ACHIEVEMENTS.associate { it.id to true }
        progressFlow.emit(allUnlocked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ALL_ACHIEVEMENTS.size, state.unlockedCount)
        assertEquals(ALL_ACHIEVEMENTS.size, state.totalCount)
        job.cancel()
    }

    @Test
    fun totalCount_matchesAllAchievementsSize() = runTest {
        val job = launch { viewModel.uiState.collect { } }
        progressFlow.emit(emptyMap())
        advanceUntilIdle()

        assertEquals(ALL_ACHIEVEMENTS.size, viewModel.uiState.value.totalCount)
        job.cancel()
    }

    @Test
    fun progressUpdate_reflectsInState() = runTest {
        val job = launch { viewModel.uiState.collect { } }
        progressFlow.emit(emptyMap())
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.unlockedCount)

        progressFlow.emit(mapOf("tasks_10" to true, "streak_7" to true))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.unlockedCount)
        job.cancel()
    }
}
