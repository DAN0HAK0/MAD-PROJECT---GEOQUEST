package com.dan.mad_project_geoquest.screens

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

private const val PROXIMITY_RADIUS_METRES = 50f

fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0]
}

@Composable
fun MapScreen(cacheViewModel: CacheViewModel) {
    val allCaches by cacheViewModel.allCaches.collectAsState()
    val foundCaches by cacheViewModel.foundCaches.collectAsState()
    val userLocation by cacheViewModel.userLocation.collectAsState()
    val mapView = rememberMapViewWithLifecycle()

    var hasMovedCamera by remember { mutableStateOf(false) }
    var selectedCache by remember { mutableStateOf<Cache?>(null) }
    var logFindResult by remember { mutableStateOf<String?>(null) }

    val cacheSnapshot = remember(allCaches) { allCaches.toList() }
    val foundCacheIds = remember(foundCaches) { foundCaches.map { it.CacheID }.toSet() }

    // Work out which cache the user is currently within range of
    val nearbyCache = remember(userLocation, allCaches) {
        val loc = userLocation ?: return@remember null
        allCaches
            .filter { it.CacheLatitude != 0.0 && it.CacheLongitude != 0.0 }
            .filter { it.CacheID !in foundCacheIds }
            .minByOrNull { cache ->
                distanceBetween(
                    loc.latitude, loc.longitude,
                    cache.CacheLatitude, cache.CacheLongitude
                )
            }?.let { closest ->
                val distance = distanceBetween(
                    loc.latitude, loc.longitude,
                    closest.CacheLatitude, closest.CacheLongitude
                )
                if (distance <= PROXIMITY_RADIUS_METRES) closest else null
            }
    }

    LaunchedEffect(Unit) {
        cacheViewModel.loadAllCaches()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView.apply {
                getMapAsync { googleMap ->
                    try {
                        googleMap.isMyLocationEnabled = true
                        googleMap.uiSettings.isMyLocationButtonEnabled = true
                    } catch (e: SecurityException) { e.printStackTrace() }

                    val kingston = LatLng(51.4123, -0.3007)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kingston, 14f))

                    googleMap.setOnMarkerClickListener { marker ->
                        val cache = marker.tag as? Cache
                        selectedCache = cache
                        true
                    }
                }
            }},
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                mv.getMapAsync { googleMap ->
                    googleMap.clear()

                    userLocation?.let { loc ->
                        val userLatLng = LatLng(loc.latitude, loc.longitude)
                        if (!hasMovedCamera) {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                            )
                            hasMovedCamera = true
                        }
                    }

                    cacheSnapshot.forEach { cache ->
                        if (cache.CacheLatitude != 0.0 && cache.CacheLongitude != 0.0) {
                            val position = LatLng(cache.CacheLatitude, cache.CacheLongitude)
                            val isFound = cache.CacheID in foundCacheIds
                            val isNearby = nearbyCache?.CacheID == cache.CacheID

                            val hue = when {
                                isFound -> BitmapDescriptorFactory.HUE_AZURE
                                isNearby -> BitmapDescriptorFactory.HUE_YELLOW
                                else -> BitmapDescriptorFactory.HUE_RED
                            }

                            val marker: Marker? = googleMap.addMarker(
                                MarkerOptions()
                                    .position(position)
                                    .title(cache.CacheName)
                                    .snippet(
                                        when {
                                            isFound -> "Already found"
                                            isNearby -> "You are in range — tap to log"
                                            else -> "Get within 50m to unlock"
                                        }
                                    )
                                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
                            )
                            marker?.tag = cache
                        }
                    }

                    googleMap.setOnMarkerClickListener { marker ->
                        val cache = marker.tag as? Cache
                        selectedCache = cache
                        true
                    }
                }
            }
        )

        // Cache count badge
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "${allCaches.size} caches loaded",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Legend badge
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = "Red — out of range", fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Yellow — in range", fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Blue — found", fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Log find result snackbar
        logFindResult?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                action = {
                    TextButton(onClick = { logFindResult = null }) { Text("OK") }
                }
            ) { Text(msg) }
        }
    }

    // Bottom sheet — passes whether user is in range
    selectedCache?.let { cache ->
        val isInRange = nearbyCache?.CacheID == cache.CacheID
        val isFound = cache.CacheID in foundCacheIds
        CacheBottomSheet(
            cache = cache,
            isInRange = isInRange,
            isFound = isFound,
            onDismiss = { selectedCache = null },
            onLogFind = {
                cacheViewModel.logFind(cache) { success ->
                    logFindResult = if (success) "Find logged successfully."
                    else "Could not log find — try again."
                    selectedCache = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheBottomSheet(
    cache: Cache,
    isInRange: Boolean,
    isFound: Boolean,
    onDismiss: () -> Unit,
    onLogFind: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (isFound) {
                // Already found — show everything
                FoundCacheContent(cache = cache, onDismiss = onDismiss)

            } else if (isInRange) {
                // In range — show full details and log button
                InRangeCacheContent(cache = cache, onLogFind = onLogFind)

            } else {
                // Out of range — show locked view
                LockedCacheContent(cache = cache)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun LockedCacheContent(cache: Cache) {
    Text(
        text = cache.CacheName,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Out of Range",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "You need to be within 50 metres of this cache to view its details and log a find.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }

    Spacer(Modifier.height(12.dp))
    Text(
        text = "${cache.CachePoints.toInt()} points available",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
fun InRangeCacheContent(cache: Cache, onLogFind: () -> Unit) {
    // Cache image
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
                text = "You are in range",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        text = cache.CacheName,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
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
            text = "Clue: ${cache.CacheClue}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
    }
    Text(
        text = "Points: ${cache.CachePoints.toInt()}",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onLogFind,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Log This Find", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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

    Text(
        text = cache.CacheName,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
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
            text = "Clue: ${cache.CacheClue}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
    }
    Text(
        text = "Points: ${cache.CachePoints.toInt()}",
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Already Found",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "You have already logged this cache.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Close", fontSize = 16.sp)
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE  -> mapView.onCreate(android.os.Bundle())
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return mapView
}