package com.dan.mad_project_geoquest.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.dan.mad_project_geoquest.navigation.NavObjects

@Composable
fun BottomNavBar(
    currentDestination: Any?,
    isAdmin: Boolean = false,
    onNavigate: (NavObjects) -> Unit
) {
    if (isAdmin) {
        // Admin only sees Admin and Settings
        NavigationBar {
            NavigationBarItem(
                selected = currentDestination is NavObjects.Admin,
                onClick = { onNavigate(NavObjects.Admin) },
                icon = { Icon(Icons.Filled.Star, contentDescription = "Admin") },
                label = { Text("Admin") }
            )
            NavigationBarItem(
                selected = currentDestination is NavObjects.Settings,
                onClick = { onNavigate(NavObjects.Settings) },
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        }
    } else {
        // Regular user nav
        NavigationBar {
            NavigationBarItem(
                selected = currentDestination is NavObjects.Home,
                onClick = { onNavigate(NavObjects.Home) },
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text("Home") }
            )
            NavigationBarItem(
                selected = currentDestination is NavObjects.Blank,
                onClick = { onNavigate(NavObjects.Blank) },
                icon = { Icon(Icons.Filled.Star, contentDescription = "Leaderboard") },
                label = { Text("Leaderbo") }
            )
            NavigationBarItem(
                selected = currentDestination is NavObjects.Map,
                onClick = { onNavigate(NavObjects.Map) },
                icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Map") },
                label = { Text("Map") }
            )
            NavigationBarItem(
                selected = currentDestination is NavObjects.Stats,
                onClick = { onNavigate(NavObjects.Stats) },
                icon = { Icon(Icons.Filled.Info, contentDescription = "Stats") },
                label = { Text("Stats") }
            )
            NavigationBarItem(
                selected = currentDestination is NavObjects.Settings,
                onClick = { onNavigate(NavObjects.Settings) },
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        }
    }
}