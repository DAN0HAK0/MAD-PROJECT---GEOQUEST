package com.dan.mad_project_geoquest.components.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.Player
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.components.eventStatusLabel
import com.dan.mad_project_geoquest.components.formatEventDate

@Composable
fun AdminOverviewTab(onEventClick: (Event) -> Unit) {
    var events    by remember { mutableStateOf<List<Event>>(emptyList()) }
    var players   by remember { mutableStateOf<List<Player>>(emptyList()) }
    var caches    by remember { mutableStateOf<List<Cache>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            events  = RetrofitClient.instance.getEvents()
                .filter { it.EventOwnerID == 900 && it.EventIspublic }
            players = RetrofitClient.instance.getPlayers()
            caches  = RetrofitClient.instance.getCaches()
        } catch (_: Exception) {}
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Events", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(events) { event ->
            val eventPlayers = players.filter { it.PlayerEventID == event.EventID }
            val eventCaches  = caches.filter  { it.CacheEventID  == event.EventID }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onEventClick(event) },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.EventName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            eventStatusLabel(event.EventStatusID).first,
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "ID: ${event.EventID}  •  ${eventPlayers.size} players  •  ${eventCaches.size} caches",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${formatEventDate(event.EventStart)} — ${formatEventDate(event.EventFinish)}",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.Edit, contentDescription = "View event",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}