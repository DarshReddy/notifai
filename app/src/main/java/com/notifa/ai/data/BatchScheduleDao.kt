package com.notifa.ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchScheduleDao {
    @Query("SELECT * FROM batch_schedules WHERE isEnabled = 1 ORDER BY timeInMinutes ASC")
    fun getEnabledSchedules(): Flow<List<BatchSchedule>>

    @Query("SELECT * FROM batch_schedules ORDER BY timeInMinutes ASC")
    fun getAllSchedules(): Flow<List<BatchSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BatchSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<BatchSchedule>)

    @Update
    suspend fun updateSchedule(schedule: BatchSchedule)

    @Query("DELETE FROM batch_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: Int)

    @Query("DELETE FROM batch_schedules")
    suspend fun deleteAll()
}

