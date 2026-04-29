package com.benimgunlerim.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val name: String,
    val description: String,
    val cost: Int,
    val category: String,
)

val ALL_SHOP_ITEMS = listOf(
    // Çerçeveler (Başarım kartı görünümleri)
    ShopItem("badge_minimal", "🤍", "Minimal Çerçeve", "Başarım kartlarında sade çizgi çerçeve", 60, "badge"),
    ShopItem("badge_gold", "🥇", "Altın Çerçeve", "Başarım kartlarında altın çerçeve", 80, "badge"),
    ShopItem("badge_crystal", "💎", "Kristal Çerçeve", "Işıl ışıl kristal çerçeve", 120, "badge"),
    // Kutlama Efektleri
    ShopItem("celebrate_sparkle", "✨", "Işıltı Flaşı", "Görev bitince zarif ışık efekti", 70, "effect"),
    ShopItem("celebrate_stars", "🌟", "Yıldız Patlaması", "Görev bitince yıldız animasyonu", 90, "effect"),
    ShopItem("celebrate_confetti", "🎉", "Konfeti Yağmuru", "Görev bitince tam konfeti yağmuru", 100, "effect"),
    // Renk Aksentler
    ShopItem("accent_ocean", "🌊", "Okyanus Mavisi", "Rutin kartları için okyanus tonları", 75, "accent"),
    ShopItem("accent_forest", "🌿", "Orman Yeşili", "Derin yeşil rutin kartları", 75, "accent"),
    ShopItem("accent_sunset", "🌅", "Gün Batımı", "Sıcak turuncu-pembe rutin kartları", 75, "accent"),
    ShopItem("accent_aurora", "🔮", "Kuzey Işıkları", "Neon mavi-mor efekt", 120, "accent"),
    // Rapor Modu
    ShopItem("report_minimal", "📋", "Minimal Rapor", "Haftalık özetini sade göster", 60, "report"),
    ShopItem("report_detailed", "📊", "Detaylı Rapor", "Haftalık raporda grafik ve geniş analiz", 150, "report"),
)

data class ShopUiState(
    val gold: Int = 0,
    val items: List<ShopItem> = ALL_SHOP_ITEMS,
    val ownedItemIds: Set<String> = emptySet(),
    val dailyRewardAvailable: Boolean = false,
    val isClaimingDailyReward: Boolean = false,
    val purchasingItemId: String? = null,
    val purchaseMessage: String? = null,
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
    private val dateTimeProvider: DateTimeProvider,
) : ViewModel() {

    private val _purchaseMessage = MutableStateFlow<String?>(null)
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
                    _purchaseMessage.value = "Günlük hediye zaten alındı."
                    return@launch
                }
                prefsRepository.claimDailyReward(todayStr, 25)
                _purchaseMessage.value = "+25 🪙 Günlük hediye alındı!"
            } finally {
                _isClaimingDailyReward.value = false
            }
        }
    }

    fun purchaseItem(item: ShopItem) {
        if (_purchasingItemId.value != null) return
        if (item.id in uiState.value.ownedItemIds) {
            _purchaseMessage.value = "Bu urune zaten sahipsin."
            return
        }
        viewModelScope.launch {
            _purchasingItemId.value = item.id
            try {
                val success = prefsRepository.purchaseItem(item.id, item.cost)
                _purchaseMessage.value = if (success) {
                    achievementTracker.tryUnlock("first_buy")
                    "${item.name} satın alındı! ✨"
                } else {
                    "Yeterli altın yok 😢"
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
