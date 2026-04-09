package com.dan.mad_project_geoquest.api


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