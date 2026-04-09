package com.dan.mad_project_geoquest.components

import androidx.compose.ui.graphics.Color
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
        1 -> Pair("Pending",   Color(0xFFFF9800))
        2 -> Pair("Active",    Color(0xFF4CAF50))
        3 -> Pair("Paused",    Color(0xFF9E9E9E))
        4 -> Pair("Cancelled", Color(0xFFF44336))
        5 -> Pair("Completed", Color(0xFF2196F3))
        else -> Pair("Active", Color(0xFF4CAF50))
    }
}