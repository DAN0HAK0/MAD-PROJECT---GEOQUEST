package com.dan.mad_project_geoquest.components.stats

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.ui.theme.Sand

@Composable
fun BrownStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtitle: String,
    containerColor: Color = CardBrown
) {
    Card(
        modifier = modifier.border(1.5.dp, GoldBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Sand, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GoldText)
            Text(subtitle, fontSize = 11.sp, color = CreamText.copy(alpha = 0.65f))
        }
    }
}