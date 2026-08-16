@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.benimgunlerim.R
import com.benimgunlerim.ui.AppDestination
import com.benimgunlerim.ui.TestTags
import com.benimgunlerim.ui.achievements.AchievementsScreen
import com.benimgunlerim.ui.components.layout.AppTopBar
import com.benimgunlerim.ui.onboarding.OnboardingScreen
import com.benimgunlerim.ui.onboarding.OnboardingViewModel
import com.benimgunlerim.ui.plan.PlanScreen
import com.benimgunlerim.ui.progress.ProgressScreen
import com.benimgunlerim.ui.routines.RoutineDetailScreen
import com.benimgunlerim.ui.routines.RoutinesScreen
import com.benimgunlerim.ui.settings.SettingsScreen
import com.benimgunlerim.ui.shop.ShopScreen
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.today.TodayRoute

enum class Destination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Today(
        route = AppDestination.Today.route,
        labelRes = R.string.nav_today,
        icon = Icons.Outlined.CheckCircle,
        selectedIcon = Icons.Filled.CheckCircle,
    ),
    Plan(
        route = AppDestination.Plan.route,
        labelRes = R.string.nav_plan,
        icon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Filled.CalendarMonth,
    ),
    Routines(
        route = AppDestination.Routines.route,
        labelRes = R.string.nav_routines,
        icon = Icons.Outlined.LocalFireDepartment,
        selectedIcon = Icons.Filled.LocalFireDepartment,
    ),
    Progress(
        route = AppDestination.Progress.route,
        labelRes = R.string.nav_progress,
        icon = Icons.Outlined.BarChart,
        selectedIcon = Icons.Filled.BarChart,
    ),
    Settings(
        route = AppDestination.Settings.route,
        labelRes = R.string.nav_settings,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
    ),
}

@Composable
fun BenimGunlerimApp(
    requestedStartRoute: String? = null,
    forceOnboardingCompleted: Boolean = false,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userPreferences by viewModel.preferences.collectAsState()
    val effectiveCompleted = forceOnboardingCompleted || userPreferences.onboardingCompleted

    val startDestination = when {
        !effectiveCompleted -> AppDestination.Onboarding.route
        requestedStartRoute == AppDestination.Plan.route -> AppDestination.Plan.route
        requestedStartRoute == AppDestination.Routines.route -> AppDestination.Routines.route
        else -> AppDestination.Today.route
    }

    val showBottomBar = currentRoute in Destination.entries.map { it.route }

    LaunchedEffect(requestedStartRoute, effectiveCompleted) {
        if (effectiveCompleted && requestedStartRoute != null) {
            val target = when (requestedStartRoute) {
                AppDestination.Plan.route -> AppDestination.Plan.route
                AppDestination.Routines.route -> AppDestination.Routines.route
                else -> null
            }
            if (target != null && currentRoute != target) {
                navController.navigate(target) {
                    popUpTo(Destination.Today.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (showBottomBar) {
                val todayFormattedDate = remember {
                    java.time.LocalDate.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("d MMMM, EEEE", java.util.Locale("tr")),
                    )
                }
                val (topBarTitle, topBarSubtitle) = when (currentRoute) {
                    Destination.Today.route    -> "Benim Günlerim" to todayFormattedDate
                    Destination.Plan.route     -> "Plan" to "Günlerini düzenle"
                    Destination.Routines.route -> "Rutinler" to "Ritmini koru"
                    Destination.Progress.route -> "İstatistik" to "Gelişimini gör"
                    Destination.Settings.route -> "Ayarlar" to "Deneyimini düzenle"
                    else                       -> "Benim Günlerim" to null
                }

                AppTopBar(
                    title = topBarTitle,
                    subtitle = topBarSubtitle,
                    showProfile = true,
                    showNotification = true,
                    hasNotificationBadge = false,
                    onProfileClick = {
                        navController.navigate(Destination.Settings.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNotificationClick = {
                        navController.navigate(Destination.Settings.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = com.benimgunlerim.ui.theme.Divider,
                    ),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    Destination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            modifier = Modifier.testTag(
                                when (destination) {
                                    Destination.Today    -> TestTags.BottomNavToday
                                    Destination.Plan     -> TestTags.BottomNavPlan
                                    Destination.Routines -> TestTags.BottomNavRoutines
                                    Destination.Progress -> TestTags.BottomNavProgress
                                    Destination.Settings -> TestTags.BottomNavSettings
                                },
                            ),
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Destination.Today.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = {
                                Text(
                                    stringResource(destination.labelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.icon,
                                    contentDescription = stringResource(destination.labelRes),
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = com.benimgunlerim.ui.theme.BrandPrimary,
                                selectedTextColor = com.benimgunlerim.ui.theme.BrandPrimary,
                                indicatorColor = com.benimgunlerim.ui.theme.BrandPrimarySoft,
                                unselectedIconColor = com.benimgunlerim.ui.theme.TextSecondary,
                                unselectedTextColor = com.benimgunlerim.ui.theme.TextSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) },
        ) {
            composable(AppDestination.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { needId, intensityId, routineNames, taskTitle ->
                        viewModel.completeOnboarding(needId, intensityId, routineNames, taskTitle)
                        navController.navigate(Destination.Today.route) {
                            popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Destination.Today.route) {
                TodayRoute(
                    onNavigateToRoutines = {
                        navController.navigate(Destination.Routines.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPlan = {
                        navController.navigate(Destination.Plan.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Destination.Settings.route) {
                            popUpTo(Destination.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenRoutineDetail = { routineId ->
                        navController.navigate(AppDestination.RoutineDetailPattern.createRoute(routineId))
                    },
                )
            }
            composable(Destination.Plan.route) { PlanScreen() }
            composable(Destination.Routines.route) {
                RoutinesScreen(onOpenRoutineDetail = { routineId ->
                    navController.navigate(AppDestination.RoutineDetailPattern.createRoute(routineId))
                })
            }
            composable(AppDestination.RoutineDetailPattern.route) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString(
                    AppDestination.RoutineDetailPattern.ARG_ROUTINE_ID,
                ) ?: return@composable
                RoutineDetailScreen(
                    routineId = routineId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destination.Progress.route) {
                ProgressScreen(
                    onNavigateToAchievements = { navController.navigate(AppDestination.Achievements.route) },
                )
            }
            composable(AppDestination.Achievements.route) {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }
            composable(AppDestination.Shop.route) {
                ShopScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(AppDestination.Onboarding.route)
                    },
                )
            }
        }
    }
}
