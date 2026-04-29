package com.benimgunlerim.ui.onboarding

import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.usecase.AddRoutineUseCase
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val prefsRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val addRoutineUseCase: AddRoutineUseCase = mockk(relaxed = true)
    private val addTaskUseCase: AddTaskUseCase = mockk(relaxed = true)
    private val dateTimeProvider: DateTimeProvider = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val fixedDate = LocalDate.of(2025, 6, 9)

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { prefsRepository.preferences } returns flowOf(UserPreferences())
        every { dateTimeProvider.today() } returns fixedDate
        viewModel = OnboardingViewModel(prefsRepository, addRoutineUseCase, addTaskUseCase, dateTimeProvider, analyticsTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── completeOnboarding ────────────────────────────────────────────────────

    @Test
    fun completeOnboarding_addsApprovedRoutines() = runTest {
        viewModel.completeOnboarding(
            needId = "productivity",
            intensityId = "medium",
            approvedRoutineNames = listOf("Morning Run", "Read 30 min"),
            approvedTaskTitle = null,
        )
        advanceUntilIdle()

        coVerify { addRoutineUseCase("Morning Run", any(), any(), any(), any(), any()) }
        coVerify { addRoutineUseCase("Read 30 min", any(), any(), any(), any(), any()) }
    }

    @Test
    fun completeOnboarding_withTask_addsTask() = runTest {
        viewModel.completeOnboarding(
            needId = "productivity",
            intensityId = "low",
            approvedRoutineNames = emptyList(),
            approvedTaskTitle = "Plan my week",
        )
        advanceUntilIdle()

        coVerify { addTaskUseCase("Plan my week", fixedDate, null, null, null, 2, null) }
    }

    @Test
    fun completeOnboarding_withBlankTask_doesNotAddTask() = runTest {
        viewModel.completeOnboarding(
            needId = "productivity",
            intensityId = "low",
            approvedRoutineNames = emptyList(),
            approvedTaskTitle = "   ",
        )
        advanceUntilIdle()

        coVerify(exactly = 0) { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun completeOnboarding_withNullTask_doesNotAddTask() = runTest {
        viewModel.completeOnboarding(
            needId = "productivity",
            intensityId = "low",
            approvedRoutineNames = emptyList(),
            approvedTaskTitle = null,
        )
        advanceUntilIdle()

        coVerify(exactly = 0) { addTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun completeOnboarding_callsCompleteOnboardingWithNeedId() = runTest {
        viewModel.completeOnboarding(
            needId = "wellbeing",
            intensityId = "high",
            approvedRoutineNames = emptyList(),
            approvedTaskTitle = null,
        )
        advanceUntilIdle()

        coVerify { prefsRepository.completeOnboarding("wellbeing") }
    }

    @Test
    fun completeOnboarding_grantsXp() = runTest {
        viewModel.completeOnboarding(
            needId = "productivity",
            intensityId = "low",
            approvedRoutineNames = emptyList(),
            approvedTaskTitle = null,
        )
        advanceUntilIdle()

        coVerify { prefsRepository.addXp(10) }
    }

    @Test
    fun completeOnboarding_tracksAnalytics() = runTest {
        viewModel.completeOnboarding(
            needId = "productivity",
            intensityId = "medium",
            approvedRoutineNames = listOf("Run"),
            approvedTaskTitle = "Task",
        )
        advanceUntilIdle()

        coVerify {
            analyticsTracker.track(
                match { event ->
                    event.name == "onboarding_completed" &&
                        event.properties["need"] == "productivity" &&
                        event.properties["intensity"] == "medium"
                },
            )
        }
    }

    @Test
    fun completeOnboarding_withNoRoutines_addsNoRoutines() = runTest {
        viewModel.completeOnboarding(
            needId = "productivity",
            intensityId = "low",
            approvedRoutineNames = emptyList(),
            approvedTaskTitle = null,
        )
        advanceUntilIdle()

        coVerify(exactly = 0) { addRoutineUseCase(any(), any(), any(), any(), any(), any()) }
    }
}
