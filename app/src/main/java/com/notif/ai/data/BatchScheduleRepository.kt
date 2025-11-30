package com.notif.ai.data

import kotlinx.coroutines.flow.Flow

class BatchScheduleRepository(private val dao: BatchScheduleDao) {
    fun getAll(): Flow<List<BatchSchedule>> = dao.getAllSchedules()
    suspend fun add(minutesFromMidnight: Int) {
        dao.insertSchedule(BatchSchedule(timeInMinutes = minutesFromMidnight))
    }

    suspend fun update(id: Int, minutesFromMidnight: Int, enabled: Boolean = true) {
        dao.updateSchedule(
            BatchSchedule(
                id = id,
                timeInMinutes = minutesFromMidnight,
                isEnabled = enabled
            )
        )
    }

    suspend fun delete(id: Int) {
        dao.deleteSchedule(id)
    }

    suspend fun clear() {
        dao.deleteAll()
    }
}
