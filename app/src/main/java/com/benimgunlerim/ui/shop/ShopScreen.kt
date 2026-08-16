package com.benimgunlerim.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.ui.components.gamification.GoldBadge
import com.benimgunlerim.ui.components.layout.ScreenScaffold
import com.benimgunlerim.ui.components.molecules.InfoCard
import com.benimgunlerim.ui.components.molecules.SectionBlock
import com.benimgunlerim.ui.components.organisms.ShopItemCard
import com.benimgunlerim.ui.theme.AppTokens

@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    ScreenScaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Dükkan",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                GoldBadge(gold = state.gold)
            }

            InfoCard(
                title = "Günlük Hediye",
                body = "Her gün dükkanı ziyaret et ve bedava altın kazan!",
            )

            SectionBlock(title = "Mağaza Ürünleri") {
                state.items.forEach { item ->
                    val isOwned = item.id in state.ownedItemIds
                    ShopItemCard(
                        title = item.name,
                        description = item.description,
                        priceGold = item.cost,
                        isPurchased = isOwned,
                        onPurchaseClick = { viewModel.purchaseItem(item) },
                        userGold = state.gold,
                    )
                }
            }
        }
    }
}
