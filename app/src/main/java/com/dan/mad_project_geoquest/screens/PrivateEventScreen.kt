package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.CachePayload
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.EventPayload
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import kotlinx.coroutines.launch
import com.dan.mad_project_geoquest.components.formatEventDate
import com.dan.mad_project_geoquest.components.eventStatusLabel
import com.dan.mad_project_geoquest.components.admin.AdminEditCacheDialog
import com.dan.mad_project_geoquest.components.admin.AdminEditEventForm
import com.dan.mad_project_geoquest.components.admin.AdminCacheCard
import com.dan.mad_project_geoquest.components.admin.AdminTextField
// ── Entry point — shows owned events list ─────────────────────────

@Composable
fun MyEventsScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var ownedEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    fun reload() {
        scope.launch {
            isLoading = true
            try {
                val all = RetrofitClient.instance.getEvents()
                ownedEvents = all.filter {
                    it.EventOwnerID == SessionManager.currentUser?.UserID && !it.EventIspublic
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    selectedEvent?.let { event ->
        MyEventDetailScreen(
            event = event,
            onBack = {
                selectedEvent = null
                reload()
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "My Private Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { showCreateForm = !showCreateForm }) {
                Text(if (showCreateForm) "Cancel" else "+ New Event")
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            resultMessage?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.contains("success", ignoreCase = true) || msg.contains("created", ignoreCase = true))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(msg, fontSize = 13.sp)
                            TextButton(onClick = { resultMessage = null }) { Text("OK") }
                        }
                    }
                }
            }

            if (showCreateForm) {
                item {
                    CreatePrivateEventForm(
                        onCancel = { showCreateForm = false },
                        onCreated = { msg ->
                            resultMessage = msg
                            showCreateForm = false
                            reload()
                        }
                    )
                }
            }

            if (ownedEvents.isEmpty() && !showCreateForm) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No private events yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap '+ New Event' to create one",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(ownedEvents) { event ->
                    OwnedEventCard(
                        event = event,
                        onClick = { selectedEvent = event }
                    )
                }
            }
        }
    }
}

// ── Owned event card ──────────────────────────────────────────────

@Composable
fun OwnedEventCard(event: Event, onClick: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.EventName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${formatEventDate(event.EventStart)} — ${formatEventDate(event.EventFinish)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Manage", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Invite code chip — share this with participants
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Invite code",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            "${event.EventID}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("${event.EventID}"))
                            copied = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(if (copied) "Copied!" else "Copy", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ── Event detail — manage caches ──────────────────────────────────

@Composable
fun MyEventDetailScreen(event: Event, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var caches by remember { mutableStateOf<List<Cache>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddCache by remember { mutableStateOf(false) }
    var showEditEvent by remember { mutableStateOf(false) }
    var selectedCache by remember { mutableStateOf<Cache?>(null) }
    var deleteConfirmCache by remember { mutableStateOf<Cache?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var currentEvent by remember { mutableStateOf(event) }

    fun reloadCaches() {
        scope.launch {
            try {
                caches = RetrofitClient.instance.getCaches()
                    .filter { it.CacheEventID == currentEvent.EventID }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { reloadCaches() }

    selectedCache?.let { cache ->
        AdminEditCacheDialog(
            cache = cache,
            onDismiss = { selectedCache = null },
            onSave = { payload ->
                scope.launch {
                    try {
                        val r = RetrofitClient.instance.updateCache(cache.CacheID, payload)
                        resultMessage = if (r.isSuccessful) "Cache updated" else "Failed: HTTP ${r.code()}"
                        reloadCaches()
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
                                val r = RetrofitClient.instance.deleteCache(cache.CacheID)
                                resultMessage = if (r.isSuccessful) "Cache deleted" else "Failed: HTTP ${r.code()}"
                                caches = caches.filter { it.CacheID != cache.CacheID }
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                currentEvent.EventName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showEditEvent = !showEditEvent }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            resultMessage?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.contains("success", ignoreCase = true) || msg.contains("updated") || msg.contains("deleted"))
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(msg, fontSize = 13.sp)
                            TextButton(onClick = { resultMessage = null }) { Text("OK") }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Invite code — share this with participants",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            "${currentEvent.EventID}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
                                    val r = RetrofitClient.instance.updateEvent(currentEvent.EventID, payload)
                                    if (r.isSuccessful) {
                                        resultMessage = "Event updated"
                                        showEditEvent = false
                                        val updated = RetrofitClient.instance.getEvents()
                                        updated.find { it.EventID == currentEvent.EventID }?.let { currentEvent = it }
                                    } else {
                                        resultMessage = "Failed: HTTP ${r.code()}"
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Caches (${caches.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { showAddCache = !showAddCache }) {
                        Text(if (showAddCache) "Cancel" else "+ Add Cache")
                    }
                }
            }

            if (showAddCache) {
                item {
                    AddCacheForm(
                        eventId = currentEvent.EventID,
                        onCancel = { showAddCache = false },
                        onCreated = { msg ->
                            resultMessage = msg
                            showAddCache = false
                            reloadCaches()
                        }
                    )
                }
            }

            if (caches.isEmpty()) {
                item {
                    Text(
                        "No caches yet — add one above",
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

// ── Create private event form ─────────────────────────────────────

@Composable
fun CreatePrivateEventForm(
    onCancel: () -> Unit,
    onCreated: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventStart by remember { mutableStateOf("2026-04-01T00:00:00.000Z") }
    var eventFinish by remember { mutableStateOf("2026-09-01T23:59:59.000Z") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("New Private Event", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "Only people with the invite code can join.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AdminTextField(value = eventName, onValueChange = { eventName = it }, label = "Event Name (min. 8 characters)")
            AdminTextField(value = eventDescription, onValueChange = { eventDescription = it }, label = "Description", minLines = 2)
            AdminTextField(value = eventStart, onValueChange = { eventStart = it }, label = "Start (yyyy-MM-ddTHH:mm:ss.SSSZ)")
            AdminTextField(value = eventFinish, onValueChange = { eventFinish = it }, label = "Finish (yyyy-MM-ddTHH:mm:ss.SSSZ)")

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val user = SessionManager.currentUser
                        when {
                            user == null -> errorMessage = "Not logged in"
                            eventName.trim().length < 8 -> errorMessage = "Event name must be at least 8 characters"
                            eventDescription.isBlank() -> errorMessage = "Please enter a description"
                            else -> {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.instance.createEvent(
                                            EventPayload(
                                                EventName = eventName.trim(),
                                                EventDescription = eventDescription.trim(),
                                                EventOwnerID = user.UserID,
                                                EventIspublic = false,
                                                EventStart = eventStart.trim(),
                                                EventFinish = eventFinish.trim(),
                                                EventStatusID = 2
                                            )
                                        )
                                        if (response.isSuccessful) {
                                            onCreated("Event '$eventName' created! Share the invite code with participants.")
                                        } else {
                                            errorMessage = "Failed: HTTP ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e::class.simpleName}: ${e.localizedMessage}"
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Create")
                    }
                }
            }
        }
    }
}

// ── Add cache form (inside a user's own event) ────────────────────

@Composable
fun AddCacheForm(
    eventId: Int,
    onCancel: () -> Unit,
    onCreated: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var cacheName by remember { mutableStateOf("") }
    var cacheDescription by remember { mutableStateOf("") }
    var cacheClue by remember { mutableStateOf("") }
    var cachePoints by remember { mutableStateOf("") }
    var cacheLatitude by remember { mutableStateOf("") }
    var cacheLongitude by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Add Cache", fontSize = 16.sp, fontWeight = FontWeight.Bold)

            AdminTextField(value = cacheName, onValueChange = { cacheName = it }, label = "Cache Name")
            AdminTextField(value = cacheDescription, onValueChange = { cacheDescription = it }, label = "Description", minLines = 2)
            AdminTextField(value = cacheClue, onValueChange = { cacheClue = it }, label = "Clue")
            AdminTextField(value = cachePoints, onValueChange = { cachePoints = it }, label = "Points (e.g. 10)")
            AdminTextField(value = cacheLatitude, onValueChange = { cacheLatitude = it }, label = "Latitude (e.g. 51.4109)")
            AdminTextField(value = cacheLongitude, onValueChange = { cacheLongitude = it }, label = "Longitude (e.g. -0.3081)")

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val points = cachePoints.toDoubleOrNull()
                        val lat = cacheLatitude.toDoubleOrNull()
                        val lng = cacheLongitude.toDoubleOrNull()
                        if (cacheName.isBlank() || points == null || lat == null || lng == null) {
                            errorMessage = "Please fill in all fields correctly"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val response = RetrofitClient.instance.createCache(
                                    CachePayload(
                                        CacheName = cacheName.trim(),
                                        CacheDescription = cacheDescription.trim(),
                                        CacheEventID = eventId,
                                        CacheImageURL = "https://static.generated.photos/vue-static/face-generator/landing/wall/1.jpg",
                                        CacheClue = cacheClue.trim(),
                                        CachePoints = points,
                                        CacheLatitude = lat,
                                        CacheLongitude = lng
                                    )
                                )
                                if (response.isSuccessful) {
                                    onCreated("Cache '$cacheName' added!")
                                } else {
                                    errorMessage = "Failed: HTTP ${response.code()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.localizedMessage}"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Add")
                    }
                }
            }
        }
    }
}