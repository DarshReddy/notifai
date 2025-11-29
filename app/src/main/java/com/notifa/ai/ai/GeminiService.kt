package com.notifa.ai.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.notifa.ai.BuildConfig

class GeminiService {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 200
        }
    )

    suspend fun summarizeNotification(title: String, text: String, appName: String): String {
        return try {
            val prompt = """
                Summarize this notification in one concise sentence (max 15 words):
                
                App: $appName
                Title: $title
                Content: $text
                
                Provide ONLY the summary, no extra text.
            """.trimIndent()

            val response = model.generateContent(prompt)
            response.text?.trim() ?: "Summary unavailable"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun summarizeBatch(notifications: List<String>): String {
        return try {
            val notificationList = notifications.joinToString("\n") { "• $it" }
            val prompt = """
                You have ${notifications.size} notifications. Create a brief summary (max 30 words):
                
                $notificationList
                
                Provide ONLY the summary highlighting key points.
            """.trimIndent()

            val response = model.generateContent(prompt)
            response.text?.trim() ?: "Multiple notifications received"
        } catch (e: Exception) {
            "${notifications.size} notifications received"
        }
    }

    suspend fun categorizePriority(title: String, text: String): String {
        return try {
            val prompt = """
                Analyze this notification and respond with ONLY ONE WORD: HIGH, NORMAL, or LOW
                
                Title: $title
                Content: $text
                
                HIGH = urgent, time-sensitive, important person
                NORMAL = regular updates, messages
                LOW = promotions, spam, non-urgent
                
                Response (one word only):
            """.trimIndent()

            val response = model.generateContent(prompt)
            val result = response.text?.trim()?.uppercase() ?: "NORMAL"

            when {
                result.contains("HIGH") -> "HIGH"
                result.contains("LOW") -> "LOW"
                else -> "NORMAL"
            }
        } catch (e: Exception) {
            "NORMAL"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GeminiService? = null

        fun getInstance(): GeminiService {
            return INSTANCE ?: synchronized(this) {
                val instance = GeminiService()
                INSTANCE = instance
                instance
            }
        }
    }
}

