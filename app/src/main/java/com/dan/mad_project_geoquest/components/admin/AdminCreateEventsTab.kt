package com.dan.mad_project_geoquest.components.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.EventPayload
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AdminCreateEventTab() {
    val scope = rememberCoroutineScope()
    var eventName        by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventStart       by remember { mutableStateOf("2026-04-01T00:00:00.000Z") }
    var eventFinish      by remember { mutableStateOf("2026-09-01T23:59:59.000Z") }
    var eventStatusId    by remember { mutableStateOf("1") }
    var eventIsPublic    by remember { mutableStateOf(true) }
    var resultMessage    by remember { mutableStateOf<String?>(null) }
    var isLoading        by remember { mutableStateOf(false) }

    val statuses = listOf(
        "1" to "Pending", "2" to "Active", "3" to "Paused",
        "4" to "Cancelled", "5" to "Completed"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create New Event", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Fill in the details to create a new GeoQuest event.", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        AdminTextField(eventName,        { eventName = it },        "Event Name")
        AdminTextField(eventDescription, { eventDescription = it }, "Description", minLines = 3)
        AdminTextField(eventStart,       { eventStart = it },       "Start Date (yyyy-MM-ddTHH:mm:ss.SSSZ)")
        AdminTextField(eventFinish,      { eventFinish = it },      "Finish Date (yyyy-MM-ddTHH:mm:ss.SSSZ)")
        Text(
            "Event Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    msg, modifier = Modifier.padding(12.dp), fontSize = 13.sp,
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
                isLoading = true; resultMessage = null
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.createEvent(
                            EventPayload(
                                eventName.trim(), eventDescription.trim(), currentUser.UserID,
                                eventIsPublic, eventStart.trim(), eventFinish.trim(), statusId
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
                CircularProgressIndicator(
                    Modifier.size(20.dp),
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