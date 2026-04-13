package com.benimgunlerim.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.AchievementTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    val purchaseMessage: String? = null,
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
) : ViewModel() {

    private val _purchaseMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ShopUiState> = combine(
        prefsRepository.preferences,
        _purchaseMessage,
    ) { prefs, msg ->
        val todayStr = LocalDate.now().toString()
        ShopUiState(
            gold = prefs.gold,
            items = ALL_SHOP_ITEMS,
            ownedItemIds = prefs.ownedItems.split(",").filter { it.isNotBlank() }.toSet(),
            dailyRewardAvailable = prefs.lastDailyRewardDate != todayStr,
            purchaseMessage = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopUiState())

    fun claimDailyReward() {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            prefsRepository.claimDailyReward(todayStr, 25)
            _purchaseMessage.value = "+25 🪙 Günlük hediye alındı!"
        }
    }

    fun purchaseItem(item: ShopItem) {
        viewModelScope.launch {
            val success = prefsRepository.purchaseItem(item.id, item.cost)
            _purchaseMessage.value = if (success) {
                achievementTracker.tryUnlock("first_buy")
                "${item.name} satın alındı! ✨"
            } else {
                "Yeterli altın yok 😢"
            }
        }
    }

    fun clearMessage() {
        _purchaseMessage.value = null
    }
}
