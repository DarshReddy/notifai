package com.notif.ai.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notif.ai.NotifaApplication
import java.util.Calendar

class DailySummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as NotifaApplication).repository
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.timeInMillis

            repository.deleteOlderThan(yesterday)

            // In a real app, you would fetch the notifications,
            // send them to the AI client, and store the summary.
            // For now, we'll just log a message.
            Log.d("DailySummaryWorker", "Daily summary worker ran.")

            Result.success()
        } catch (e: Exception) {
            Log.e("DailySummaryWorker", "Error in daily summary worker", e)
            Result.failure()
        }
    }
}
