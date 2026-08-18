package com.benimgunlerim.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.DetailScreenScaffold
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary

data class PrivacySection(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val bulletPoints: List<String> = emptyList(),
)

@Suppress("LongMethod")
@Composable
private fun privacySections(): List<PrivacySection> = listOf(
    PrivacySection(
        title = stringResource(R.string.privacy_section1_title),
        icon = Icons.Rounded.Lock,
        description = stringResource(R.string.privacy_section1_desc),
        bulletPoints = listOf(
            stringResource(R.string.privacy_section1_bullet1),
            stringResource(R.string.privacy_section1_bullet2),
        ),
    ),
    PrivacySection(
        title = stringResource(R.string.privacy_section2_title),
        icon = Icons.Rounded.Analytics,
        description = stringResource(R.string.privacy_section2_desc),
        bulletPoints = listOf(
            stringResource(R.string.privacy_section2_bullet1),
            stringResource(R.string.privacy_section2_bullet2),
        ),
    ),
    PrivacySection(
        title = stringResource(R.string.privacy_section3_title),
        icon = Icons.Rounded.BugReport,
        description = stringResource(R.string.privacy_section3_desc),
        bulletPoints = listOf(
            stringResource(R.string.privacy_section3_bullet1),
        ),
    ),
    PrivacySection(
        title = stringResource(R.string.privacy_section4_title),
        icon = Icons.Rounded.CloudDownload,
        description = stringResource(R.string.privacy_section4_desc),
        bulletPoints = listOf(
            stringResource(R.string.privacy_section4_bullet1),
            stringResource(R.string.privacy_section4_bullet2),
        ),
    ),
    PrivacySection(
        title = stringResource(R.string.privacy_section5_title),
        icon = Icons.Rounded.NotificationsActive,
        description = stringResource(R.string.privacy_section5_desc),
        bulletPoints = listOf(
            stringResource(R.string.privacy_section5_bullet1),
            stringResource(R.string.privacy_section5_bullet2),
        ),
    ),
    PrivacySection(
        title = stringResource(R.string.privacy_section6_title),
        icon = Icons.Rounded.DeleteForever,
        description = stringResource(R.string.privacy_section6_desc),
        bulletPoints = listOf(
            stringResource(R.string.privacy_section6_bullet1),
            stringResource(R.string.privacy_section6_bullet2),
        ),
    ),
    PrivacySection(
        title = stringResource(R.string.privacy_section7_title),
        icon = Icons.Rounded.Email,
        description = stringResource(R.string.privacy_section7_desc),
        bulletPoints = listOf(
            stringResource(R.string.privacy_section7_bullet1),
        ),
    ),
)

@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    webPolicyUrl: String = "https://krayirhan.com/benimgunlerim/privacy",
) {
    val context = LocalContext.current

    DetailScreenScaffold(
        title = stringResource(R.string.settings_privacy_policy_title),
        onBack = onNavigateBack,
        actions = {
            IconButton(
                onClick = {
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webPolicyUrl))
                        context.startActivity(intent)
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInBrowser,
                    contentDescription = stringResource(R.string.privacy_web_action_cd),
                    tint = BrandPrimary,
                )
            }
        },
    ) { padding ->
        val sections = privacySections()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PrivacyHeaderCard() }
            items(sections) { section -> PrivacySectionCard(section) }
        }
    }
}

@Composable
private fun PrivacyHeaderCard() {
    AppSurface(
        radius = AppTokens.Radius.md,
        padding = AppTokens.Spacing.cardInner,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = BrandPrimary,
            )
            Column {
                Text(
                    text = stringResource(R.string.privacy_header_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.privacy_header_updated),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PrivacySectionCard(section: PrivacySection) {
    AppSurface(
        radius = AppTokens.Radius.md,
        padding = AppTokens.Spacing.cardInner,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = BrandPrimary,
                )
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (section.bulletPoints.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    section.bulletPoints.forEach { bullet ->
                        Text(
                            text = "• $bullet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                        )
                    }
                }
            }
        }
    }
}
