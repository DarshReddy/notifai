package com.notif.ai.ui.insights

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notif.ai.data.NotificationCategory
import com.notif.ai.data.NotificationRepository
import com.notif.ai.util.AppUsageData
import com.notif.ai.util.UsageStatsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale.getDefault

data class InsightsData(
    val totalNotifications: Int,
    val batchedCount: Int,
    val instantCount: Int,
    val screenTime: String,
    val interruptionReduction: Int,
    val topApps: List<AppUsageData>
)

class InsightsViewModel(
    private val repository: NotificationRepository,
    private val context: Context
) : ViewModel() {

    private val _insightsData = MutableStateFlow<InsightsData?>(null)
    val insightsData: StateFlow<InsightsData?> = _insightsData.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            repository.getAllNotifications().collect { notifications ->
                // Get today's notifications
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                val todayStart = calendar.timeInMillis

                val todayNotifications = notifications.filter { it.timestamp >= todayStart }

                val totalCount = todayNotifications.size
                val batchedCount =
                    todayNotifications.count { it.category == NotificationCategory.BATCHED }
                val instantCount =
                    todayNotifications.count { it.category == NotificationCategory.INSTANT }

                // Calculate interruption reduction (batched notifications don't interrupt)
                val reductionPercent = if (totalCount > 0) {
                    ((batchedCount.toFloat() / totalCount) * 100).toInt()
                } else 0

                // Get screen time
                val screenTimeMillis = UsageStatsHelper.getTotalScreenTime(context)
                val screenTimeFormatted = UsageStatsHelper.formatMillisToTime(screenTimeMillis)

                // Get notification counts per app
                val notificationCounts = todayNotifications
                    .groupBy { it.packageName }
                    .mapValues { it.value.size }

                // Get top apps
                val topApps = if (UsageStatsHelper.hasUsageStatsPermission(context)) {
                    UsageStatsHelper.getTopApps(context, notificationCounts)
                } else {
                    // Fallback to just notification counts
                    notificationCounts.entries
                        .sortedByDescending { it.value }
                        .take(5)
                        .map { entry ->
                            val appName =
                                entry.key.split(".").lastOrNull()?.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
                                } ?: entry.key
                            AppUsageData(appName, entry.key, entry.value, 0)
                        }
                }

                _insightsData.value = InsightsData(
                    totalNotifications = totalCount,
                    batchedCount = batchedCount,
                    instantCount = instantCount,
                    screenTime = screenTimeFormatted,
                    interruptionReduction = reductionPercent,
                    topApps = topApps
                )
            }
        }
    }
}

