package com.notifa.ai.ui.inbox

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifa.ai.ai.GeminiService
import com.notifa.ai.data.NotificationCategory
import com.notifa.ai.data.NotificationEntity
import com.notifa.ai.data.NotificationRepository
import com.notifa.ai.data.PreferencesManager
import com.notifa.ai.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InboxViewModel(
    private val repository: NotificationRepository,
    private val geminiService: GeminiService,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModel() {

    val notifications = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lastBatchTime = preferencesManager.lastBatchTime
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private fun buildBatchSummary(batched: List<NotificationEntity>): String? {
        if (batched.size < 3) return null
        val apps = batched.map { it.packageName.split('.').last() }
        val topCounts = apps.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.take(3)
        val appPart = topCounts.joinToString { "${it.key}(${it.value})" }
        return "${batched.size} notifications from $appPart"
    }

    init {
        // Update sticky notification when notifications change
        viewModelScope.launch {
            notifications.collect { notifList ->
                val totalCount = notifList.size
                val batchedList = notifList.filter { it.category == NotificationCategory.BATCHED }
                val batchedCount = batchedList.size
                val instantCount = notifList.count { it.category == NotificationCategory.INSTANT }

                // Calculate next batch time
                val nextBatchTime = calculateNextBatchTime()
                val batchSummary = buildBatchSummary(batchedList)

                if (totalCount > 0) {
                    NotificationHelper.updateSummaryNotification(
                        context,
                        totalCount,
                        batchedCount,
                        instantCount,
                        nextBatchTime,
                        batchSummary
                    )
                } else {
                    NotificationHelper.cancelSummaryNotification(context)
                }
            }
        }
    }

    private fun calculateNextBatchTime(): String {
        val nextBatchMillis = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours
        val diff = nextBatchMillis - System.currentTimeMillis()
        val minutes = (diff / 1000 / 60).toInt()
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
    }

    fun summarizeNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val summary = geminiService.summarizeNotification(
                    title = notification.title,
                    text = notification.text,
                    appName = notification.packageName
                )

                // Update notification with summary
                repository.insert(
                    notification.copy(
                        summary = summary,
                        isSummarized = true
                    )
                )
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            // Update notification as read
            val notification = notifications.value.find { it.id == notificationId }
            notification?.let {
                repository.insert(it.copy(isRead = true))
            }
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            val notification = notifications.value.find { it.id == notificationId }
            notification?.let {
                repository.delete(it)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
