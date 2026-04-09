package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.components.stats.BrownStatCard
import com.dan.mad_project_geoquest.components.stats.CardBrown
import com.dan.mad_project_geoquest.components.stats.CardBrownLight
import com.dan.mad_project_geoquest.components.stats.CreamText
import com.dan.mad_project_geoquest.components.stats.FindRow
import com.dan.mad_project_geoquest.components.stats.GoldBorder
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Gold
import com.dan.mad_project_geoquest.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(statsViewModel: StatsViewModel) {
    val state by statsViewModel.uiState.collectAsState()
    val user = SessionManager.currentUser

    LaunchedEffect(Unit) { statsViewModel.loadStats() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Stats", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { statsViewModel.loadStats() }) {
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
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            state.errorMessage?.let { err ->
                item { Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }

            //User Profile info
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, GoldBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBrown),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = user?.UserImageURL
                                ?: "https://static.generated.photos/vue-static/face-generator/landing/wall/1.jpg",
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(3.dp, Gold, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(state.username, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CreamText)
                        Text("GeoQuest Explorer", fontSize = 13.sp, color = Gold)
                    }
                }
            }

// Showing User Level and Points
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BrownStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Level",
                        value = "${(state.totalFinds / 5) + 1}",
                        subtitle = "${state.totalFinds % 5}/5 to next"
                    )
                    BrownStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Points",
                        value = "${state.totalPoints.toInt()}",
                        subtitle = "pts earned"
                    )
                }
            }

            //Showing the total caches found by the user/player
            item {
                BrownStatCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Caches Found",
                    value = "${state.totalFinds}",
                    subtitle = "Keep exploring to find more",
                    containerColor = CardBrownLight
                )
            }

            //showing all the recent caches found by the user
            item {
                Text(
                    "Recent Finds", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (state.recentFinds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No finds yet — head to the map!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.recentFinds) { find -> FindRow(find = find) }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}