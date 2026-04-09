package com.dan.mad_project_geoquest.api

// Simple singleton to persist the logged-in user across the app session
object SessionManager {
    var currentUser: User? = null
    var currentPlayer: Player? = null

    val isAdmin: Boolean
        get() = currentUser?.UserID == 900

    fun clear() {
        currentUser = null
        currentPlayer = null
    }
}


// ADMIN ACCOUNT:
//USER NAME: AdminDan
//PASSWORD: AdminAccess