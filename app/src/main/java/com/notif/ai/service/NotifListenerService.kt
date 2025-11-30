package com.notif.ai.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notif.ai.ai.GeminiService
import com.notif.ai.data.AppDatabase
import com.notif.ai.data.AppPreferenceRepository
import com.notif.ai.data.NotificationCategory
import com.notif.ai.data.NotificationEntity
import com.notif.ai.data.NotificationRepository
import com.notif.ai.util.NotificationHelper
import com.notif.ai.util.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotifListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var repository: NotificationRepository
    private lateinit var appPrefRepo: AppPreferenceRepository

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        repository = NotificationRepository(db.notificationDao())
        appPrefRepo = AppPreferenceRepository(db.appPreferenceDao())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.notification?.let { notification ->
            val packageName = sbn.packageName

            // Skip system and our own app notifications
            if (packageName == "android" || packageName.contains("systemui") || packageName == applicationContext.packageName) {
                return
            }

            val title = notification.extras.getString("android.title", "")
            val text = notification.extras.getString("android.text", "")
            val timestamp = sbn.postTime

            if (title.isEmpty() && text.isEmpty()) return

            // Simple classification rules
            scope.launch {
                val priorityString = GeminiService.categorizePriority(title, text, packageName)

                val priority = when (priorityString) {
                    "My Priority" -> Priority.MY_PRIORITY
                    "Promotional" -> Priority.PROMOTIONAL
                    "Spam" -> Priority.SPAM
                    else -> return@launch
                }

                val pref = appPrefRepo.get(packageName)
                val category = pref?.category ?: when {
                    packageName.contains("dialer") || packageName.contains("phone") -> NotificationCategory.INSTANT
                    packageName.contains("alarm") || packageName.contains("calendar") -> NotificationCategory.INSTANT
                    else -> NotificationCategory.BATCHED
                }
                val notificationEntity = NotificationEntity(
                    packageName = packageName,
                    title = title,
                    text = text,
                    timestamp = timestamp,
                    priority = priority,
                    category = category,
                    isRead = false,
                    isSummarized = false
                )
                repository.insert(notificationEntity)
                if (category == NotificationCategory.BATCHED) cancelNotification(sbn.key)
                // Make sure we set ongoing (sticky) when we post the summary
                val total = repository.countAll()
                if (total > 0) {
                    val batchedCount = repository.countByCategory(NotificationCategory.BATCHED)
                    val instantCount = repository.countByCategory(NotificationCategory.INSTANT)
                    val next = "2h"
                    val summaryLine =
                        if (batchedCount >= 3) "$batchedCount batched notifications" else null
                    NotificationHelper.updateSummaryNotification(
                        applicationContext,
                        total,
                        batchedCount,
                        instantCount,
                        next,
                        summaryLine
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
