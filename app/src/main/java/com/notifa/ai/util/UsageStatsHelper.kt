package com.notifa.ai.util

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

data class AppUsageData(
    val appName: String,
    val packageName: String,
    val notificationCount: Int,
    val usageTimeMillis: Long
)

object UsageStatsHelper {

    private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

    private fun dayStartMillis(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val zone = ZoneId.systemDefault()
                LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
            } catch (_: Throwable) {
                legacyDayStart()
            }
        } else legacyDayStart()
    }

    private fun legacyDayStart(): Long {
        val now = System.currentTimeMillis()
        val tz = TimeZone.getDefault()
        val offset = tz.getOffset(now)
        return now - ((now + offset) % MILLIS_PER_DAY)
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calendar.timeInMillis,
            System.currentTimeMillis()
        )
        return stats != null && stats.isNotEmpty()
    }

    fun getTopApps(context: Context, notificationCounts: Map<String, Int>): List<AppUsageData> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager
        val start = dayStartMillis()
        val end = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end) ?: emptyList()
        val appDataList = mutableListOf<AppUsageData>()
        val seen = HashSet<String>()
        stats.forEach { usageStats ->
            val packageName = usageStats.packageName
            if (packageName == "android" || packageName.contains("systemui") || usageStats.totalTimeInForeground <= 0) return@forEach
            if (!seen.add(packageName)) return@forEach
            val notifCount = notificationCounts[packageName] ?: 0
            if (notifCount <= 0) return@forEach
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                appDataList.add(
                    AppUsageData(
                        appName = appName,
                        packageName = packageName,
                        notificationCount = notifCount,
                        usageTimeMillis = usageStats.totalTimeInForeground
                    )
                )
            } catch (_: PackageManager.NameNotFoundException) { }
        }
        return appDataList.sortedByDescending { it.notificationCount }.take(5)
    }

    fun getTotalScreenTime(context: Context): Long {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start = dayStartMillis()
        val end = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(start, end)
        var totalScreenTime = 0L
        val eventLog = mutableListOf<UsageEvents.Event>()
        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                eventLog.add(event)
            }
        }

        for (i in 0 until eventLog.size - 1) {
            val currentEvent = eventLog[i]
            val nextEvent = eventLog[i + 1]
            if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND &&
                (nextEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND || nextEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND)
            ) {
                val timeDiff = nextEvent.timeStamp - currentEvent.timeStamp
                totalScreenTime += timeDiff
            }
        }
        return totalScreenTime
    }

    fun formatMillisToTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis / (1000 * 60)) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
