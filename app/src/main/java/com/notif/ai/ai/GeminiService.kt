package com.notif.ai.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.notif.ai.data.UserFeedback

data class NotificationData(
    val appName: String,
    val title: String,
    val text: String
)

object GeminiService {

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName = "gemini-2.5-flash")
    }

    suspend fun generateBatchSummary(notifications: List<NotificationData>): String {
        if (notifications.isEmpty()) return "No notifications."

        return try {
            val notifListString = notifications.joinToString("\n") {
                "- [${it.appName}] ${it.title}: ${it.text}"
            }

            val prompt = """
                You are a smart personal notification assistant.
                Summarize the following notifications into a single, natural, and concise paragraph (max 30 words).

                Guidelines:
                - Group related notifications (e.g., "3 messages from WhatsApp", "Updates from Instagram").
                - Prioritize "My Priority" content (people, urgent tasks, OTPs).
                - Mention important updates briefly.
                - Ignore promotional or spam content unless it's the only thing there.
                - Do not use bullet points. Write like a helpful personal assistant.
                - If there are messages from people, mention who they are if possible.

                Notifications:
                $notifListString

                Summary:
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: "Summary unavailable"
        } catch (e: Exception) {
            "Error generating summary"
        }
    }

    suspend fun categorizePriority(
        title: String,
        text: String,
        appName: String,
        userExamples: List<UserFeedback> = emptyList()
    ): String {
        return try {
            val userExamplesString = if (userExamples.isNotEmpty()) {
                val examples = userExamples.joinToString("\n") {
                    "Input: [${it.packageName}] ${it.title}: ${it.text}\nOutput: ${it.userCorrectedCategory}"
                }
                """
                User Specific Rules (Highest Priority - Learn from these):
                $examples
                """
            } else {
                ""
            }

            val prompt = """
                Role: You are an Android Notification Classifier. Classify the incoming notification into exactly one category.

                Categories:
                1. My Priority: Direct messages from people (WhatsApp, Telegram, SMS), OTPs, Rideshare/Food delivery real-time updates, Calendar events, Alarms.
                2. Important: Bank transactions, Order shipments, Work emails, System alerts, Breaking news.
                3. Promotional: Marketing, Discounts, "You might like", "Complete your purchase", Newsletters.
                4. Spam: Casino, obscure games, vague alerts, unwanted solicitation.
                5. Ignore: Persistent silent notifications, system logs, debugging info, "running in background", battery optimization alerts.

                $userExamplesString

                General Examples:
                Input: [com.whatsapp] Mom: Can you pick up milk?
                Output: My Priority

                Input: [com.amazon.mShop.android.shopping] Deal of the Day: 50% off on shoes!
                Output: Promotional

                Input: [com.google.android.gm] Bank of America: Your statement is ready.
                Output: Important
                
                Input: [com.uber] Your driver is 2 minutes away.
                Output: My Priority
                
                Input: [com.android.server.telecom] Incoming call from +1234567890
                Output: My Priority
                
                Input: [android] USB debugging connected
                Output: Ignore

                Input: [$appName] $title: $text
                Output:
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim() ?: "Important"

            when {
                result.contains("My Priority", ignoreCase = true) -> "My Priority"
                result.contains("Important", ignoreCase = true) -> "Important"
                result.contains("Promotional", ignoreCase = true) -> "Promotional"
                result.contains("Spam", ignoreCase = true) -> "Spam"
                result.contains("Ignore", ignoreCase = true) -> "Ignore"
                else -> "Important"
            }
        } catch (e: Exception) {
            "Important"
        }
    }
}
