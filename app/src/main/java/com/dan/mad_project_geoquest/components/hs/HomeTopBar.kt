package com.dan.mad_project_geoquest.components.hs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Sand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    username: String,
    onMyEventsClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "GeoQuest",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome, $username",
                    fontSize = 12.sp,
                    color = Sand
                )
            }
        },
        actions = {
            TextButton(onClick = onMyEventsClick) {
                Text("My Events", color = Cream, fontSize = 13.sp)
            }
            IconButton(onClick = onRefreshClick) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBrown,
            titleContentColor = Cream,
            actionIconContentColor = Cream
        )
    )
}