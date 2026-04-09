package com.dan.mad_project_geoquest.components.hs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.components.eventStatusLabel
import com.dan.mad_project_geoquest.components.formatEventDate
import com.dan.mad_project_geoquest.ui.theme.Gold

@Composable
fun EventCard(
    event: Event,
    eventCaches: List<Cache>,
    foundCacheIds: Set<Int>,
    isJoined: Boolean,
    isOwner: Boolean,
    onJoin: (enteredCode: String?) -> Unit,
    onViewDetails: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showCodeDialog by remember { mutableStateOf(false) }

    val showAsJoined = isJoined || isOwner
    val foundCount = eventCaches.count { it.CacheID in foundCacheIds }
    val totalCount = eventCaches.size
    val progress = if (totalCount > 0) foundCount.toFloat() / totalCount.toFloat() else 0f
    val (statusLabel, statusColor) = eventStatusLabel(event.EventStatusID)

    if (showCodeDialog) {
        JoinPrivateEventDialog(
            event = event,
            onDismiss = { showCodeDialog = false },
            onConfirm = {
                showCodeDialog = false
                onJoin(null)
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (showAsJoined) expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EventCardHeader(
                    event = event,
                    isOwner = isOwner,
                    statusLabel = statusLabel,
                    statusColor = statusColor,
                    modifier = Modifier.weight(1f)
                )

                if (showAsJoined) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Button(
                        onClick = {
                            if (event.EventIspublic) onJoin(null) else showCodeDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Join", fontSize = 13.sp)
                    }
                }
            }

            if (showAsJoined) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "$foundCount / $totalCount caches found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    trackColor = MaterialTheme.colorScheme.surface
                )

                if (expanded) {
                    Spacer(Modifier.height(16.dp))
                    if (eventCaches.isEmpty()) {
                        Text(
                            text = "No caches in this event yet.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        eventCaches.forEach { cache ->
                            CacheListItem(cache = cache, isFound = cache.CacheID in foundCacheIds)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View Full Event Details", fontSize = 14.sp)
                    }
                }

            } else {
                if (event.EventDescription.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = event.EventDescription,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$totalCount caches to discover",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}



@Composable
private fun EventCardHeader(
    event: Event,
    isOwner: Boolean,
    statusLabel: String,
    statusColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = event.EventName, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (event.EventIspublic) "Public" else "Private",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("|", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = statusLabel, fontSize = 11.sp, color = statusColor)
            if (isOwner) {
                Text("|", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Code: ${event.EventID}", fontSize = 11.sp, color = Gold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${formatEventDate(event.EventStart)} — ${formatEventDate(event.EventFinish)}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}