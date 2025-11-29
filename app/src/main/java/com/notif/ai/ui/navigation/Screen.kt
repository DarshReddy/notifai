package com.notif.ai.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Inbox : Screen("inbox", "Inbox")
    object Settings : Screen("settings", "Settings")
    object Insights : Screen("insights", "Insights")
    object BatchSchedule : Screen("batch_schedule", "Batch Schedule")
    object AppCategories : Screen("app_categories", "App Categories")
}
