package com.notifa.ai.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

data class AppUsageData(
    val appName: String,
    val packageName: String,
    val notificationCount: Int,
    val usageTimeMillis: Long
)

object UsageStatsHelper {

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
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
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
                val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
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

        val screenEvents = mutableListOf<UsageEvents.Event>()
        while (events.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            events.getNextEvent(currentEvent)
            if (currentEvent.eventType == UsageEvents.Event.SCREEN_INTERACTIVE ||
                currentEvent.eventType == UsageEvents.Event.KEYGUARD_HIDDEN ||
                currentEvent.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE) {
                screenEvents.add(currentEvent)
            }
        }

        if (screenEvents.isEmpty()) {
            return 0L
        }

        val sessions = mutableListOf<Pair<Long, Long>>()
        var sessionStart: Long? = null

        screenEvents.sortBy { it.timeStamp }

        screenEvents.forEach { event ->
            if (event.eventType == UsageEvents.Event.SCREEN_INTERACTIVE || event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                if (sessionStart == null) {
                    sessionStart = event.timeStamp
                }
            } else if (event.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE) {
                if (sessionStart != null) {
                    sessions.add(Pair(sessionStart, event.timeStamp))
                    sessionStart = null
                }
            }
        }

        sessionStart?.let {
            sessions.add(Pair(it, end))
        }

        if (sessions.isEmpty()) return 0L

        // Merge overlapping intervals
        val mergedSessions = mutableListOf<Pair<Long, Long>>()
        sessions.sortBy { it.first }
        var (currentStart, currentEnd) = sessions.first()

        for (i in 1 until sessions.size) {
            val (nextStart, nextEnd) = sessions[i]
            if (nextStart < currentEnd) {
                currentEnd = maxOf(currentEnd, nextEnd)
            } else {
                mergedSessions.add(Pair(currentStart, currentEnd))
                currentStart = nextStart
                currentEnd = nextEnd
            }
        }
        mergedSessions.add(Pair(currentStart, currentEnd))

        return mergedSessions.sumOf { it.second - it.first }
    }

    fun formatMillisToTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis / (1000 * 60)) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
