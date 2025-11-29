package com.notifa.ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPreferenceDao {
    @Query("SELECT * FROM app_preferences ORDER BY appName ASC")
    fun getAllApps(): Flow<List<AppPreference>>

    @Query("SELECT * FROM app_preferences WHERE category = :category")
    fun getAppsByCategory(category: NotificationCategory): Flow<List<AppPreference>>

    @Query("SELECT * FROM app_preferences WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): AppPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppPreference)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppPreference>)

    @Update
    suspend fun updateApp(app: AppPreference)

    @Query("DELETE FROM app_preferences WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)
}

