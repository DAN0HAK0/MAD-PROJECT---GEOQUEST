package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.components.admin.AdminCreateCacheTab
import com.dan.mad_project_geoquest.components.admin.AdminCreateEventTab
import com.dan.mad_project_geoquest.components.admin.AdminEventDetailScreen
import com.dan.mad_project_geoquest.components.admin.AdminOverviewTab
import com.dan.mad_project_geoquest.components.admin.AdminUsersTab
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Sand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    var selectedTab   by remember { mutableIntStateOf(0) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val tabs = listOf("Overview", "Users", "Cache", "New Event")

    Scaffold(
        topBar = {
            if (selectedEvent == null) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Admin Panel", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    SessionManager.currentUser?.UserUsername ?: "",
                                    fontSize = 12.sp, color = Sand
                                )
                            }
                        },
                        actions = {
                            OutlinedButton(
                                onClick = onLogout,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Cream),
                                border = BorderStroke(1.dp, Cream.copy(alpha = 0.5f))
                            ) { Text("Logout", fontSize = 13.sp) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkBrown,
                            titleContentColor = Cream,
                            actionIconContentColor = Cream
                        )
                    )
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                modifier = Modifier.height(40.dp),
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = {
                        Text(
                            selectedEvent!!.EventName,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedEvent = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBrown,
                        titleContentColor = Cream,
                        navigationIconContentColor = Cream,
                        actionIconContentColor = Cream
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                selectedEvent != null -> AdminEventDetailScreen(
                    event = selectedEvent!!,
                    onBack = { selectedEvent = null }
                )
                selectedTab == 0 -> AdminOverviewTab(onEventClick = { selectedEvent = it })
                selectedTab == 1 -> AdminUsersTab()
                selectedTab == 2 -> AdminCreateCacheTab()
                selectedTab == 3 -> AdminCreateEventTab()
            }
        }
    }
}