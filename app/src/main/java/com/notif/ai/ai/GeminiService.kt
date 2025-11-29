package com.notif.ai.ai

import com.google.firebase.FirebaseApp
import com.google.firebase.vertexai.FirebaseVertexAI
import com.google.firebase.vertexai.type.generationConfig


class GeminiService {

    private val generativeModel =
        FirebaseVertexAI.getInstance(FirebaseApp.getInstance()).generativeModel(
            modelName = "gemini-1.5-flash-latest",
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
                result.contains("Promotional", ignoreCase = true) -> "Promotional"
                result.contains("Spam", ignoreCase = true) -> "Spam"
                else -> "Important"
            }
        } catch (e: Exception) {
            "Important"
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

