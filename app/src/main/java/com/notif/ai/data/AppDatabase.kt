package com.notif.ai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NotificationEntity::class,
        AppPreference::class,
        BatchSchedule::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun batchScheduleDao(): BatchScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notifa_database"
                )
                    .fallbackToDestructiveMigration(true) // For hackathon speed - destroys DB on schema change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
