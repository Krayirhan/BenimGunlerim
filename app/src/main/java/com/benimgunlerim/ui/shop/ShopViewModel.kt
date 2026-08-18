package com.benimgunlerim.ui.shop

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.R
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShopItem(
    val id: String,
    val emoji: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val cost: Int,
    val category: String,
)

/** Dükkan mesajları — kullanıcıya gösterilecek metin, [ShopMessage] resolve edildiği yerde
 * (Composable katmanı) stringResource() ile çözülür. Şu an UI'da render edilmiyor (bkz. ShopScreen),
 * ama string kaynağı hazır bekletiliyor. */
sealed class ShopMessage {
    data object DailyGiftAlreadyClaimed : ShopMessage()
    data object DailyGiftClaimed : ShopMessage()
    data class ItemPurchased(@StringRes val itemNameRes: Int) : ShopMessage()
    data object AlreadyOwned : ShopMessage()
    data object InsufficientGold : ShopMessage()
}

val ALL_SHOP_ITEMS = listOf(
    // Çerçeveler (Başarım kartı görünümleri)
    ShopItem("badge_minimal", "🤍", R.string.shop_item_badge_minimal_name, R.string.shop_item_badge_minimal_desc, 60, "badge"),
    ShopItem("badge_gold", "🥇", R.string.shop_item_badge_gold_name, R.string.shop_item_badge_gold_desc, 80, "badge"),
    ShopItem("badge_crystal", "💎", R.string.shop_item_badge_crystal_name, R.string.shop_item_badge_crystal_desc, 120, "badge"),
    // Kutlama Efektleri
    ShopItem("celebrate_sparkle", "✨", R.string.shop_item_celebrate_sparkle_name, R.string.shop_item_celebrate_sparkle_desc, 70, "effect"),
    ShopItem("celebrate_stars", "🌟", R.string.shop_item_celebrate_stars_name, R.string.shop_item_celebrate_stars_desc, 90, "effect"),
    ShopItem("celebrate_confetti", "🎉", R.string.shop_item_celebrate_confetti_name, R.string.shop_item_celebrate_confetti_desc, 100, "effect"),
    // Renk Aksentler
    ShopItem("accent_ocean", "🌊", R.string.shop_item_accent_ocean_name, R.string.shop_item_accent_ocean_desc, 75, "accent"),
    ShopItem("accent_forest", "🌿", R.string.shop_item_accent_forest_name, R.string.shop_item_accent_forest_desc, 75, "accent"),
    ShopItem("accent_sunset", "🌅", R.string.shop_item_accent_sunset_name, R.string.shop_item_accent_sunset_desc, 75, "accent"),
    ShopItem("accent_aurora", "🔮", R.string.shop_item_accent_aurora_name, R.string.shop_item_accent_aurora_desc, 120, "accent"),
    // Rapor Modu
    ShopItem("report_minimal", "📋", R.string.shop_item_report_minimal_name, R.string.shop_item_report_minimal_desc, 60, "report"),
    ShopItem("report_detailed", "📊", R.string.shop_item_report_detailed_name, R.string.shop_item_report_detailed_desc, 150, "report"),
)

data class ShopUiState(
    val gold: Int = 0,
    val items: List<ShopItem> = ALL_SHOP_ITEMS,
    val ownedItemIds: Set<String> = emptySet(),
    val dailyRewardAvailable: Boolean = false,
    val isClaimingDailyReward: Boolean = false,
    val purchasingItemId: String? = null,
    val purchaseMessage: ShopMessage? = null,
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
    private val dateTimeProvider: DateTimeProvider,
) : ViewModel() {

    private val _purchaseMessage = MutableStateFlow<ShopMessage?>(null)
    private val _isClaimingDailyReward = MutableStateFlow(false)
    private val _purchasingItemId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ShopUiState> = combine(
        prefsRepository.preferences,
        _purchaseMessage,
        _isClaimingDailyReward,
        _purchasingItemId,
    ) { prefs, msg, isClaimingDailyReward, purchasingItemId ->
        val todayStr = dateTimeProvider.today().toString()
        ShopUiState(
            gold = prefs.gold,
            items = ALL_SHOP_ITEMS,
            ownedItemIds = prefs.ownedItems.split(",").filter { it.isNotBlank() }.toSet(),
            dailyRewardAvailable = prefs.lastDailyRewardDate != todayStr,
            isClaimingDailyReward = isClaimingDailyReward,
            purchasingItemId = purchasingItemId,
            purchaseMessage = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopUiState())

    fun claimDailyReward() {
        if (_isClaimingDailyReward.value) return
        viewModelScope.launch {
            _isClaimingDailyReward.value = true
            try {
                val todayStr = dateTimeProvider.today().toString()
                val dailyRewardAvailable = prefsRepository.preferences.first().lastDailyRewardDate != todayStr
                if (!dailyRewardAvailable) {
                    _purchaseMessage.value = ShopMessage.DailyGiftAlreadyClaimed
                    return@launch
                }
                prefsRepository.claimDailyReward(todayStr, 25)
                _purchaseMessage.value = ShopMessage.DailyGiftClaimed
            } finally {
                _isClaimingDailyReward.value = false
            }
        }
    }

    fun purchaseItem(item: ShopItem) {
        if (_purchasingItemId.value != null) return
        if (item.id in uiState.value.ownedItemIds) {
            _purchaseMessage.value = ShopMessage.AlreadyOwned
            return
        }
        viewModelScope.launch {
            _purchasingItemId.value = item.id
            try {
                val success = prefsRepository.purchaseItem(item.id, item.cost)
                _purchaseMessage.value = if (success) {
                    achievementTracker.tryUnlock("first_buy")
                    ShopMessage.ItemPurchased(item.nameRes)
                } else {
                    ShopMessage.InsufficientGold
                }
            } finally {
                _purchasingItemId.value = null
            }
        }
    }

    fun clearMessage() {
        _purchaseMessage.value = null
    }
}
