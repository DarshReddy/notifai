package com.notif.ai.data

import kotlinx.coroutines.flow.Flow

class AppPreferenceRepository(private val dao: AppPreferenceDao) {
    fun getAll(): Flow<List<AppPreference>> = dao.getAllApps()

    suspend fun get(packageName: String): AppPreference? = dao.getApp(packageName)
    suspend fun upsert(app: AppPreference) = dao.insertApp(app)
    suspend fun upsertAll(apps: List<AppPreference>) = dao.insertApps(apps)
}
