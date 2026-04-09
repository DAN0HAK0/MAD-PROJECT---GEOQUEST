package com.dan.mad_project_geoquest.components.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.CachePayload
import com.dan.mad_project_geoquest.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun AdminCreateCacheTab() {
    val scope = rememberCoroutineScope()
    var cacheName        by remember { mutableStateOf("") }
    var cacheDescription by remember { mutableStateOf("") }
    var cacheEventId     by remember { mutableStateOf("") }
    var cacheImageUrl    by remember { mutableStateOf("") }
    var cacheClue        by remember { mutableStateOf("") }
    var cachePoints      by remember { mutableStateOf("") }
    var cacheLatitude    by remember { mutableStateOf("") }
    var cacheLongitude   by remember { mutableStateOf("") }
    var resultMessage    by remember { mutableStateOf<String?>(null) }
    var isLoading        by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create New Cache", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Fill in all fields to add a new cache to an event.", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        AdminTextField(cacheName,        { cacheName = it },        "Cache Name")
        AdminTextField(cacheDescription, { cacheDescription = it }, "Description", minLines = 3)
        AdminTextField(cacheEventId,     { cacheEventId = it },     "Event ID (e.g. 245)")
        AdminTextField(cacheImageUrl,    { cacheImageUrl = it },    "Image URL")
        AdminTextField(cacheClue,        { cacheClue = it },        "Clue", minLines = 2)
        AdminTextField(cachePoints,      { cachePoints = it },      "Points (e.g. 20)")
        AdminTextField(cacheLatitude,    { cacheLatitude = it },    "Latitude (e.g. 51.4109)")
        AdminTextField(cacheLongitude,   { cacheLongitude = it },   "Longitude (e.g. -0.3081)")

        resultMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.startsWith("Cache"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    msg, modifier = Modifier.padding(12.dp), fontSize = 13.sp,
                    color = if (msg.startsWith("Cache"))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Button(
            onClick = {
                val eventId = cacheEventId.toIntOrNull()
                val points  = cachePoints.toDoubleOrNull()
                val lat     = cacheLatitude.toDoubleOrNull()
                val lng     = cacheLongitude.toDoubleOrNull()
                if (cacheName.isBlank() || cacheDescription.isBlank() ||
                    eventId == null || points == null || lat == null || lng == null || cacheClue.isBlank()
                ) {
                    resultMessage = "Please fill in all fields correctly"
                    return@Button
                }
                isLoading = true; resultMessage = null
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.createCache(
                            CachePayload(
                                cacheName.trim(), cacheDescription.trim(), eventId,
                                cacheImageUrl.trim().ifBlank {
                                    "https://static.generated.photos/vue-static/face-generator/landing/wall/1.jpg"
                                },
                                cacheClue.trim(), points, lat, lng
                            )
                        )
                        if (response.isSuccessful) {
                            resultMessage = "Cache '$cacheName' created successfully"
                            cacheName = ""; cacheDescription = ""; cacheEventId = ""
                            cacheImageUrl = ""; cacheClue = ""; cachePoints = ""
                            cacheLatitude = ""; cacheLongitude = ""
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
                Text("Create Cache", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}