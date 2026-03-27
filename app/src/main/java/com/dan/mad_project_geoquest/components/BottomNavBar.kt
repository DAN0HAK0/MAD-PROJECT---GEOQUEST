package com.dan.mad_project_geoquest.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.navigation.NavObjects

@Composable
fun BottomNavBar(
    currentDestination: Any?,
    onNavigate: (Any) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        // Nav bar background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .shadow(8.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    selected = currentDestination is NavObjects.Home,
                    onClick = { onNavigate(NavObjects.Home) }
                )
                NavBarItem(
                    icon = Icons.Filled.Star,
                    label = "Leaderboard",
                    selected = currentDestination is NavObjects.Blank,
                    onClick = { onNavigate(NavObjects.Blank) }
                )
                Spacer(modifier = Modifier.size(60.dp))
                NavBarItem(
                    icon = Icons.Filled.Info,
                    label = "Stats",
                    selected = currentDestination is NavObjects.Stats,
                    onClick = { onNavigate(NavObjects.Stats) }
                )
                NavBarItem(
                    icon = Icons.Filled.Build,
                    label = "Settings",
                    selected = currentDestination is NavObjects.Settings,
                    onClick = { onNavigate(NavObjects.Settings) }
                )
            }
        }

        // Map button floating above
        Box(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(color = Color.Red)
                .clickable { onNavigate(NavObjects.Map) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = "Map",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}