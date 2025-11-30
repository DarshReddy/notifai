package com.notif.ai.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.notif.ai.ui.feedback.FeedbackScreen
import com.notif.ai.ui.home.HomeScreen
import com.notif.ai.ui.home.HomeViewModel
import com.notif.ai.ui.insights.InsightsScreen
import com.notif.ai.ui.insights.InsightsViewModel
import com.notif.ai.ui.navigation.Screen
import com.notif.ai.ui.settings.AppCategoriesScreen
import com.notif.ai.ui.settings.AppCategoriesViewModel
import com.notif.ai.ui.settings.BatchScheduleScreen
import com.notif.ai.ui.settings.BatchScheduleViewModel
import com.notif.ai.ui.settings.SettingsScreen

@Composable
fun MainScreen(
    notificationListViewModel: NotificationListViewModel,
    homeViewModel: HomeViewModel,
    insightsViewModel: InsightsViewModel,
    batchScheduleViewModel: BatchScheduleViewModel,
    appCategoriesViewModel: AppCategoriesViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(
                    BottomNavItem(Screen.Home, Icons.Default.Home),
                    BottomNavItem(Screen.Insights, Icons.Default.BarChart),
                    BottomNavItem(Screen.Settings, Icons.Default.Settings)
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.screen.title) },
                        label = { Text(item.screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    notificationListViewModel = notificationListViewModel
                )
            }
            composable(Screen.Insights.route) {
                InsightsScreen(viewModel = insightsViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBatchSchedule = { navController.navigate(Screen.BatchSchedule.route) },
                    onNavigateAppCategories = { navController.navigate(Screen.AppCategories.route) },
                    onNavigateFeedback = { navController.navigate(Screen.Feedback.route) }
                )
            }
            composable(Screen.BatchSchedule.route) {
                BatchScheduleScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = batchScheduleViewModel
                )
            }
            composable(Screen.AppCategories.route) {
                AppCategoriesScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = appCategoriesViewModel
                )
            }
            composable(Screen.Feedback.route) {
                FeedbackScreen(
                    viewModel = insightsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector
)
