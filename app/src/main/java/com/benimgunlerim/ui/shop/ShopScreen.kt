package com.benimgunlerim.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.gamification.GoldBadge
import com.benimgunlerim.ui.components.layout.DetailScreenScaffold
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

    DetailScreenScaffold(
        title = stringResource(R.string.shop_title),
        onBack = onBack,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = AppTokens.Spacing.screenHorizontal, vertical = AppTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sectionGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GoldBadge(gold = state.gold)
            }

            InfoCard(
                title = stringResource(R.string.shop_daily_gift_title),
                body = stringResource(R.string.shop_daily_gift_info),
            )

            SectionBlock(title = stringResource(R.string.shop_products_section_title)) {
                state.items.forEach { item ->
                    val isOwned = item.id in state.ownedItemIds
                    ShopItemCard(
                        title = stringResource(item.nameRes),
                        description = stringResource(item.descriptionRes),
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
