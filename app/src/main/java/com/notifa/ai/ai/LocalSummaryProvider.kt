package com.notifa.ai.ai

class LocalSummaryProvider : SummaryProvider {
    override suspend fun getSummary(notifications: List<String>): String {
        val count = notifications.size
        val apps = notifications.map { it.split(":")[0] }.distinct()
        return "You have $count notifications from ${apps.joinToString(", ")}."
    }
}
