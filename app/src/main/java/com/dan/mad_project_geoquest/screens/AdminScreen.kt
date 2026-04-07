package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.dan.mad_project_geoquest.api.CachePayload
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.EventPayload
import com.dan.mad_project_geoquest.api.Player
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    cacheViewModel: CacheViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val tabs = listOf("Overview", "Create Cache", "Create Event")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Admin Panel", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${SessionManager.currentUser?.UserUsername}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedButton(onClick = onLogout, shape = RoundedCornerShape(8.dp)) {
                Text("Logout", fontSize = 13.sp)
            }
        }

        if (selectedEvent == null) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }
        }

        when {
            selectedEvent != null -> AdminEventDetailScreen(
                event = selectedEvent!!,
                onBack = { selectedEvent = null }
            )
            selectedTab == 0 -> AdminOverviewTab(onEventClick = { selectedEvent = it })
            selectedTab == 1 -> AdminCreateCacheTab()
            selectedTab == 2 -> AdminCreateEventTab()
        }
    }
}

// ── Overview Tab ──────────────────────────────────────────────────

@Composable
fun AdminOverviewTab(onEventClick: (Event) -> Unit) {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var caches by remember { mutableStateOf<List<Cache>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            events = RetrofitClient.instance.getEvents()
            players = RetrofitClient.instance.getPlayers()
            caches = RetrofitClient.instance.getCaches()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(modifier = Modifier.weight(1f), label = "Events", value = "${events.size}")
                AdminStatCard(modifier = Modifier.weight(1f), label = "Players", value = "${players.size}")
                AdminStatCard(modifier = Modifier.weight(1f), label = "Caches", value = "${caches.size}")
            }
        }

        item {
            Text(
                text = "Events",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(events) { event ->
            val eventPlayers = players.filter { it.PlayerEventID == event.EventID }
            val eventCaches = caches.filter { it.CacheEventID == event.EventID }
            val statusLabel = eventStatusLabel(event.EventStatusID).first

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
                        Text(text = event.EventName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = statusLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "ID: ${event.EventID}  •  ${eventPlayers.size} players  •  ${eventCaches.size} caches",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatEventDate(event.EventStart)} — ${formatEventDate(event.EventFinish)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "View event",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── Event Detail Screen ───────────────────────────────────────────

@Composable
fun AdminEventDetailScreen(event: Event, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var caches by remember { mutableStateOf<List<Cache>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCache by remember { mutableStateOf<Cache?>(null) }
    var showEditEvent by remember { mutableStateOf(false) }
    var deleteConfirmCache by remember { mutableStateOf<Cache?>(null) }
    var deleteConfirmEvent by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var currentEvent by remember { mutableStateOf(event) }

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
                            caches = RetrofitClient.instance.getCaches().filter {
                                it.CacheEventID == event.EventID
                            }
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
            text = { Text("Are you sure you want to delete '${cache.CacheName}'? This cannot be undone.") },
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
            text = { Text("Are you sure you want to delete '${currentEvent.EventName}'? This cannot be undone.") },
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
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmEvent = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = currentEvent.EventName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showEditEvent = !showEditEvent }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit event", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { deleteConfirmEvent = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete event", tint = MaterialTheme.colorScheme.error)
            }
        }

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
                            containerColor = if (msg.contains("success") || msg.contains("updated") || msg.contains("deleted"))
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
                            Text(text = msg, fontSize = 13.sp)
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
                                    val response = RetrofitClient.instance.updateEvent(currentEvent.EventID, payload)
                                    if (response.isSuccessful) {
                                        resultMessage = "Event updated successfully"
                                        showEditEvent = false
                                        val updatedEvents = RetrofitClient.instance.getEvents()
                                        updatedEvents.find { it.EventID == currentEvent.EventID }
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Event Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            AdminDetailRow("ID", "${currentEvent.EventID}")
                            AdminDetailRow("Status", eventStatusLabel(currentEvent.EventStatusID).first)
                            AdminDetailRow("Visibility", if (currentEvent.EventIspublic) "Public" else "Private")
                            AdminDetailRow("Start", formatEventDate(currentEvent.EventStart))
                            AdminDetailRow("Finish", formatEventDate(currentEvent.EventFinish))
                            AdminDetailRow("Owner", currentEvent.EventOwner?.UserUsername ?: "Unknown")
                            if (currentEvent.EventDescription.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = currentEvent.EventDescription,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(text = "Caches (${caches.size})", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (caches.isEmpty()) {
                item {
                    Text(
                        text = "No caches in this event yet.",
                        fontSize = 13.sp,
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

@Composable
fun AdminDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
    }
}

@Composable
fun AdminCacheCard(cache: Cache, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = cache.CacheName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = "ID: ${cache.CacheID}  •  ${cache.CachePoints.toInt()} pts",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${cache.CacheLatitude}, ${cache.CacheLongitude}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit cache", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete cache", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (cache.CacheDescription.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = cache.CacheDescription.take(100) + if (cache.CacheDescription.length > 100) "..." else "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Edit Event Form ───────────────────────────────────────────────

@Composable
fun AdminEditEventForm(event: Event, onSave: (EventPayload) -> Unit, onCancel: () -> Unit) {
    var eventName by remember { mutableStateOf(event.EventName) }
    var eventDescription by remember { mutableStateOf(event.EventDescription) }
    var eventStart by remember { mutableStateOf(event.EventStart) }
    var eventFinish by remember { mutableStateOf(event.EventFinish) }
    var eventStatusId by remember { mutableStateOf("${event.EventStatusID}") }
    var eventIsPublic by remember { mutableStateOf(event.EventIspublic) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val statuses = listOf("1" to "Pending", "2" to "Active", "3" to "Paused", "4" to "Cancelled", "5" to "Completed")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Edit Event", fontSize = 16.sp, fontWeight = FontWeight.Bold)

            AdminTextField(value = eventName, onValueChange = { eventName = it }, label = "Event Name")
            AdminTextField(value = eventDescription, onValueChange = { eventDescription = it }, label = "Description", minLines = 2)
            AdminTextField(value = eventStart, onValueChange = { eventStart = it }, label = "Start (yyyy-MM-ddTHH:mm:ss.SSSZ)")
            AdminTextField(value = eventFinish, onValueChange = { eventFinish = it }, label = "Finish (yyyy-MM-ddTHH:mm:ss.SSSZ)")

            Text(text = "Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statuses.take(3).forEach { (id, label) ->
                    FilterChip(
                        selected = eventStatusId == id,
                        onClick = { eventStatusId = id },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statuses.drop(3).forEach { (id, label) ->
                    FilterChip(
                        selected = eventStatusId == id,
                        onClick = { eventStatusId = id },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (eventIsPublic) "Public Event" else "Private Event",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (eventIsPublic) "Anyone can join" else "Invite only",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = eventIsPublic, onCheckedChange = { eventIsPublic = it })
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val statusId = eventStatusId.toIntOrNull() ?: 1
                        val currentUser = SessionManager.currentUser
                        if (eventName.isBlank() || eventDescription.isBlank() || currentUser == null) {
                            errorMessage = "Please fill in all fields"
                            return@Button
                        }
                        onSave(
                            EventPayload(
                                EventName = eventName.trim(),
                                EventDescription = eventDescription.trim(),
                                EventOwnerID = currentUser.UserID,
                                EventIspublic = eventIsPublic,
                                EventStart = eventStart.trim(),
                                EventFinish = eventFinish.trim(),
                                EventStatusID = statusId
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

// ── Edit Cache Dialog ─────────────────────────────────────────────

@Composable
fun AdminEditCacheDialog(cache: Cache, onDismiss: () -> Unit, onSave: (CachePayload) -> Unit) {
    var cacheName by remember { mutableStateOf(cache.CacheName) }
    var cacheDescription by remember { mutableStateOf(cache.CacheDescription) }
    var cacheImageUrl by remember { mutableStateOf(cache.CacheImageURL) }
    var cacheClue by remember { mutableStateOf(cache.CacheClue) }
    var cachePoints by remember { mutableStateOf("${cache.CachePoints}") }
    var cacheLatitude by remember { mutableStateOf("${cache.CacheLatitude}") }
    var cacheLongitude by remember { mutableStateOf("${cache.CacheLongitude}") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cache", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminTextField(value = cacheName, onValueChange = { cacheName = it }, label = "Cache Name")
                AdminTextField(value = cacheDescription, onValueChange = { cacheDescription = it }, label = "Description", minLines = 2)
                AdminTextField(value = cacheImageUrl, onValueChange = { cacheImageUrl = it }, label = "Image URL")
                AdminTextField(value = cacheClue, onValueChange = { cacheClue = it }, label = "Clue", minLines = 2)
                AdminTextField(value = cachePoints, onValueChange = { cachePoints = it }, label = "Points")
                AdminTextField(value = cacheLatitude, onValueChange = { cacheLatitude = it }, label = "Latitude")
                AdminTextField(value = cacheLongitude, onValueChange = { cacheLongitude = it }, label = "Longitude")
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val points = cachePoints.toDoubleOrNull()
                    val lat = cacheLatitude.toDoubleOrNull()
                    val lng = cacheLongitude.toDoubleOrNull()
                    if (cacheName.isBlank() || points == null || lat == null || lng == null) {
                        errorMessage = "Please fill in all fields correctly"
                        return@Button
                    }
                    onSave(
                        CachePayload(
                            CacheName = cacheName.trim(),
                            CacheDescription = cacheDescription.trim(),
                            CacheEventID = cache.CacheEventID,
                            CacheImageURL = cacheImageUrl.trim().ifBlank {
                                "https://static.generated.photos/vue-static/face-generator/landing/wall/1.jpg"
                            },
                            CacheClue = cacheClue.trim(),
                            CachePoints = points,
                            CacheLatitude = lat,
                            CacheLongitude = lng
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Stat Card ─────────────────────────────────────────────────────

@Composable
fun AdminStatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Create Cache Tab ──────────────────────────────────────────────

@Composable
fun AdminCreateCacheTab() {
    val scope = rememberCoroutineScope()

    var cacheName by remember { mutableStateOf("") }
    var cacheDescription by remember { mutableStateOf("") }
    var cacheEventId by remember { mutableStateOf("") }
    var cacheImageUrl by remember { mutableStateOf("") }
    var cacheClue by remember { mutableStateOf("") }
    var cachePoints by remember { mutableStateOf("") }
    var cacheLatitude by remember { mutableStateOf("") }
    var cacheLongitude by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Create New Cache", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Fill in all fields to add a new cache to an event.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        AdminTextField(value = cacheName, onValueChange = { cacheName = it }, label = "Cache Name")
        AdminTextField(value = cacheDescription, onValueChange = { cacheDescription = it }, label = "Description", minLines = 3)
        AdminTextField(value = cacheEventId, onValueChange = { cacheEventId = it }, label = "Event ID (e.g. 245)")
        AdminTextField(value = cacheImageUrl, onValueChange = { cacheImageUrl = it }, label = "Image URL")
        AdminTextField(value = cacheClue, onValueChange = { cacheClue = it }, label = "Clue", minLines = 2)
        AdminTextField(value = cachePoints, onValueChange = { cachePoints = it }, label = "Points (e.g. 20)")
        AdminTextField(value = cacheLatitude, onValueChange = { cacheLatitude = it }, label = "Latitude (e.g. 51.4109)")
        AdminTextField(value = cacheLongitude, onValueChange = { cacheLongitude = it }, label = "Longitude (e.g. -0.3081)")

        resultMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.startsWith("Cache"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = if (msg.startsWith("Cache"))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Button(
            onClick = {
                val eventId = cacheEventId.toIntOrNull()
                val points = cachePoints.toDoubleOrNull()
                val lat = cacheLatitude.toDoubleOrNull()
                val lng = cacheLongitude.toDoubleOrNull()

                if (cacheName.isBlank() || cacheDescription.isBlank() ||
                    eventId == null || points == null || lat == null || lng == null || cacheClue.isBlank()
                ) {
                    resultMessage = "Please fill in all fields correctly"
                    return@Button
                }

                isLoading = true
                resultMessage = null

                scope.launch {
                    try {
                        val response = RetrofitClient.instance.createCache(
                            CachePayload(
                                CacheName = cacheName.trim(),
                                CacheDescription = cacheDescription.trim(),
                                CacheEventID = eventId,
                                CacheImageURL = cacheImageUrl.trim().ifBlank {
                                    "https://static.generated.photos/vue-static/face-generator/landing/wall/1.jpg"
                                },
                                CacheClue = cacheClue.trim(),
                                CachePoints = points,
                                CacheLatitude = lat,
                                CacheLongitude = lng
                            )
                        )
                        if (response.isSuccessful) {
                            resultMessage = "Cache '$cacheName' created successfully"
                            cacheName = ""; cacheDescription = ""; cacheEventId = ""
                            cacheImageUrl = ""; cacheClue = ""; cachePoints = ""
                            cacheLatitude = ""; cacheLongitude = ""
                        } else {
                            resultMessage = "Failed: HTTP ${response.code()}"
                        }
                    } catch (e: Exception) {
                        resultMessage = "Error: ${e.localizedMessage}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Create Cache", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Create Event Tab ──────────────────────────────────────────────

@Composable
fun AdminCreateEventTab() {
    val scope = rememberCoroutineScope()

    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventStart by remember { mutableStateOf("2026-04-01T00:00:00.000Z") }
    var eventFinish by remember { mutableStateOf("2026-09-01T23:59:59.000Z") }
    var eventStatusId by remember { mutableStateOf("1") }
    var eventIsPublic by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val statuses = listOf("1" to "Pending", "2" to "Active", "3" to "Paused", "4" to "Cancelled", "5" to "Completed")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Create New Event", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Fill in the details to create a new GeoQuest event.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        AdminTextField(value = eventName, onValueChange = { eventName = it }, label = "Event Name")
        AdminTextField(value = eventDescription, onValueChange = { eventDescription = it }, label = "Description", minLines = 3)
        AdminTextField(value = eventStart, onValueChange = { eventStart = it }, label = "Start Date (yyyy-MM-ddTHH:mm:ss.SSSZ)")
        AdminTextField(value = eventFinish, onValueChange = { eventFinish = it }, label = "Finish Date (yyyy-MM-ddTHH:mm:ss.SSSZ)")

        Text(text = "Event Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.take(3).forEach { (id, label) ->
                FilterChip(
                    selected = eventStatusId == id,
                    onClick = { eventStatusId = id },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.drop(3).forEach { (id, label) ->
                FilterChip(
                    selected = eventStatusId == id,
                    onClick = { eventStatusId = id },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (eventIsPublic) "Public Event" else "Private Event",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (eventIsPublic) "Anyone can join" else "Invite only",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = eventIsPublic, onCheckedChange = { eventIsPublic = it })
        }

        resultMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.startsWith("Event"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = if (msg.startsWith("Event"))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Button(
            onClick = {
                val statusId = eventStatusId.toIntOrNull() ?: 1
                val currentUser = SessionManager.currentUser
                if (eventName.isBlank() || eventDescription.isBlank() ||
                    eventStart.isBlank() || eventFinish.isBlank() || currentUser == null
                ) {
                    resultMessage = "Please fill in all fields"
                    return@Button
                }

                isLoading = true
                resultMessage = null

                scope.launch {
                    try {
                        val response = RetrofitClient.instance.createEvent(
                            EventPayload(
                                EventName = eventName.trim(),
                                EventDescription = eventDescription.trim(),
                                EventOwnerID = currentUser.UserID,
                                EventIspublic = eventIsPublic,
                                EventStart = eventStart.trim(),
                                EventFinish = eventFinish.trim(),
                                EventStatusID = statusId
                            )
                        )
                        if (response.isSuccessful) {
                            resultMessage = "Event '$eventName' created successfully"
                            eventName = ""; eventDescription = ""
                            eventStart = "2026-04-01T00:00:00.000Z"
                            eventFinish = "2026-09-01T23:59:59.000Z"
                            eventStatusId = "1"; eventIsPublic = true
                        } else {
                            resultMessage = "Failed: HTTP ${response.code()}"
                        }
                    } catch (e: Exception) {
                        resultMessage = "Error: ${e.localizedMessage}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Create Event", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Shared text field ─────────────────────────────────────────────

@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        minLines = minLines
    )
}