@file:Suppress("SpellCheckingInspection")
package com.benimgunlerim.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.benimgunlerim.ui.achievements.AchievementsScreen
import com.benimgunlerim.ui.onboarding.OnboardingScreen
import com.benimgunlerim.ui.onboarding.OnboardingViewModel
import com.benimgunlerim.ui.plan.PlanScreen
import com.benimgunlerim.ui.progress.ProgressScreen
import com.benimgunlerim.ui.routines.RoutineDetailScreen
import com.benimgunlerim.ui.routines.RoutinesScreen
import com.benimgunlerim.ui.settings.SettingsScreen
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.today.TodayScreen

private enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Today(AppDestination.Today.route, "Bugün", Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle),
    Plan(AppDestination.Plan.route, "Plan", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    Routines(AppDestination.Routines.route, "Rutinler", Icons.Outlined.LocalFireDepartment, Icons.Filled.LocalFireDepartment),
    Progress(AppDestination.Progress.route, "İlerlemen", Icons.Outlined.BarChart, Icons.Filled.BarChart),
    Settings(AppDestination.Settings.route, "Ayarlar", Icons.Outlined.Settings, Icons.Filled.Settings),
}

@Composable
fun BenimGunlerimApp(
    requestedStartRoute: String? = null,
    forceOnboardingCompleted: Boolean = false,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsState()
    val navController = rememberNavController()
    val startDestination = if (forceOnboardingCompleted || preferences.onboardingCompleted) {
        Destination.Today.route
    } else {
        AppDestination.Onboarding.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = Destination.entries.any { it.route == currentRoute }

    LaunchedEffect(preferences.onboardingCompleted, requestedStartRoute) {
        if (preferences.onboardingCompleted && requestedStartRoute != null) {
            val destination = Destination.entries.firstOrNull { it.route == requestedStartRoute }
                ?: return@LaunchedEffect
            navController.navigate(destination.route) {
                popUpTo(Destination.Today.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    Destination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.1f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                            label = "navScale",
                        )
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
                                    destination.label,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            icon = {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                                        contentDescription = destination.label,
                                        modifier = Modifier.scale(scale),
                                    )
                                    if (selected) {
                                        Surface(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .offset(x = 2.dp, y = (-2).dp),
                                            shape = CircleShape,
                                            color = CandyPrimary,
                                            content = {},
                                        )
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CandyPrimary,
                                selectedTextColor = CandyPrimary,
                                indicatorColor = CandyPrimary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            composable(Destination.Today.route) { TodayScreen() }
            composable(Destination.Plan.route) { PlanScreen() }
            composable(Destination.Routines.route) {
                RoutinesScreen(onNavigateToDetail = { routineId ->
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
                    onOpenAchievements = { navController.navigate(AppDestination.Achievements.route) },
                )
            }
            composable(AppDestination.Achievements.route) {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}
