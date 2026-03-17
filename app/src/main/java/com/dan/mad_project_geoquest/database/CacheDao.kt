package com.dan.mad_project_geoquest.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {
    @Query("SELECT * FROM caches WHERE isFound = 1")
    fun getFoundCaches(): Flow<List<Cache>>

    @Query("SELECT * FROM caches")
    fun getAllCaches(): Flow<List<Cache>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(caches: List<Cache>)

    @Query("SELECT COUNT(*) FROM caches")
    suspend fun getCount(): Int

    @Query("UPDATE caches SET isFound = 1 WHERE id = :cacheId")
    suspend fun markAsFound(cacheId: Int)

    @Query("UPDATE caches SET notificationSent = 1 WHERE id = :cacheId")
    suspend fun markNotificationSent(cacheId: Int)
}