package com.benimgunlerim.ui.shop

import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.AchievementTracker
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val prefsRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val achievementTracker: AchievementTracker = mockk(relaxed = true)

    private lateinit var viewModel: ShopViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { prefsRepository.preferences } returns flowOf(UserPreferences())
        viewModel = ShopViewModel(prefsRepository, achievementTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun uiState_initialPurchaseMessage_isNull() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.purchaseMessage)
        job.cancel()
    }

    @Test
    fun uiState_items_containsAllShopItems() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(ALL_SHOP_ITEMS, viewModel.uiState.value.items)
        job.cancel()
    }

    // ── claimDailyReward ──────────────────────────────────────────────────────

    @Test
    fun claimDailyReward_callsRepository() = runTest {
        viewModel.claimDailyReward()
        advanceUntilIdle()

        coVerify { prefsRepository.claimDailyReward(any(), 25) }
    }

    @Test
    fun claimDailyReward_setsPurchaseMessage() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.claimDailyReward()
        advanceUntilIdle()

        val message = viewModel.uiState.value.purchaseMessage
        assertEquals("+25 🪙 Günlük hediye alındı!", message)
        job.cancel()
    }

    // ── purchaseItem ──────────────────────────────────────────────────────────

    @Test
    fun purchaseItem_whenSuccess_setsSuccessMessage() = runTest {
        val item = ALL_SHOP_ITEMS.first()
        coEvery { prefsRepository.purchaseItem(item.id, item.cost) } returns true

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.purchaseItem(item)
        advanceUntilIdle()

        val message = viewModel.uiState.value.purchaseMessage
        assertEquals("${item.name} satın alındı! ✨", message)
        job.cancel()
    }

    @Test
    fun purchaseItem_whenSuccess_triesToUnlockFirstBuyAchievement() = runTest {
        val item = ALL_SHOP_ITEMS.first()
        coEvery { prefsRepository.purchaseItem(item.id, item.cost) } returns true

        viewModel.purchaseItem(item)
        advanceUntilIdle()

        coVerify { achievementTracker.tryUnlock("first_buy") }
    }

    @Test
    fun purchaseItem_whenInsufficientGold_setsFailureMessage() = runTest {
        val item = ALL_SHOP_ITEMS.first()
        coEvery { prefsRepository.purchaseItem(item.id, item.cost) } returns false

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.purchaseItem(item)
        advanceUntilIdle()

        val message = viewModel.uiState.value.purchaseMessage
        assert(message != null && message.isNotBlank()) {
            "Expected failure message but got: $message"
        }
        assert(message != "${item.name} satın alındı! ✨") {
            "Expected failure message but got success message"
        }
        job.cancel()
    }

    @Test
    fun purchaseItem_whenInsufficientGold_doesNotUnlockAchievement() = runTest {
        val item = ALL_SHOP_ITEMS.first()
        coEvery { prefsRepository.purchaseItem(item.id, item.cost) } returns false

        viewModel.purchaseItem(item)
        advanceUntilIdle()

        coVerify(exactly = 0) { achievementTracker.tryUnlock(any()) }
    }

    // ── clearMessage ──────────────────────────────────────────────────────────

    @Test
    fun clearMessage_resetsToNull() = runTest {
        coEvery { prefsRepository.purchaseItem(any(), any()) } returns true
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.purchaseItem(ALL_SHOP_ITEMS.first())
        advanceUntilIdle()

        viewModel.clearMessage()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.purchaseMessage)
        job.cancel()
    }
}
