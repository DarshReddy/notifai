package com.notifa.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreference(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: NotificationCategory,
    val isEnabled: Boolean = true,
    val iconUri: String? = null
)

