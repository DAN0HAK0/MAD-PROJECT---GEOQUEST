package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Gold
import com.dan.mad_project_geoquest.ui.theme.Sand
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel
import java.text.SimpleDateFormat
import java.util.Locale

fun formatEventDate(dateStr: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
        val output = SimpleDateFormat("dd MMM yyyy", Locale.UK)
        output.format(input.parse(dateStr)!!)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}

fun eventStatusLabel(statusId: Int): Pair<String, Color> {
    return when (statusId) {
        1 -> Pair("Pending", Color(0xFFFF9800))
        2 -> Pair("Active", Color(0xFF4CAF50))
        3 -> Pair("Paused", Color(0xFF9E9E9E))
        4 -> Pair("Cancelled", Color(0xFFF44336))
        5 -> Pair("Completed", Color(0xFF2196F3))
        else -> Pair("Active", Color(0xFF4CAF50))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(cacheViewModel: CacheViewModel) {
    val homeState by cacheViewModel.homeUiState.collectAsState()
    val user = SessionManager.currentUser
    var joinMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Events", "Completed", "In Progress")

    LaunchedEffect(Unit) {
        cacheViewModel.loadHomeData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GeoQuest",
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome, ${user?.UserUsername ?: "Explorer"}!",
                            fontSize = 12.sp,
                            color = Sand
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { cacheViewModel.loadHomeData() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBrown,
                    titleContentColor = Cream,
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
            Column(modifier = Modifier.fillMaxSize()) {

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkBrown,
                    contentColor = Cream,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Gold
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            selectedContentColor = Cream,
                            unselectedContentColor = Sand
                        )
                    }
                }

                if (homeState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@Column
                }

                homeState.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                val foundCacheIds = homeState.myFinds.map { it.FindCacheID }.toSet()
                val currentUser = SessionManager.currentUser

                val filteredEvents = when (selectedTab) {
                    0 -> homeState.activeEvents
                    1 -> homeState.activeEvents.filter { event ->
                        val eventCaches = homeState.allCaches.filter {
                            it.CacheEventID == event.EventID
                        }
                        val isJoined = homeState.allPlayers.any {
                            it.PlayerUserID == currentUser?.UserID &&
                                    it.PlayerEventID == event.EventID
                        }
                        if (!isJoined || eventCaches.isEmpty()) return@filter false
                        val foundCount = eventCaches.count { it.CacheID in foundCacheIds }
                        foundCount == eventCaches.size
                    }
                    2 -> homeState.activeEvents.filter { event ->
                        val eventCaches = homeState.allCaches.filter {
                            it.CacheEventID == event.EventID
                        }
                        val isJoined = homeState.allPlayers.any {
                            it.PlayerUserID == currentUser?.UserID &&
                                    it.PlayerEventID == event.EventID
                        }
                        if (!isJoined || eventCaches.isEmpty()) return@filter false
                        val foundCount = eventCaches.count { it.CacheID in foundCacheIds }
                        foundCount < eventCaches.size
                    }
                    else -> emptyList()
                }

                if (filteredEvents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "No events available"
                                    1 -> "No completed events yet"
                                    else -> "No events in progress"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = when (selectedTab) {
                                    0 -> "Check back soon!"
                                    1 -> "Find all caches in an event to complete it"
                                    else -> "Join an event and start finding caches"
                                },
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredEvents) { event ->
                            val eventCaches = homeState.allCaches.filter {
                                it.CacheEventID == event.EventID
                            }
                            val isJoined = homeState.allPlayers.any {
                                it.PlayerUserID == currentUser?.UserID &&
                                        it.PlayerEventID == event.EventID
                            }
                            EventCard(
                                event = event,
                                eventCaches = eventCaches,
                                foundCacheIds = foundCacheIds,
                                isJoined = isJoined,
                                onJoin = {
                                    cacheViewModel.joinEvent(event) { _, message ->
                                        joinMessage = message
                                    }
                                }
                            )
                        }
                    }
                }
            }

            joinMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    action = {
                        TextButton(onClick = { joinMessage = null }) { Text("OK") }
                    }
                ) { Text(msg) }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    eventCaches: List<Cache>,
    foundCacheIds: Set<Int>,
    isJoined: Boolean,
    onJoin: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val foundCount = eventCaches.count { it.CacheID in foundCacheIds }
    val totalCount = eventCaches.size
    val progress = if (totalCount > 0) foundCount.toFloat() / totalCount.toFloat() else 0f
    val statusLabel = eventStatusLabel(event.EventStatusID).first
    val statusColor = eventStatusLabel(event.EventStatusID).second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isJoined) expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.EventName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (event.EventIspublic) "Public" else "Private",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "|",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${formatEventDate(event.EventStart)} — ${formatEventDate(event.EventFinish)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isJoined) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Button(
                        onClick = onJoin,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Join", fontSize = 13.sp)
                    }
                }
            }

            if (isJoined) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "$foundCount / $totalCount caches found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
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
                            val isFound = cache.CacheID in foundCacheIds
                            CacheListItem(cache = cache, isFound = isFound)
                            Spacer(Modifier.height(8.dp))
                        }
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
fun CacheListItem(cache: Cache, isFound: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFound)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isFound) cache.CacheName else "Locked",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFound)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(2.dp))
                if (isFound) {
                    Text(
                        text = cache.CacheDescription.take(80) +
                                if (cache.CacheDescription.length > 80) "..." else "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Find this cache to unlock its details",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFound)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
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
        }
    }
}