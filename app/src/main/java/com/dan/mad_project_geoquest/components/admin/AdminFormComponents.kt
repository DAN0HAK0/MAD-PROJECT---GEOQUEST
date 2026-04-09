package com.dan.mad_project_geoquest.components.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.CachePayload
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.EventPayload
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.api.User

// ── Edit Event Form ───────────────────────────────────────────────

@Composable
fun AdminEditEventForm(event: Event, onSave: (EventPayload) -> Unit, onCancel: () -> Unit) {
    var eventName        by remember { mutableStateOf(event.EventName) }
    var eventDescription by remember { mutableStateOf(event.EventDescription) }
    var eventStart       by remember { mutableStateOf(event.EventStart) }
    var eventFinish      by remember { mutableStateOf(event.EventFinish) }
    var eventStatusId    by remember { mutableStateOf("${event.EventStatusID}") }
    var eventIsPublic    by remember { mutableStateOf(event.EventIspublic) }
    var errorMessage     by remember { mutableStateOf<String?>(null) }

    val statuses = listOf(
        "1" to "Pending", "2" to "Active", "3" to "Paused",
        "4" to "Cancelled", "5" to "Completed"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit Event", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            AdminTextField(eventName,        { eventName = it },        "Event Name")
            AdminTextField(eventDescription, { eventDescription = it }, "Description", minLines = 2)
            AdminTextField(eventStart,       { eventStart = it },       "Start (yyyy-MM-ddTHH:mm:ss.SSSZ)")
            AdminTextField(eventFinish,      { eventFinish = it },      "Finish (yyyy-MM-ddTHH:mm:ss.SSSZ)")
            Text("Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                        if (eventIsPublic) "Public Event" else "Private Event",
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (eventIsPublic) "Anyone can join" else "Invite only",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = eventIsPublic, onCheckedChange = { eventIsPublic = it })
            }
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
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
                        val statusId = eventStatusId.toIntOrNull() ?: 1
                        val currentUser = SessionManager.currentUser
                        if (eventName.isBlank() || eventDescription.isBlank() || currentUser == null) {
                            errorMessage = "Please fill in all fields"
                            return@Button
                        }
                        onSave(
                            EventPayload(
                                eventName.trim(), eventDescription.trim(),
                                currentUser.UserID, eventIsPublic, eventStart.trim(),
                                eventFinish.trim(), statusId
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Save Changes") }
            }
        }
    }
}

// ── Edit Cache Dialog ─────────────────────────────────────────────

@Composable
fun AdminEditCacheDialog(cache: Cache, onDismiss: () -> Unit, onSave: (CachePayload) -> Unit) {
    var cacheName        by remember { mutableStateOf(cache.CacheName) }
    var cacheDescription by remember { mutableStateOf(cache.CacheDescription) }
    var cacheImageUrl    by remember { mutableStateOf(cache.CacheImageURL) }
    var cacheClue        by remember { mutableStateOf(cache.CacheClue) }
    var cachePoints      by remember { mutableStateOf("${cache.CachePoints}") }
    var cacheLatitude    by remember { mutableStateOf("${cache.CacheLatitude}") }
    var cacheLongitude   by remember { mutableStateOf("${cache.CacheLongitude}") }
    var errorMessage     by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cache", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminTextField(cacheName,        { cacheName = it },        "Cache Name")
                AdminTextField(cacheDescription, { cacheDescription = it }, "Description", minLines = 2)
                AdminTextField(cacheImageUrl,    { cacheImageUrl = it },    "Image URL")
                AdminTextField(cacheClue,        { cacheClue = it },        "Clue", minLines = 2)
                AdminTextField(cachePoints,      { cachePoints = it },      "Points")
                AdminTextField(cacheLatitude,    { cacheLatitude = it },    "Latitude")
                AdminTextField(cacheLongitude,   { cacheLongitude = it },   "Longitude")
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val points = cachePoints.toDoubleOrNull()
                val lat    = cacheLatitude.toDoubleOrNull()
                val lng    = cacheLongitude.toDoubleOrNull()
                if (cacheName.isBlank() || points == null || lat == null || lng == null) {
                    errorMessage = "Please fill in all fields correctly"
                    return@Button
                }
                onSave(
                    CachePayload(
                        cacheName.trim(), cacheDescription.trim(), cache.CacheEventID,
                        cacheImageUrl.trim().ifBlank {
                            "https://static.generated.photos/vue-static/face-generator/landing/wall/1.jpg"
                        },
                        cacheClue.trim(), points, lat, lng
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Delete User Dialog ────────────────────────────────────────────

@Composable
fun AdminDeleteUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    var enteredPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var error            by remember { mutableStateOf(false) }
    var isDeleting       by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete User", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "You are about to permanently delete the account for " +
                            "\"${user.UserUsername}\". This cannot be undone.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Enter this user's password to confirm deletion:",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = enteredPassword,
                    onValueChange = { enteredPassword = it; error = false },
                    label = { Text("User's password") },
                    isError = error,
                    supportingText = {
                        if (error) Text(
                            "Incorrect password — deletion cancelled",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.KeyboardArrowUp else Icons.Filled.Close,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredPassword == user.UserPassword) {
                        isDeleting = true
                        onConfirmed()
                    } else {
                        error = true
                        enteredPassword = ""
                    }
                },
                enabled = !isDeleting && enteredPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Delete User")
                }
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}