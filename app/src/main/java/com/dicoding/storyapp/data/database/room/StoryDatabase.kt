package com.dicoding.storyapp.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.dicoding.storyapp.data.database.RemoteKeys
import com.dicoding.storyapp.data.database.RemoteKeysDao
import com.dicoding.storyapp.data.database.entity.ListStoryEntity


@Database(
    entities = [ListStoryEntity::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {

    abstract fun storyDao(): ListStoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java,
                    "story_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}