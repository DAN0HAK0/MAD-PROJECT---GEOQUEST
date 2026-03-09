package com.dan.mad_project_geoquest.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.database.Cache
import com.dan.mad_project_geoquest.components.CacheCard

val fakeCaches = listOf(
    Cache(1, "Hidden Treasure", "A cache hidden near the old oak tree", 51.5074, -0.1278, "Look under the biggest rock", 100),
    Cache(2, "River Cache", "Somewhere along the riverbank", 51.5080, -0.1290, "Follow the sound of water", 150),
    Cache(3, "Park Secret", "Buried near the park entrance", 51.5090, -0.1300, "Three steps from the bench", 200)
)

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "GeoQuest Caches",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(fakeCaches) { cache ->
                CacheCard(cache = cache)
            }
        }
    }
}