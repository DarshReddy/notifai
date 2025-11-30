package com.notif.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notif.ai.util.Priority

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val priority: Priority,
    val summary: String? = null,
    val category: NotificationCategory = NotificationCategory.BATCHED,
    val isRead: Boolean = false,
    val isSummarized: Boolean = false,
    val batchGroup: String? = null
)

enum class NotificationCategory {
    INSTANT,
    BATCHED,
    IGNORE
}
