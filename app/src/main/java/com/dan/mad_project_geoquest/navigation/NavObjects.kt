package com.dan.mad_project_geoquest.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavObjects : NavKey {

    @Serializable
    data object Home : NavObjects

    @Serializable
    data object Blank : NavObjects

    @Serializable
    data object Map : NavObjects

    @Serializable
    data object Stats : NavObjects

    @Serializable
    data object Settings : NavObjects
}