package com.notif.ai.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notif.ai.ai.GeminiService
import com.notif.ai.ai.NotificationData
import com.notif.ai.data.AppDatabase
import com.notif.ai.data.AppPreferenceRepository
import com.notif.ai.data.NotificationCategory
import com.notif.ai.data.NotificationDao
import com.notif.ai.data.NotificationEntity
import com.notif.ai.data.UserFeedbackDao
import com.notif.ai.util.NotificationHelper
import com.notif.ai.util.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotifListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var notificationDao: NotificationDao
    private lateinit var appPrefRepo: AppPreferenceRepository
    private lateinit var userFeedbackDao: UserFeedbackDao

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        notificationDao = db.notificationDao()
        appPrefRepo = AppPreferenceRepository(db.appPreferenceDao())
        userFeedbackDao = db.userFeedbackDao()
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

            if (title.isNullOrEmpty() && text.isNullOrEmpty()) return

            scope.launch {
                // Fetch user feedback for this app to help Gemini learn
                val userFeedback = userFeedbackDao.getFeedbackForApp(packageName)
                
                val priorityString =
                    GeminiService.categorizePriority(
                        title ?: "",
                        text ?: "",
                        packageName,
                        userFeedback
                    )

                val priority = when (priorityString) {
                    "My Priority" -> Priority.MY_PRIORITY
                    "Important" -> Priority.IMPORTANT
                    "Promotional" -> Priority.PROMOTIONAL
                    "Spam" -> Priority.SPAM
                    else -> Priority.IMPORTANT
                }

                val pref = appPrefRepo.get(packageName)
                val category = pref?.category ?: when {
                    priority == Priority.MY_PRIORITY -> NotificationCategory.INSTANT
                    packageName.contains("dialer") || packageName.contains("phone") -> NotificationCategory.INSTANT
                    packageName.contains("alarm") || packageName.contains("calendar") -> NotificationCategory.INSTANT
                    else -> NotificationCategory.BATCHED
                }

                val notificationEntity = NotificationEntity(
                    packageName = packageName,
                    title = title ?: "",
                    text = text ?: "",
                    timestamp = timestamp,
                    priority = priority,
                    category = category,
                    isRead = false,
                    isSummarized = false
                )

                notificationDao.insert(notificationEntity)

                if (category == NotificationCategory.BATCHED) {
                    cancelNotification(sbn.key)
                    updateBatchSummary()
                } else {
                    updateBatchSummary()
                }
            }
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private suspend fun updateBatchSummary() {
        val notifications = notificationDao.getAllBatchedNotifications()

        if (notifications.isEmpty()) {
            NotificationHelper.cancelSummaryNotification(applicationContext)
            return
        }

        val categorizedNotifications = notifications.groupBy {
            when (it.priority) {
                Priority.MY_PRIORITY -> "My Priority"
                Priority.PROMOTIONAL -> "Promotional"
                Priority.SPAM -> "Spam"
                Priority.IMPORTANT -> "Important"
            }
        }

        val summaryInput = notifications.take(20).map {
            NotificationData(
                appName = getAppName(it.packageName),
                title = it.title,
                text = it.text
            )
        }

        val batchSummary = GeminiService.generateBatchSummary(summaryInput)

        NotificationHelper.showSummaryNotification(
            applicationContext,
            notifications.size,
            categorizedNotifications,
            batchSummary
        )

        notificationDao.markAsSummarized(notifications.map { it.id })
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
