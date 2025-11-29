package com.notifa.ai.data

import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAll()
    }

    fun getNotificationsByPriority(priority: String): Flow<List<NotificationEntity>> {
        return notificationDao.getByPriority(priority)
    }

    suspend fun insert(notification: NotificationEntity) {
        notificationDao.insert(notification)
    }

    suspend fun delete(notification: NotificationEntity) {
        notificationDao.deleteById(notification.id)
    }

    suspend fun deleteById(id: Int) {
        notificationDao.deleteById(id)
    }

    suspend fun deleteAll() {
        notificationDao.deleteAll()
    }

    suspend fun deleteOlderThan(timestamp: Long) {
        notificationDao.deleteOlderThan(timestamp)
    }

    suspend fun countAll(): Int = notificationDao.countAll()
    suspend fun countByCategory(category: NotificationCategory): Int = notificationDao.countByCategory(category)
}
