package com.dan.mad_project_geoquest.components.lb

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dan.mad_project_geoquest.api.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFilterDropdown(
    availableEvents: List<Event>,
    selectedEventId: Int?,
    isPublicTab: Boolean,
    onEventSelected: (Int?) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedLabel = availableEvents
        .find { it.EventID == selectedEventId }
        ?.EventName
        ?: if (isPublicTab) "All public events" else "All private events"

    ExposedDropdownMenuBox(
        expanded = dropdownExpanded,
        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (isPublicTab) "All public events" else "All private events") },
                onClick = { onEventSelected(null); dropdownExpanded = false }
            )
            HorizontalDivider()
            availableEvents.forEach { event ->
                DropdownMenuItem(
                    text = { Text(event.EventName) },
                    onClick = { onEventSelected(event.EventID); dropdownExpanded = false }
                )
            }
        }
    }
}