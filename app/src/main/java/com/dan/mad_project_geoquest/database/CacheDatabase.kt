package com.dan.mad_project_geoquest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Cache::class], version = 2)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile private var INSTANCE: CacheDatabase? = null

        fun getDatabase(context: Context): CacheDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CacheDatabase::class.java,
                    "cache_database"
                )
                    .fallbackToDestructiveMigration()
                    .setQueryExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
                    .build().also { INSTANCE = it }
            }
        }
    }
}