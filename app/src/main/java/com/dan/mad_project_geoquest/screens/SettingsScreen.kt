package com.dan.mad_project_geoquest.screens

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
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel

@Composable
fun SettingsScreen(
    cacheViewModel: CacheViewModel,
    onLogout: () -> Unit
) {
    val user = SessionManager.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // ── Account Card ──────────────────────────────────────────
        SectionTitle("Account")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsInfoRow("Username", user?.UserUsername ?: "—")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("Name", "${user?.UserFirstname} ${user?.UserLastname}".trim().ifBlank { "—" })
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("Phone", user?.UserPhone?.ifBlank { "—" } ?: "—")
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Preferences Card ──────────────────────────────────────
        SectionTitle("Preferences")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ToggleRow(
                    label = "Push Notifications",
                    sublabel = "Alerts for nearby caches",
                    checked = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )
                Divider()
                ToggleRow(
                    label = "Location Tracking",
                    sublabel = "Required for proximity detection",
                    checked = locationEnabled,
                    onToggle = { locationEnabled = it }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── About Card ────────────────────────────────────────────
        SectionTitle("About")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsInfoRow("Version", "1.0.0")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("API", "mark0s.com/geoquest/v1")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("Platform", "Android · Jetpack Compose")
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Logout Button ─────────────────────────────────────────
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
        ) {
            Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
    }

    // ── Logout Dialog ──────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        cacheViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ToggleRow(label: String, sublabel: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(sublabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle
        )
    }
}