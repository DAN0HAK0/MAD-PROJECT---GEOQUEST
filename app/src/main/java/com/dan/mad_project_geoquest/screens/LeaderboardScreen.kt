package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel
import com.dan.mad_project_geoquest.viewmodel.LeaderboardEntry

@Composable
fun LeaderboardScreen(cacheViewModel: CacheViewModel) {
    val state by cacheViewModel.leaderboardUiState.collectAsState()

    LaunchedEffect(Unit) {
        cacheViewModel.loadLeaderboard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Leaderboard",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { cacheViewModel.loadLeaderboard() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }

        Text(
            text = "Ranked by caches found",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }

        if (state.entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data yet — go find some caches!", fontSize = 16.sp)
            }
        } else {
            // Podium for top 3
            if (state.entries.size >= 3) {
                Podium(
                    first = state.entries[0],
                    second = state.entries[1],
                    third = state.entries[2]
                )
                Spacer(Modifier.height(24.dp))
            }

            // Rest of the list from 4th place onwards
            val remainingEntries = if (state.entries.size > 3) state.entries.drop(3) else emptyList()

            if (remainingEntries.isNotEmpty()) {
                Text(
                    text = "Other Players",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(remainingEntries) { _, entry ->
                        LeaderboardRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
fun Podium(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry
) {
    val goldColor = Color(0xFFFFD700)
    val silverColor = Color(0xFFC0C0C0)
    val bronzeColor = Color(0xFFCD7F32)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 2nd place
        PodiumColumn(
            entry = second,
            medal = "🥈",
            color = silverColor,
            heightDp = 90,
            rankLabel = "2nd"
        )

        // 1st place — tallest
        PodiumColumn(
            entry = first,
            medal = "🥇",
            color = goldColor,
            heightDp = 120,
            rankLabel = "1st"
        )

        // 3rd place
        PodiumColumn(
            entry = third,
            medal = "🥉",
            color = bronzeColor,
            heightDp = 70,
            rankLabel = "3rd"
        )
    }
}

@Composable
fun PodiumColumn(
    entry: LeaderboardEntry,
    medal: String,
    color: Color,
    heightDp: Int,
    rankLabel: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Medal emoji
        Text(text = medal, fontSize = 28.sp)

        Spacer(Modifier.height(4.dp))

        // Username
        Text(
            text = entry.username,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (entry.isCurrentUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )

        // Find count
        Text(
            text = "${entry.findCount} finds",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        // Podium block
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(heightDp.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rankLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (entry.isCurrentUser) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "YOU",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${entry.findCount} finds",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}