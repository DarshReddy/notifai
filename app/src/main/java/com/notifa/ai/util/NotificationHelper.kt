package com.notifa.ai.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.notifa.ai.MainActivity
import com.notifa.ai.R

object NotificationHelper {

    private const val CHANNEL_ID = "notifa_summary_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifa Summary"
            val descriptionText = "Daily notification summary and stats"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSummaryNotification(
        context: Context,
        totalCount: Int,
        batchedCount: Int,
        instantCount: Int,
        nextBatchTime: String,
        batchSummary: String? = null
    ) {
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

        val summaryLine = if (!batchSummary.isNullOrBlank()) "\n🧠 Summary: $batchSummary" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📊 Today's Summary")
            .setContentText("$totalCount notifications • $batchedCount batched • Next batch in $nextBatchTime")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "📥 Total: $totalCount notifications\n🔕 Batched: $batchedCount\n⚡ Instant: $instantCount\n⏰ Next batch: $nextBatchTime$summaryLine"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Makes it sticky
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun updateSummaryNotification(
        context: Context,
        totalCount: Int,
        batchedCount: Int,
        instantCount: Int,
        nextBatchTime: String,
        batchSummary: String? = null
    ) {
        showSummaryNotification(context, totalCount, batchedCount, instantCount, nextBatchTime, batchSummary)
    }

    fun cancelSummaryNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
