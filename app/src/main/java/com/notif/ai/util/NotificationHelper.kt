package com.notif.ai.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.notif.ai.MainActivity
import com.notif.ai.R
import com.notif.ai.data.NotificationEntity

object NotificationHelper {

    private const val CHANNEL_ID = "notifa_summary_channel"
    private const val NOTIFICATION_ID = 1001

    // Cache the latest data to allow UI updates without re-fetching
    private var lastTotalCount: Int = 0
    private var lastCategorizedNotifications: Map<String, List<NotificationEntity>> = emptyMap()
    private var lastBatchSummary: String? = null

    // Track expanded states
    private val expandedCategories = mutableSetOf<String>("My Priority")

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifa Summary"
            val descriptionText = "Daily notification summary and stats"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSummaryNotification(
        context: Context,
        totalCount: Int,
        categorizedNotifications: Map<String, List<NotificationEntity>>,
        batchSummary: String?
    ) {
        // Update cache
        lastTotalCount = totalCount
        lastCategorizedNotifications = categorizedNotifications
        lastBatchSummary = batchSummary

        renderNotification(context)
    }

    private fun renderNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationLayout =
            RemoteViews(context.packageName, R.layout.custom_notification_layout)

        notificationLayout.setTextViewText(
            R.id.summary_text,
            "Summary available • $lastTotalCount Notifications"
        )

        if (lastBatchSummary != null) {
            notificationLayout.setTextViewText(R.id.summary_text, lastBatchSummary)
        }

        // Clear previous category views
        notificationLayout.removeAllViews(R.id.notification_categories_container)

        lastCategorizedNotifications.forEach { (category, notifications) ->
            val categoryLayout =
                RemoteViews(context.packageName, R.layout.notification_category_item)
            categoryLayout.setTextViewText(R.id.category_name, category)
            categoryLayout.setTextViewText(R.id.category_count, notifications.size.toString())
            categoryLayout.setImageViewResource(R.id.category_icon, getCategoryIcon(category))
            categoryLayout.setOnClickPendingIntent(
                R.id.category_header,
                getCategoryClickIntent(context, category)
            )

            val isExpanded = expandedCategories.contains(category)
            if (isExpanded) {
                categoryLayout.setViewVisibility(R.id.notifications_container, View.VISIBLE)
                categoryLayout.setImageViewResource(
                    R.id.category_expand_icon,
                    R.drawable.ic_expand_less
                )
            } else {
                categoryLayout.setViewVisibility(R.id.notifications_container, View.GONE)
                categoryLayout.setImageViewResource(
                    R.id.category_expand_icon,
                    R.drawable.ic_expand_more
                )
            }


            notifications.forEach { notification ->
                val notificationItemLayout =
                    RemoteViews(context.packageName, R.layout.notification_item)
                notificationItemLayout.setTextViewText(
                    R.id.notification_item_title,
                    notification.title
                )
                notificationItemLayout.setTextViewText(
                    R.id.notification_item_content,
                    notification.text
                )
                categoryLayout.addView(R.id.notifications_container, notificationItemLayout)
            }
            notificationLayout.addView(R.id.notification_categories_container, categoryLayout)
        }


        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setSilent(true)
            .setOngoing(true) // Sticky

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun onCategoryClick(context: Context, category: String) {
        if (expandedCategories.contains(category)) {
            expandedCategories.remove(category)
        } else {
            expandedCategories.clear()
            expandedCategories.add(category)
        }
        renderNotification(context)
    }

    private fun getCategoryClickIntent(context: Context, category: String): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = "com.notif.ai.CATEGORY_CLICK"
            putExtra("category", category)
        }
        // Use a unique request code based on category hash to ensure distinct PendingIntents
        return PendingIntent.getBroadcast(
            context,
            category.hashCode(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun getCategoryIcon(category: String): Int {
        return when (category) {
            "My Priority" -> R.drawable.ic_my_priority
            "Important" -> R.drawable.ic_important
            "Promotional" -> R.drawable.ic_promotional
            else -> R.drawable.ic_category_placeholder
        }
    }

    fun cancelSummaryNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
