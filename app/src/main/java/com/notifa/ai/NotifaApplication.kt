package com.notifa.ai

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.notifa.ai.data.AppDatabase
import com.notifa.ai.data.NotificationRepository
import com.notifa.ai.worker.DailySummaryWorker
import java.util.concurrent.TimeUnit

class NotifaApplication : Application(), Configuration.Provider {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { NotificationRepository(database.notificationDao()) }

    override fun onCreate() {
        super.onCreate()
        setupDailySummaryWorker()
    }

    private fun setupDailySummaryWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailySummaryRequest =
            PeriodicWorkRequestBuilder<DailySummaryWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dailySummary",
            ExistingPeriodicWorkPolicy.KEEP,
            dailySummaryRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
