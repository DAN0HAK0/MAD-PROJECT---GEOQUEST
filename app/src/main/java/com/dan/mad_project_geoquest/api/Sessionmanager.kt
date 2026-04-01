package com.dan.mad_project_geoquest.api

// Simple singleton to persist the logged-in user across the app session
object SessionManager {
    var currentUser: User? = null
    var currentPlayer: Player? = null

    fun clear() {
        currentUser = null
        currentPlayer = null
    }
}