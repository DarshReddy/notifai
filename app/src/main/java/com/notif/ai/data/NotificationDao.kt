package com.notif.ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    // Exclude IGNORE category from default views if any sneak in
    @Query("SELECT * FROM notifications WHERE category != 'IGNORE' ORDER BY timestamp DESC")
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

    @Query("SELECT COUNT(*) FROM notifications WHERE priority = :priority")
    fun countByPriority(priority: String): Flow<Int>

    @Query("SELECT * FROM notifications WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getAllBatchedNotifications(category: NotificationCategory = NotificationCategory.BATCHED): List<NotificationEntity>

    @Query("UPDATE notifications SET isSummarized = 1 WHERE id IN (:ids)")
    suspend fun markAsSummarized(ids: List<Int>)
}
