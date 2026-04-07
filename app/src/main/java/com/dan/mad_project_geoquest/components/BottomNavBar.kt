package com.dan.mad_project_geoquest.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dan.mad_project_geoquest.navigation.NavObjects
import com.dan.mad_project_geoquest.ui.theme.Brown
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Gold
import com.dan.mad_project_geoquest.ui.theme.Sand

@Composable
fun BottomNavBar(
    currentDestination: Any?,
    isAdmin: Boolean = false,
    onNavigate: (NavObjects) -> Unit
) {
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Cream,
        selectedTextColor = Cream,
        unselectedIconColor = Sand,
        unselectedTextColor = Sand,
        indicatorColor = Brown
    )

    if (isAdmin) {
        NavigationBar(containerColor = DarkBrown) {
            NavigationBarItem(
                selected = currentDestination is NavObjects.Admin,
                onClick = { onNavigate(NavObjects.Admin) },
                icon = { Icon(Icons.Filled.Star, contentDescription = "Admin") },
                label = { Text("Admin") },
                colors = navItemColors
            )
            NavigationBarItem(
                selected = currentDestination is NavObjects.Settings,
                onClick = { onNavigate(NavObjects.Settings) },
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                colors = navItemColors
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            NavigationBar(
                containerColor = DarkBrown,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NavigationBarItem(
                    selected = currentDestination is NavObjects.Home,
                    onClick = { onNavigate(NavObjects.Home) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = currentDestination is NavObjects.Blank,
                    onClick = { onNavigate(NavObjects.Blank) },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Rankings") },
                    label = { Text("Rankings") },
                    colors = navItemColors
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationBarItem(
                    selected = currentDestination is NavObjects.Stats,
                    onClick = { onNavigate(NavObjects.Stats) },
                    icon = { Icon(Icons.Filled.Info, contentDescription = "Stats") },
                    label = { Text("Stats") },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = currentDestination is NavObjects.Settings,
                    onClick = { onNavigate(NavObjects.Settings) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = navItemColors
                )
            }

            FloatingActionButton(
                onClick = { onNavigate(NavObjects.Map) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
                    .size(72.dp),
                containerColor = Gold,
                contentColor = DarkBrown,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Map",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}