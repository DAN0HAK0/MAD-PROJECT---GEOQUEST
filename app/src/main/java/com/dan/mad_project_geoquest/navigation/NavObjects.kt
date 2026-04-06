package com.dan.mad_project_geoquest.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavObjects : NavKey {

    @Serializable
    data object Login : NavObjects

    @Serializable
    data object Register : NavObjects

    @Serializable
    data object Home : NavObjects

    @Serializable
    data object Blank : NavObjects  // Originally had this as blank screen but now changed to leaderboard screen

    @Serializable
    data object Map : NavObjects

    @Serializable
    data object Stats : NavObjects

    @Serializable
    data object Settings : NavObjects

    @Serializable
    data object Admin : NavObjects
}