@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.shop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.GoldBadge
import com.benimgunlerim.ui.components.ScreenHeader
import com.benimgunlerim.ui.components.SectionTitle
import com.benimgunlerim.ui.components.VerticalSpacer
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CandyTertiary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.XpGold

@Composable
fun ShopScreen(viewModel: ShopViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.purchaseMessage) {
        state.purchaseMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Shop categories (resolved outside LazyListScope so @Composable stringResource() is valid)
    val categories = listOf(
        stringResource(R.string.shop_category_badges) to "badge",
        stringResource(R.string.shop_category_effects) to "effect",
        stringResource(R.string.shop_category_accents) to "accent",
        stringResource(R.string.shop_category_reports) to "report",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            item {
                ScreenHeader(
                    title = "\uD83C\uDFEA D\u00fckkan",
                    subtitle = "Arkada\u015f\u0131n\u0131 s\u00fcsle, d\u00fcnyan\u0131 renklendir!",
                    extra = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GoldBadge(gold = state.gold)
                        }
                    },
                )
            }

            // Daily gift box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.dailyRewardAvailable) XpGold.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "\uD83C\uDF81", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = "G\u00fcnl\u00fck Hediye",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (state.dailyRewardAvailable) {
                            Text(
                                text = stringResource(R.string.shop_daily_gift_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = XpGold,
                                textAlign = TextAlign.Center,
                            )
                            Button(
                                onClick = { viewModel.claimDailyReward() },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = XpGold),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                enabled = !state.isClaimingDailyReward,
                            ) {
                                Text(stringResource(R.string.shop_daily_gift_claim_btn), color = Color.White)
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.shop_daily_gift_claimed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            categories.forEach { (catName, catId) ->
                val categoryItems = state.items.filter { it.category == catId }
                if (categoryItems.isNotEmpty()) {
                    item {
                        SectionTitle(
                            title = catName,
                            subtitle = stringResource(R.string.shop_category_item_count, categoryItems.size),
                            accentColor = CandyPrimary,
                        )
                    }
                    items(categoryItems) { shopItem ->
                        val owned = shopItem.id in state.ownedItemIds
                        ShopItemCard(
                            item = shopItem,
                            owned = owned,
                            canAfford = state.gold >= shopItem.cost,
                            isPurchasing = state.purchasingItemId == shopItem.id,
                            onBuy = { viewModel.purchaseItem(shopItem) },
                        )
                    }
                }
            }

            item { VerticalSpacer(16) }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun ShopItemCard(
    item: ShopItem,
    owned: Boolean,
    canAfford: Boolean,
    isPurchasing: Boolean,
    onBuy: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (owned) CompletedGreen.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "shopBg",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (owned) CompletedGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CandyPrimary.copy(alpha = 0.15f), CandyTertiary.copy(alpha = 0.15f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = item.emoji, style = MaterialTheme.typography.headlineSmall)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (owned) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CompletedGreen)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(stringResource(R.string.shop_item_owned), style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = canAfford && !isPurchasing,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = XpGold,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    contentPadding = ButtonDefaults.ContentPadding,
                ) {
                    Text(
                        if (isPurchasing) "..." else stringResource(R.string.shop_purchase_button_icon) + " ${item.cost}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}