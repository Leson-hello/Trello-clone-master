package com.example.trelloclonemaster3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [BoardEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrelloDatabase : RoomDatabase() {

    abstract fun boardDao(): BoardDao

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