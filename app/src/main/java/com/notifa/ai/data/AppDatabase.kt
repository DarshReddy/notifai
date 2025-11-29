package com.notifa.ai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.notifa.ai.util.Priority

@Database(
    entities = [
        NotificationEntity::class,
        AppPreference::class,
        BatchSchedule::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun batchScheduleDao(): BatchScheduleDao

    class Converters {
        @TypeConverter
        fun fromPriority(priority: Priority): String = priority.name

        @TypeConverter
        fun toPriority(name: String): Priority = Priority.valueOf(name)

        @TypeConverter
        fun fromNotificationCategory(category: NotificationCategory): String = category.name

        @TypeConverter
        fun toNotificationCategory(name: String): NotificationCategory = NotificationCategory.valueOf(name)
    }

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
                .fallbackToDestructiveMigration() // For hackathon speed - destroys DB on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
