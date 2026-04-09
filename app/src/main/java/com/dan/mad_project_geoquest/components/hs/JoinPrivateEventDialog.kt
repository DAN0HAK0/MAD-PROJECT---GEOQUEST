package com.dan.mad_project_geoquest.components.hs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.Event

@Composable
fun JoinPrivateEventDialog(
    event: Event,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Private Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the invite code to join \"${event.EventName}\".")
                Text(
                    text = "Ask the event organiser for the invite code.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it; error = false },
                    label = { Text("Invite code") },
                    isError = error,
                    supportingText = {
                        if (error) Text("Incorrect code — check with the event organiser")
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (code.trim() == event.EventID.toString()) onConfirm()
                else error = true
            }) { Text("Join") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}