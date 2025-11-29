package com.notifa.ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batch_schedules")
data class BatchSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timeInMinutes: Int, // Minutes from midnight (e.g., 420 = 7:00 AM)
    val isEnabled: Boolean = true,
    val emoji: String = "🌅"
)

