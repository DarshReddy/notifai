package com.notif.ai.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

object GeminiService {

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName = "gemini-2.5-flash")
    }

    suspend fun summarizeNotification(title: String, text: String, appName: String): String {
        return try {
            val prompt = """
                Summarize this notification in one concise sentence (max 15 words):
                
                App: $appName
                Title: $title
                Content: $text
                
                Provide ONLY the summary, no extra text.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: "Summary unavailable"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun categorizePriority(title: String, text: String, appName: String): String {
        return try {
            val prompt = """
                Analyze this notification from '$appName' and respond with ONLY ONE of the following categories: My Priority, Important, Promotional, Spam

                Title: $title
                Content: $text

                - My Priority: Urgent, from a person, time-sensitive alerts.
                - Important: General messages, updates, and news.
                - Promotional: Advertisements, sales, and marketing messages.
                - Spam: Unsolicited, irrelevant, or unwanted messages.

                Response (one category only):
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim() ?: "Important"

            when {
                result.contains("My Priority", ignoreCase = true) -> "My Priority"
                result.contains("Important", ignoreCase = true) -> "Important"
                result.contains("Promotional", ignoreCase = true) -> "Promotional"
                result.contains("Spam", ignoreCase = true) -> "Spam"
                else -> "Important" // Default to Important instead of None
            }
        } catch (e: Exception) {
            "Important" // Default on error
        }
    }
}
