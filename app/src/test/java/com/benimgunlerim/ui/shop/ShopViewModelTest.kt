package com.benimgunlerim.ui.shop

import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
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
    private val dateTimeProvider: DateTimeProvider = mockk(relaxed = true)
    private val fixedDate = LocalDate.of(2025, 6, 9)

    private lateinit var viewModel: ShopViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { prefsRepository.preferences } returns flowOf(UserPreferences())
        every { dateTimeProvider.today() } returns fixedDate
        viewModel = ShopViewModel(prefsRepository, achievementTracker, dateTimeProvider)
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
        assertEquals(ShopMessage.DailyGiftClaimed, message)
        job.cancel()
    }

    @Test
    fun claimDailyReward_whenAlreadyClaimed_setsAlreadyClaimedMessage() = runTest {
        every { prefsRepository.preferences } returns flowOf(
            UserPreferences(lastDailyRewardDate = fixedDate.toString()),
        )
        val vm = ShopViewModel(prefsRepository, achievementTracker, dateTimeProvider)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.claimDailyReward()
        advanceUntilIdle()

        assertEquals(ShopMessage.DailyGiftAlreadyClaimed, vm.uiState.value.purchaseMessage)
        coVerify(exactly = 0) { prefsRepository.claimDailyReward(any(), any()) }
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
        assertEquals(ShopMessage.ItemPurchased(item.nameRes), message)
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
        assertEquals(ShopMessage.InsufficientGold, message)
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

    @Test
    fun purchaseItem_whenAlreadyOwned_setsOwnedMessageAndSkipsRepository() = runTest {
        val item = ALL_SHOP_ITEMS.first()
        every { prefsRepository.preferences } returns flowOf(
            UserPreferences(ownedItems = item.id),
        )
        val vm = ShopViewModel(prefsRepository, achievementTracker, dateTimeProvider)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.purchaseItem(item)
        advanceUntilIdle()

        assertEquals(ShopMessage.AlreadyOwned, vm.uiState.value.purchaseMessage)
        coVerify(exactly = 0) { prefsRepository.purchaseItem(any(), any()) }
        job.cancel()
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
