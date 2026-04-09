package com.dan.mad_project_geoquest.components.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LogFindChoiceDialog(
    cacheName: String,
    onTakePhoto: () -> Unit,
    onSkipPhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Find") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "You've reached $cacheName!", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Would you like to take a photo to record your discovery?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onTakePhoto) { Text("📸  Take Photo") }
        },
        dismissButton = {
            OutlinedButton(onClick = onSkipPhoto) { Text("Skip — Log Without Photo") }
        }
    )
}