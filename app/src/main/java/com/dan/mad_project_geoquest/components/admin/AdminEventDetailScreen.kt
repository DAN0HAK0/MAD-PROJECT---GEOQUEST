package com.dan.mad_project_geoquest.components.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.components.eventStatusLabel
import com.dan.mad_project_geoquest.components.formatEventDate
import kotlinx.coroutines.launch

@Composable
fun AdminEventDetailScreen(event: Event, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var caches              by remember { mutableStateOf<List<Cache>>(emptyList()) }
    var isLoading           by remember { mutableStateOf(true) }
    var selectedCache       by remember { mutableStateOf<Cache?>(null) }
    var showEditEvent       by remember { mutableStateOf(false) }
    var deleteConfirmCache  by remember { mutableStateOf<Cache?>(null) }
    var deleteConfirmEvent  by remember { mutableStateOf(false) }
    var resultMessage       by remember { mutableStateOf<String?>(null) }
    var currentEvent        by remember { mutableStateOf(event) }

    LaunchedEffect(Unit) {
        try {
            caches = RetrofitClient.instance.getCaches().filter { it.CacheEventID == event.EventID }
        } catch (_: Exception) {}
        isLoading = false
    }

    selectedCache?.let { cache ->
        AdminEditCacheDialog(
            cache = cache,
            onDismiss = { selectedCache = null },
            onSave = { payload ->
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.updateCache(cache.CacheID, payload)
                        if (response.isSuccessful) {
                            resultMessage = "Cache updated successfully"
                            caches = RetrofitClient.instance.getCaches()
                                .filter { it.CacheEventID == event.EventID }
                        } else {
                            resultMessage = "Failed: HTTP ${response.code()}"
                        }
                    } catch (e: Exception) {
                        resultMessage = "Error: ${e.localizedMessage}"
                    }
                    selectedCache = null
                }
            }
        )
    }

    deleteConfirmCache?.let { cache ->
        AlertDialog(
            onDismissRequest = { deleteConfirmCache = null },
            title = { Text("Delete Cache") },
            text = { Text("Delete '${cache.CacheName}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = RetrofitClient.instance.deleteCache(cache.CacheID)
                                if (response.isSuccessful) {
                                    resultMessage = "Cache deleted"
                                    caches = caches.filter { it.CacheID != cache.CacheID }
                                } else {
                                    resultMessage = "Failed: HTTP ${response.code()}"
                                }
                            } catch (e: Exception) {
                                resultMessage = "Error: ${e.localizedMessage}"
                            }
                            deleteConfirmCache = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmCache = null }) { Text("Cancel") }
            }
        )
    }

    if (deleteConfirmEvent) {
        AlertDialog(
            onDismissRequest = { deleteConfirmEvent = false },
            title = { Text("Delete Event") },
            text = {
                Text("Delete '${currentEvent.EventName}'? All its caches will also be removed. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = RetrofitClient.instance.deleteEvent(currentEvent.EventID)
                                if (response.isSuccessful) {
                                    deleteConfirmEvent = false
                                    onBack()
                                } else {
                                    resultMessage = "Failed: HTTP ${response.code()}"
                                    deleteConfirmEvent = false
                                }
                            } catch (e: Exception) {
                                resultMessage = "Error: ${e.localizedMessage}"
                                deleteConfirmEvent = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete Event") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmEvent = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            resultMessage?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.contains("success") || msg.contains("deleted"))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(msg, fontSize = 13.sp)
                            TextButton(onClick = { resultMessage = null }) { Text("OK") }
                        }
                    }
                }
            }

            if (showEditEvent) {
                item {
                    AdminEditEventForm(
                        event = currentEvent,
                        onSave = { payload ->
                            scope.launch {
                                try {
                                    val response = RetrofitClient.instance.updateEvent(
                                        currentEvent.EventID, payload
                                    )
                                    if (response.isSuccessful) {
                                        resultMessage = "Event updated successfully"
                                        showEditEvent = false
                                        RetrofitClient.instance.getEvents()
                                            .find { it.EventID == currentEvent.EventID }
                                            ?.let { currentEvent = it }
                                    } else {
                                        resultMessage = "Failed: HTTP ${response.code()}"
                                    }
                                } catch (e: Exception) {
                                    resultMessage = "Error: ${e.localizedMessage}"
                                }
                            }
                        },
                        onCancel = { showEditEvent = false }
                    )
                }
            }

            if (!showEditEvent) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Event Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Row {
                                    IconButton(onClick = { showEditEvent = !showEditEvent }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit event",
                                            tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { deleteConfirmEvent = true }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete event",
                                            tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            AdminDetailRow("ID",         "${currentEvent.EventID}")
                            AdminDetailRow("Status",     eventStatusLabel(currentEvent.EventStatusID).first)
                            AdminDetailRow("Visibility", if (currentEvent.EventIspublic) "Public" else "Private")
                            AdminDetailRow("Start",      formatEventDate(currentEvent.EventStart))
                            AdminDetailRow("Finish",     formatEventDate(currentEvent.EventFinish))
                            AdminDetailRow("Owner",      currentEvent.EventOwner?.UserUsername ?: "Unknown")
                            if (currentEvent.EventDescription.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    currentEvent.EventDescription, fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("Caches (${caches.size})", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (caches.isEmpty()) {
                item {
                    Text(
                        "No caches in this event yet.", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(caches) { cache ->
                    AdminCacheCard(
                        cache = cache,
                        onEdit = { selectedCache = cache },
                        onDelete = { deleteConfirmCache = cache }
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}