package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.components.CacheCard
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel

@Composable
fun HomeScreen(cacheViewModel: CacheViewModel) {
    val homeState by cacheViewModel.homeUiState.collectAsState()
    val user = SessionManager.currentUser

    LaunchedEffect(Unit) {
        cacheViewModel.loadHomeData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GeoQuest",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome, ${user?.UserUsername ?: "Explorer"}!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { cacheViewModel.loadHomeData() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quick stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatChip(
                modifier = Modifier.weight(1f),
                label = "Caches Found",
                value = "${homeState.myFinds.size}"
            )
            StatChip(
                modifier = Modifier.weight(1f),
                label = "Total Caches",
                value = "${homeState.allCaches.size}"
            )
            StatChip(
                modifier = Modifier.weight(1f),
                label = "Events",
                value = "${homeState.activeEvents.size}"
            )
        }

        Spacer(Modifier.height(20.dp))

        if (homeState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        homeState.errorMessage?.let { err ->
            Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }

        Text(
            text = "Discovered Caches",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (homeState.myFinds.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🗺️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No caches found yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Head to the Map to start hunting!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Show caches the player has found
            val foundCacheIds = homeState.myFinds.map { it.FindCacheID }.toSet()
            val foundCaches = homeState.allCaches.filter { it.CacheID in foundCacheIds }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(foundCaches) { cache ->
                    CacheCard(cache = cache)
                }
            }
        }
    }
}

@Composable
fun StatChip(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}