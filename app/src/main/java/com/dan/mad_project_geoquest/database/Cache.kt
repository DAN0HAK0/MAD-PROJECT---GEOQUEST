package com.dan.mad_project_geoquest.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caches")
data class Cache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val clue: String,
    val points: Int,
    val isFound: Boolean = false,
    val notificationSent: Boolean = false
)