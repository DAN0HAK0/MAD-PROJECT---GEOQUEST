package com.dan.mad_project_geoquest.database

data class Cache(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val clue: String,
    val points: Int
)