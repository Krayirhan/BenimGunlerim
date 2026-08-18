package com.benimgunlerim.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.benimgunlerim.R
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.components.layout.DetailScreenScaffold
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary

data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val license: String,
    val description: String,
)

private val LIBRARIES = listOf(
    OpenSourceLibrary(
        name = "AndroidX Jetpack Compose",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Modern toolkit for building native Android UI.",
    ),
    OpenSourceLibrary(
        name = "Konfetti Compose",
        author = "Dion Segijn",
        license = "ISC License",
        description = "Lightweight particle system for celebration confetti.",
    ),
    OpenSourceLibrary(
        name = "Material Icons / Symbols",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Delightful, beautifully crafted symbols for common actions.",
    ),
    OpenSourceLibrary(
        name = "Kotlin Coroutines & Flow",
        author = "JetBrains s.r.o.",
        license = "Apache License 2.0",
        description = "Asynchronous or non-blocking programming in Kotlin.",
    ),
    OpenSourceLibrary(
        name = "AndroidX Lifecycle (ViewModel & Runtime KTX)",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Lifecycle-aware components and Compose ViewModel integration.",
    ),
    OpenSourceLibrary(
        name = "AndroidX Navigation Compose",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Navigation framework for Jetpack Compose screens.",
    ),
    OpenSourceLibrary(
        name = "Room Persistence Library",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Robust SQLite object mapping database library.",
    ),
    OpenSourceLibrary(
        name = "AndroidX DataStore Preferences",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Typed, asynchronous key-value preference storage.",
    ),
    OpenSourceLibrary(
        name = "AndroidX WorkManager",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Deferrable, guaranteed background task scheduling.",
    ),
    OpenSourceLibrary(
        name = "Dagger Hilt",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Dependency injection for Android applications.",
    ),
)

@Composable
fun OssLicensesScreen(
    onNavigateBack: () -> Unit,
) {
    DetailScreenScaffold(
        title = stringResource(R.string.oss_licenses_title),
        onBack = onNavigateBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppTokens.Spacing.md, vertical = AppTokens.Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.sm),
        ) {
            items(LIBRARIES) { lib ->
                AppSurface(
                    radius = AppTokens.Radius.md,
                    padding = AppTokens.Spacing.cardInner,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xxs),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = lib.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = lib.license,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = BrandPrimary,
                            )
                        }
                        Text(
                            text = lib.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = lib.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
    }
}
