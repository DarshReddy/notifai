package com.notifa.ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE priority = :priority ORDER BY timestamp DESC")
    fun getByPriority(priority: String): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun countAll(): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE category = :category")
    suspend fun countByCategory(category: NotificationCategory): Int
}
