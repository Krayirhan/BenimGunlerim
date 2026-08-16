package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.theme.AppTokens

import com.benimgunlerim.ui.theme.NightAccent
import com.benimgunlerim.ui.theme.NightMuted
import com.benimgunlerim.ui.theme.NightSurface
import com.benimgunlerim.ui.theme.NightText

private val CloseDayChevronContainer = Color(0xFF2B3B4D)

@Composable
fun CloseDayCard(
    isDayClosed: Boolean,
    onCloseDayClick: () -> Unit,
    modifier: Modifier = Modifier,
    closedMessage: String? = null,
) {
    Box(
        modifier = modifier
            .testTag(TestTags.TodayCloseDayCard)
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(NightSurface)
            .clickable(enabled = !isDayClosed, onClick = onCloseDayClick)
            .padding(horizontal = AppTokens.Spacing.cardInner, vertical = AppTokens.Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                Text(
                    text = if (isDayClosed) {
                        stringResource(R.string.today_close_day_done_title)
                    } else {
                        "🌙 " + stringResource(R.string.today_close_day_title)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDayClosed) NightText else NightAccent,
                )
                Text(
                    text = if (isDayClosed) {
                        closedMessage ?: stringResource(R.string.today_close_day_done_desc)
                    } else {
                        stringResource(R.string.today_close_day_desc)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = NightMuted,
                )
            }

            if (!isDayClosed) {
                Box(
                    modifier = Modifier
                        .size(AppTokens.IconSize.container)
                        .clip(CircleShape)
                        .background(CloseDayChevronContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.today_close_day_action),
                        tint = NightText,
                        modifier = Modifier.size(AppTokens.IconSize.sm),
                    )
                }
            }
        }
    }
}

