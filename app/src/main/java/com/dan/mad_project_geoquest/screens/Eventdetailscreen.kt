package com.dan.mad_project_geoquest.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Gold
import com.dan.mad_project_geoquest.viewmodel.HomeViewModel
import com.dan.mad_project_geoquest.components.formatEventDate
import com.dan.mad_project_geoquest.components.eventStatusLabel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val homeState by homeViewModel.uiState.collectAsState()
    val currentUser = SessionManager.currentUser

    val event = homeState.activeEvents.find { it.EventID == eventId }
    val eventCaches = homeState.allCaches.filter { it.CacheEventID == eventId }
    val foundCacheIds = homeState.myFinds.map { it.FindCacheID }.toSet()

    val isOwner = event?.EventOwnerID == currentUser?.UserID
    val isJoined = homeState.allPlayers.any {
        it.PlayerUserID == currentUser?.UserID && it.PlayerEventID == eventId
    }

    var showLeaveDialog by remember { mutableStateOf(false) }
    var leaveMessage by remember { mutableStateOf<String?>(null) }

    val foundCount = eventCaches.count { it.CacheID in foundCacheIds }
    val totalCount = eventCaches.size
    val progress = if (totalCount > 0) foundCount.toFloat() / totalCount.toFloat() else 0f

    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val statusLabel = eventStatusLabel(event.EventStatusID).first
    val statusColor = eventStatusLabel(event.EventStatusID).second


    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Event") },
            text = { Text("Are you sure you want to leave \"${event.EventName}\"? Your progress will be lost.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        homeViewModel.leaveEvent(event) { success, message ->
                            leaveMessage = message
                            if (success) onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Leave") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLeaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event.EventName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isOwner && isJoined) {
                        TextButton(onClick = { showLeaveDialog = true }) {
                            Text(
                                "Leave Event",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBrown,
                    titleContentColor = Cream,
                    navigationIconContentColor = Cream,
                    actionIconContentColor = Cream
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    EventDetailHeaderCard(
                        event = event,
                        statusLabel = statusLabel,
                        statusColor = statusColor,
                        isOwner = isOwner,
                        foundCount = foundCount,
                        totalCount = totalCount,
                        progress = progress
                    )
                }

                item {
                    Text(
                        text = "Caches",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    Text(
                        text = "$foundCount of $totalCount found",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (eventCaches.isEmpty()) {
                    item {
                        Text(
                            text = "No caches in this event yet.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(eventCaches) { cache ->
                        val isFound = cache.CacheID in foundCacheIds
                        ExpandableCacheDetailCard(cache = cache, isFound = isFound)
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            leaveMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    action = {
                        TextButton(onClick = { leaveMessage = null }) { Text("OK") }
                    }
                ) { Text(msg) }
            }
        }
    }
}


@Composable
fun EventDetailHeaderCard(
    event: Event,
    statusLabel: String,
    statusColor: Color,
    isOwner: Boolean,
    foundCount: Int,
    totalCount: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(label = if (event.EventIspublic) "Public" else "Private")
                StatusBadge(label = statusLabel, color = statusColor)
                if (isOwner) {
                    StatusBadge(label = "Organiser", color = Gold)
                }
            }

            Spacer(Modifier.height(10.dp))


            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${formatEventDate(event.EventStart)} — ${formatEventDate(event.EventFinish)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isOwner) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Invite Code: ${event.EventID}",
                    fontSize = 12.sp,
                    color = Gold,
                    fontWeight = FontWeight.SemiBold
                )
            }


            if (event.EventDescription.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = event.EventDescription,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$foundCount / $totalCount caches found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (totalCount > 0 && foundCount == totalCount) {
                    Text(
                        text = "✓ Completed!",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}



@Composable
fun StatusBadge(label: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}


@Composable
fun ExpandableCacheDetailCard(cache: Cache, isFound: Boolean) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isFound) { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFound)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isFound) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (!isFound) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isFound) cache.CacheName else "??? Hidden Cache",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFound)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        if (!isFound) {
                            Text(
                                text = "Find this cache to reveal its details",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isFound)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${cache.CachePoints.toInt()} pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isFound)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    if (isFound) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                            else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }


            AnimatedVisibility(
                visible = expanded && isFound,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(12.dp))

                    if (cache.CacheImageURL.isNotBlank()) {
                        AsyncImage(
                            model = cache.CacheImageURL,
                            contentDescription = cache.CacheName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        )
                        Spacer(Modifier.height(12.dp))
                    }


                    if (cache.CacheDescription.isNotBlank()) {
                        Text(
                            text = "Description",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = cache.CacheDescription,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    // Clues
                    if (cache.CacheClue.isNotBlank()) {
                        Text(
                            text = "Clue",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = cache.CacheClue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    // Coordinates of caches
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "%.5f, %.5f".format(cache.CacheLatitude, cache.CacheLongitude),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}