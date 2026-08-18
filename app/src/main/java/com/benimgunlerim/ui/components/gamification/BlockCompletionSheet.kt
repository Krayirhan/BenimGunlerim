package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary
import com.benimgunlerim.ui.theme.BrandPrimarySoft

/**
 * 3. Kademe Orta Kutlama: Tüm Görevler / Tüm Rutinler Bitti Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun BlockCompletionSheet(
    title: String,
    subtitle: String,
    badgeText: String,
    isLightDayMode: Boolean = false,
    onCloseDayClick: () -> Unit,
    onContinueClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = AppTokens.Radius.xxl, topEnd = AppTokens.Radius.xxl),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTokens.Spacing.xl, vertical = AppTokens.Spacing.md)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.md),
        ) {
            Text(text = "🎉", fontSize = 40.sp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (isLightDayMode) stringResource(R.string.celebration_block_light_day_body) else subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(AppTokens.Radius.md))
                    .background(BrandPrimarySoft)
                    .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.xs),
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandPrimary,
                )
            }

            Spacer(modifier = Modifier.height(AppTokens.Spacing.xxs))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
            ) {
                Button(
                    onClick = onCloseDayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppTokens.TouchTarget.min),
                    shape = RoundedCornerShape(AppTokens.Radius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                ) {
                    Text(
                        text = stringResource(R.string.celebration_block_close_day_action),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }

                OutlinedButton(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppTokens.TouchTarget.min),
                    shape = RoundedCornerShape(AppTokens.Radius.md),
                ) {
                    Text(
                        text = stringResource(R.string.celebration_action_continue),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
