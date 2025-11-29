package com.notifa.ai.ai

interface SummaryProvider {
    suspend fun getSummary(notifications: List<String>): String
}
