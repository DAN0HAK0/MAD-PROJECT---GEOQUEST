package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.components.hs.EventCard
import com.dan.mad_project_geoquest.components.hs.HomeTabRow
import com.dan.mad_project_geoquest.components.hs.HomeTopBar
import com.dan.mad_project_geoquest.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToMyEvents: () -> Unit,
    onNavigateToEventDetail: (eventId: Int) -> Unit
) {
    val homeState by homeViewModel.uiState.collectAsState()
    val currentUser = SessionManager.currentUser
    var joinMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        homeViewModel.loadHomeData()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                username = currentUser?.UserUsername ?: "Explorer",
                onMyEventsClick = onNavigateToMyEvents,
                onRefreshClick = { homeViewModel.loadHomeData() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                HomeTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                if (homeState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@Column
                }

                homeState.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                val foundCacheIds = homeState.myFinds.map { it.FindCacheID }.toSet()

                val filteredEvents = when (selectedTab) {
                    0 -> homeState.activeEvents.filter { it.EventIspublic }

                    1 -> homeState.activeEvents.filter { event ->
                        if (!event.EventIspublic) return@filter false
                        val caches = homeState.allCaches.filter { it.CacheEventID == event.EventID }
                        val isJoined = homeState.allPlayers.any {
                            it.PlayerUserID == currentUser?.UserID && it.PlayerEventID == event.EventID
                        }
                        isJoined && caches.isNotEmpty() &&
                                caches.count { it.CacheID in foundCacheIds } == caches.size
                    }

                    2 -> homeState.activeEvents.filter { event ->
                        if (!event.EventIspublic) return@filter false
                        val caches = homeState.allCaches.filter { it.CacheEventID == event.EventID }
                        val isJoined = homeState.allPlayers.any {
                            it.PlayerUserID == currentUser?.UserID && it.PlayerEventID == event.EventID
                        }
                        isJoined && caches.isNotEmpty() &&
                                caches.count { it.CacheID in foundCacheIds } < caches.size
                    }

                    // Private tab — exclude events owned by the current user
                    // (owners manage their events via My Events, not here)
                    3 -> homeState.activeEvents.filter {
                        !it.EventIspublic && it.EventOwnerID != currentUser?.UserID
                    }

                    else -> emptyList()
                }

                if (filteredEvents.isEmpty()) {
                    EmptyTabMessage(selectedTab)
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredEvents) { event ->
                            val eventCaches = homeState.allCaches.filter { it.CacheEventID == event.EventID }
                            val isJoined = homeState.allPlayers.any {
                                it.PlayerUserID == currentUser?.UserID && it.PlayerEventID == event.EventID
                            }
                            val isOwner = event.EventOwnerID == currentUser?.UserID

                            EventCard(
                                event = event,
                                eventCaches = eventCaches,
                                foundCacheIds = foundCacheIds,
                                isJoined = isJoined,
                                isOwner = isOwner,
                                onJoin = { _ ->
                                    homeViewModel.joinEvent(event) { _, message ->
                                        joinMessage = message
                                    }
                                },
                                onViewDetails = { onNavigateToEventDetail(event.EventID) }
                            )
                        }
                    }
                }
            }

            joinMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    action = {
                        TextButton(onClick = { joinMessage = null }) { Text("OK") }
                    }
                ) { Text(msg) }
            }
        }
    }
}

@Composable
private fun EmptyTabMessage(selectedTab: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (selectedTab) {
                    0 -> "No events available"
                    1 -> "No completed events yet"
                    2 -> "No events in progress"
                    else -> "No private events available"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (selectedTab) {
                    0 -> "Check back soon!"
                    1 -> "Find all caches in an event to complete it"
                    2 -> "Join an event and start finding caches"
                    else -> "Private events will appear here — ask an organiser for the invite code"
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}