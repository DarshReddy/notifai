package com.notif.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_feedback")
data class UserFeedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val predictedCategory: String,
    val userCorrectedCategory: String, // The category the user moved it to
    val timestamp: Long = System.currentTimeMillis()
)
