package com.dan.mad_project_geoquest.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferencesSection(
    modifier: Modifier = Modifier,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled      by remember { mutableStateOf(true) }
    var nearbyCacheAlerts    by remember { mutableStateOf(true) }
    var eventUpdates         by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        SectionTitle("Appearance")
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ToggleRow(
                    label = "Dark Mode",
                    sublabel = if (darkTheme) "Explorer's night journal" else "Parchment & daylight",
                    checked = darkTheme,
                    onToggle = { onThemeToggle() }
                )
            }
        }

        SectionTitle("Notifications")
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ToggleRow(
                    label = "Push Notifications",
                    sublabel = "Master toggle for all notifications",
                    checked = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )
                HorizontalDivider()
                ToggleRow(
                    label = "Nearby Cache Alerts",
                    sublabel = "Notify when a cache is within range",
                    checked = nearbyCacheAlerts && notificationsEnabled,
                    onToggle = { if (notificationsEnabled) nearbyCacheAlerts = it }
                )
                HorizontalDivider()
                ToggleRow(
                    label = "Event Updates",
                    sublabel = "News about events you've joined",
                    checked = eventUpdates && notificationsEnabled,
                    onToggle = { if (notificationsEnabled) eventUpdates = it }
                )
            }
        }

        SectionTitle("Location")
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ToggleRow(
                    label = "Location Tracking",
                    sublabel = "Required for proximity detection",
                    checked = locationEnabled,
                    onToggle = { locationEnabled = it }
                )
            }
        }

        if (!locationEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Location is required to find nearby caches. Some features will not work without it.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}