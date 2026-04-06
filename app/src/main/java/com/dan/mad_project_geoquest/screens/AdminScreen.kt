package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    val tabs = listOf("Overview", "Create Cache", "Create Event")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Admin Panel",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Logged in as ${SessionManager.currentUser?.UserUsername}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Logout", fontSize = 13.sp)
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 13.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> AdminOverviewTab()
            1 -> AdminCreateCacheTab()
            2 -> AdminCreateEventTab()
        }
    }
}

// ── Overview Tab ──────────────────────────────────────────────────

@Composable
fun AdminOverviewTab() {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var caches by remember { mutableStateOf<List<Cache>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var expandedEventId by remember { mutableIntStateOf(-1) }

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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary stat cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Events",
                    value = "${events.size}"
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Players",
                    value = "${players.size}"
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Caches",
                    value = "${caches.size}"
                )
            }
        }

        item {
            Text(
                text = "Events & Players",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(events) { event ->
            val eventPlayers = players.filter { it.PlayerEventID == event.EventID }
            val eventCaches = caches.filter { it.CacheEventID == event.EventID }
            val isExpanded = expandedEventId == event.EventID
            val statusLabel = eventStatusLabel(event.EventStatusID).first

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.EventName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ID: ${event.EventID}  •  " +
                                        "${eventPlayers.size} players  •  " +
                                        "${eventCaches.size} caches",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "📅 ${formatEventDate(event.EventStart)} — ${formatEventDate(event.EventFinish)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            expandedEventId = if (isExpanded) -1 else event.EventID
                        }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp
                                else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Players in this event:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))

                        if (eventPlayers.isEmpty()) {
                            Text(
                                text = "No players yet",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            eventPlayers.forEach { player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${player.PlayerUser?.UserUsername ?: "User #${player.PlayerUserID}"}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Player ID: ${player.PlayerID}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Caches in this event:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))

                        if (eventCaches.isEmpty()) {
                            Text(
                                text = "No caches yet",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            eventCaches.forEach { cache ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${cache.CacheName}",
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${cache.CachePoints.toInt()} pts",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Create New Cache",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fill in all fields to add a new cache to an event.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        AdminTextField(
            value = cacheName,
            onValueChange = { cacheName = it },
            label = "Cache Name"
        )
        AdminTextField(
            value = cacheDescription,
            onValueChange = { cacheDescription = it },
            label = "Description",
            minLines = 3
        )
        AdminTextField(
            value = cacheEventId,
            onValueChange = { cacheEventId = it },
            label = "Event ID (e.g. 245)"
        )
        AdminTextField(
            value = cacheImageUrl,
            onValueChange = { cacheImageUrl = it },
            label = "Image URL"
        )
        AdminTextField(
            value = cacheClue,
            onValueChange = { cacheClue = it },
            label = "Clue",
            minLines = 2
        )
        AdminTextField(
            value = cachePoints,
            onValueChange = { cachePoints = it },
            label = "Points (e.g. 20)"
        )
        AdminTextField(
            value = cacheLatitude,
            onValueChange = { cacheLatitude = it },
            label = "Latitude (e.g. 51.4109)"
        )
        AdminTextField(
            value = cacheLongitude,
            onValueChange = { cacheLongitude = it },
            label = "Longitude (e.g. -0.3081)"
        )

        resultMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.startsWith("✓"))
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
                    color = if (msg.startsWith("✓"))
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
                    eventId == null || points == null ||
                    lat == null || lng == null || cacheClue.isBlank()
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
                            resultMessage = "✓ Cache '$cacheName' created successfully!"
                            cacheName = ""
                            cacheDescription = ""
                            cacheEventId = ""
                            cacheImageUrl = ""
                            cacheClue = ""
                            cachePoints = ""
                            cacheLatitude = ""
                            cacheLongitude = ""
                        } else {
                            resultMessage = "Failed: HTTP ${response.code()}"
                        }
                    } catch (e: Exception) {
                        resultMessage = "Error: ${e.localizedMessage}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Create New Event",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fill in the details to create a new GeoQuest event.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        AdminTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = "Event Name"
        )
        AdminTextField(
            value = eventDescription,
            onValueChange = { eventDescription = it },
            label = "Description",
            minLines = 3
        )
        AdminTextField(
            value = eventStart,
            onValueChange = { eventStart = it },
            label = "Start Date (yyyy-MM-ddTHH:mm:ss.SSSZ)"
        )
        AdminTextField(
            value = eventFinish,
            onValueChange = { eventFinish = it },
            label = "Finish Date (yyyy-MM-ddTHH:mm:ss.SSSZ)"
        )

        Text(
            text = "Event Status",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val statuses = listOf(
            "1" to "⏳ Pending",
            "2" to "🟢 Active",
            "3" to "⏸ Paused",
            "4" to "❌ Cancelled",
            "5" to "✅ Completed"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statuses.forEach { (id, label) ->
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
                    text = if (eventIsPublic) "🌍 Public Event" else "🔒 Private Event",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (eventIsPublic) "Anyone can join" else "Invite only",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = eventIsPublic,
                onCheckedChange = { eventIsPublic = it }
            )
        }

        resultMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.startsWith("✓"))
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
                    color = if (msg.startsWith("✓"))
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
                            resultMessage = "✓ Event '$eventName' created successfully!"
                            eventName = ""
                            eventDescription = ""
                            eventStart = "2026-04-01T00:00:00.000Z"
                            eventFinish = "2026-09-01T23:59:59.000Z"
                            eventStatusId = "1"
                            eventIsPublic = true
                        } else {
                            resultMessage = "Failed: HTTP ${response.code()}"
                        }
                    } catch (e: Exception) {
                        resultMessage = "Error: ${e.localizedMessage}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
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