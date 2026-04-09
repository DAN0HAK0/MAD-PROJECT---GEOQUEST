package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.components.lb.EventFilterDropdown
import com.dan.mad_project_geoquest.components.lb.LeaderboardRow
import com.dan.mad_project_geoquest.components.lb.MiniPodium
import com.dan.mad_project_geoquest.components.lb.Podium
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Gold
import com.dan.mad_project_geoquest.ui.theme.Sand
import com.dan.mad_project_geoquest.viewmodel.LeaderboardEntry
import com.dan.mad_project_geoquest.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(leaderboardViewModel: LeaderboardViewModel) {
    val state by leaderboardViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        leaderboardViewModel.loadLeaderboard()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Leaderboard", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.padding(end = 8.dp)) {
                                FilterChip(
                                    selected = !state.sortByPoints,
                                    onClick = { leaderboardViewModel.onSortChanged(false) },
                                    label = { Text("Finds", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Gold,
                                        selectedLabelColor = DarkBrown
                                    )
                                )
                                Spacer(Modifier.width(6.dp))
                                FilterChip(
                                    selected = state.sortByPoints,
                                    onClick = { leaderboardViewModel.onSortChanged(true) },
                                    label = { Text("Points", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Gold,
                                        selectedLabelColor = DarkBrown
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { leaderboardViewModel.loadLeaderboard() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBrown,
                        titleContentColor = Cream,
                        actionIconContentColor = Cream
                    )
                )
                TabRow(
                    selectedTabIndex = if (state.isPublicTab) 0 else 1,
                    containerColor = DarkBrown,
                    contentColor = Cream,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                tabPositions[if (state.isPublicTab) 0 else 1]
                            ),
                            color = Gold
                        )
                    }
                ) {
                    Tab(
                        selected = state.isPublicTab,
                        onClick = { if (!state.isPublicTab) leaderboardViewModel.onTabChanged(true) },
                        text = { Text("Public Events") },
                        selectedContentColor = Cream,
                        unselectedContentColor = Sand
                    )
                    Tab(
                        selected = !state.isPublicTab,
                        onClick = { if (state.isPublicTab) leaderboardViewModel.onTabChanged(false) },
                        text = { Text("Private Events") },
                        selectedContentColor = Cream,
                        unselectedContentColor = Sand
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            if (state.availableEvents.isNotEmpty()) {
                EventFilterDropdown(
                    availableEvents = state.availableEvents,
                    selectedEventId = state.selectedEventId,
                    isPublicTab = state.isPublicTab,
                    onEventSelected = { leaderboardViewModel.onEventSelected(it) }
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.entries.isEmpty()) {
                EmptyLeaderboardMessage(
                    isPublicTab = state.isPublicTab,
                    hasNoEvents = state.availableEvents.isEmpty()
                )
                return@Column
            }

            val subtitle: (LeaderboardEntry) -> String = { entry ->
                if (state.sortByPoints) "${entry.totalPoints.toInt()} pts"
                else "${entry.findCount} finds"
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    when {
                        state.entries.size >= 3 -> Podium(
                            first = state.entries[0],
                            second = state.entries[1],
                            third = state.entries[2],
                            subtitle = subtitle
                        )
                        state.entries.size == 2 -> MiniPodium(
                            first = state.entries[0],
                            second = state.entries[1],
                            subtitle = subtitle
                        )
                        else -> LeaderboardRow(
                            entry = state.entries[0],
                            subtitle = subtitle(state.entries[0])
                        )
                    }
                }
                val remaining = if (state.entries.size > 3) state.entries.drop(3) else emptyList()
                if (remaining.isNotEmpty()) {
                    item {
                        Text(
                            "Other players",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    itemsIndexed(remaining) { _, entry ->
                        LeaderboardRow(entry = entry, subtitle = subtitle(entry))
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyLeaderboardMessage(isPublicTab: Boolean, hasNoEvents: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (hasNoEvents && !isPublicTab) "No private events joined yet"
                else "No finds recorded yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (hasNoEvents && !isPublicTab) "Join a private event to see its leaderboard"
                else "Go find some caches!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}