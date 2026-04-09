package com.dan.mad_project_geoquest.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dan.mad_project_geoquest.api.Cache

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheBottomSheet(
    cache: Cache,
    isInRange: Boolean,
    isFound: Boolean,
    clueUnlocked: Boolean,
    onDismiss: () -> Unit,
    onLogFind: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            when {
                isFound   -> FoundCacheContent(cache = cache, onDismiss = onDismiss)
                isInRange -> InRangeCacheContent(cache = cache, onLogFind = onLogFind)
                else      -> LockedCacheContent(cache = cache, clueUnlocked = clueUnlocked)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}


@Composable
fun LockedCacheContent(cache: Cache, clueUnlocked: Boolean) {
    Text(text = cache.CacheName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    cache.CacheEvent?.let {
        Text(
            text = it.EventName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Out of Range",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "You need to be within 50 metres of this cache to log a find.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }

    if (clueUnlocked && cache.CacheClue.isNotBlank()) {
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "🔍 Clue unlocked",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = cache.CacheClue,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    Text(
        "${cache.CachePoints.toInt()} points available",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
}



@Composable
fun InRangeCacheContent(cache: Cache, onLogFind: () -> Unit) {
    if (cache.CacheImageURL.isNotBlank()) {
        AsyncImage(
            model = cache.CacheImageURL,
            contentDescription = cache.CacheName,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(16.dp))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "You are in range",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(text = cache.CacheName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    cache.CacheEvent?.let {
        Text(
            text = it.EventName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(12.dp))
    if (cache.CacheDescription.isNotBlank()) {
        Text(text = cache.CacheDescription, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
    }
    if (cache.CacheClue.isNotBlank()) {
        Text(
            "Clue: ${cache.CacheClue}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
    }
    Text(
        "Points: ${cache.CachePoints.toInt()}",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onLogFind,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("📸  Log This Find", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun FoundCacheContent(cache: Cache, onDismiss: () -> Unit) {
    if (cache.CacheImageURL.isNotBlank()) {
        AsyncImage(
            model = cache.CacheImageURL,
            contentDescription = cache.CacheName,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(16.dp))
    }
    Text(text = cache.CacheName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    cache.CacheEvent?.let {
        Text(
            text = it.EventName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(12.dp))
    if (cache.CacheDescription.isNotBlank()) {
        Text(text = cache.CacheDescription, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
    }
    if (cache.CacheClue.isNotBlank()) {
        Text(
            "Clue: ${cache.CacheClue}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
    }
    Text(
        "Points: ${cache.CachePoints.toInt()}",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
    Spacer(Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Already Found",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "You have already logged this cache.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Close", fontSize = 16.sp)
    }
}