package com.notifa.ai.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.notifa.ai.ui.inbox.InboxScreen
import com.notifa.ai.ui.inbox.InboxViewModel
import com.notifa.ai.ui.navigation.Screen

@Composable
fun MainScreen(
    inboxViewModel: InboxViewModel,
    insightsViewModel: com.notifa.ai.ui.insights.InsightsViewModel,
    batchScheduleViewModel: com.notifa.ai.ui.settings.BatchScheduleViewModel,
    appCategoriesViewModel: com.notifa.ai.ui.settings.AppCategoriesViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(
                    BottomNavItem(Screen.Inbox, Icons.Default.Notifications),
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
            startDestination = Screen.Inbox.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Inbox.route) { InboxScreen(viewModel = inboxViewModel) }
            composable(Screen.Insights.route) { com.notifa.ai.ui.insights.InsightsScreen(viewModel = insightsViewModel) }
            composable(Screen.Settings.route) {
                com.notifa.ai.ui.settings.SettingsScreen(
                    onNavigateBatchSchedule = { navController.navigate(Screen.BatchSchedule.route) },
                    onNavigateAppCategories = { navController.navigate(Screen.AppCategories.route) }
                )
            }
            composable(Screen.BatchSchedule.route) { com.notifa.ai.ui.settings.BatchScheduleScreen(onBack = { navController.popBackStack() }, viewModel = batchScheduleViewModel) }
            composable(Screen.AppCategories.route) { com.notifa.ai.ui.settings.AppCategoriesScreen(onBack = { navController.popBackStack() }, viewModel = appCategoriesViewModel) }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector
)
