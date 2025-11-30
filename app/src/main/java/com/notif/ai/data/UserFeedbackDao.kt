package com.notif.ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feedback: UserFeedback)

    @Query("SELECT * FROM user_feedback ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentFeedback(): List<UserFeedback>

    @Query("SELECT * FROM user_feedback WHERE packageName = :packageName ORDER BY timestamp DESC LIMIT 5")
    suspend fun getFeedbackForApp(packageName: String): List<UserFeedback>
}
