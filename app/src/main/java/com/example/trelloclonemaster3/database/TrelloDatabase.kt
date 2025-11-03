package com.example.trelloclonemaster3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.trelloclonemaster3.model.Notification

@Database(
    entities = [BoardEntity::class, Notification::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrelloDatabase : RoomDatabase() {

    abstract fun boardDao(): BoardDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: TrelloDatabase? = null

        fun getDatabase(context: Context): TrelloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrelloDatabase::class.java,
                    "trello_database"
                )
                    .fallbackToDestructiveMigration() // Recreate database if schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // For testing purposes - create in-memory database
        fun getInMemoryDatabase(context: Context): TrelloDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                TrelloDatabase::class.java
            ).build()
        }
    }
}