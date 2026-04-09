package com.dan.mad_project_geoquest.components.stats

import java.text.SimpleDateFormat
import java.util.Locale

fun formatFindDateTime(dateStr: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
        val output = SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.UK)
        output.format(input.parse(dateStr)!!)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}