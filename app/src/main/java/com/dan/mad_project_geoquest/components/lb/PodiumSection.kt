package com.dan.mad_project_geoquest.components.lb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.viewmodel.LeaderboardEntry

@Composable
fun Podium(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry,
    subtitle: (LeaderboardEntry) -> String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PodiumColumn(second, "🥈", Color(0xFFC0C0C0), 90,  "2nd", subtitle(second))
        PodiumColumn(first,  "🥇", Color(0xFFFFD700), 120, "1st", subtitle(first))
        PodiumColumn(third,  "🥉", Color(0xFFCD7F32), 70,  "3rd", subtitle(third))
    }
}

@Composable
fun MiniPodium(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    subtitle: (LeaderboardEntry) -> String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PodiumColumn(second, "🥈", Color(0xFFC0C0C0), 90,  "2nd", subtitle(second))
        PodiumColumn(first,  "🥇", Color(0xFFFFD700), 120, "1st", subtitle(first))
    }
}

@Composable
fun PodiumColumn(
    entry: LeaderboardEntry,
    medal: String,
    color: Color,
    heightDp: Int,
    rankLabel: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(medal, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            entry.username,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (entry.isCurrentUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
        Text(
            subtitle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(heightDp.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Text(rankLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}