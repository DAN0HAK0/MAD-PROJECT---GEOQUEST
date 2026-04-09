package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.components.map.CacheBottomSheet
import com.dan.mad_project_geoquest.components.map.CacheCountOverlay
import com.dan.mad_project_geoquest.components.map.LogFindChoiceDialog
import com.dan.mad_project_geoquest.components.map.MapCompassOverlay
import com.dan.mad_project_geoquest.components.map.MapLegendOverlay
import com.dan.mad_project_geoquest.components.map.distanceBetween
import com.dan.mad_project_geoquest.components.map.rememberMapViewWithLifecycle
import com.dan.mad_project_geoquest.utils.GeoQuestNotificationHelper
import com.dan.mad_project_geoquest.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

private const val PROXIMITY_RADIUS_METRES = 50f
private const val CLUE_RADIUS_METRES = 200f

@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onOpenCamera: (cache: Cache) -> Unit
) {
    val allCaches            by mapViewModel.allCaches.collectAsState()
    val foundCaches          by mapViewModel.foundCaches.collectAsState()
    val userLocation         by mapViewModel.userLocation.collectAsState()
    val logFindResult        by mapViewModel.logFindMessage.collectAsState()
    val sessionClueUnlocked  by mapViewModel.sessionClueUnlockedIds.collectAsState()
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current

    var hasMovedCamera by remember { mutableStateOf(false) }
    var selectedCache  by remember { mutableStateOf<Cache?>(null) }
    var cacheToLog     by remember { mutableStateOf<Cache?>(null) }

    val cacheSnapshot = remember(allCaches) { allCaches.toList() }
    val foundCacheIds = remember(foundCaches) { foundCaches.map { it.CacheID }.toSet() }


    val nearbyCache = remember(userLocation, allCaches) {
        val loc = userLocation ?: return@remember null
        allCaches
            .filter { it.CacheLatitude != 0.0 && it.CacheLongitude != 0.0 }
            .filter { it.CacheID !in foundCacheIds }
            .minByOrNull { cache ->
                distanceBetween(loc.latitude, loc.longitude, cache.CacheLatitude, cache.CacheLongitude)
            }?.let { closest ->
                val distance = distanceBetween(
                    loc.latitude, loc.longitude,
                    closest.CacheLatitude, closest.CacheLongitude
                )
                if (distance <= PROXIMITY_RADIUS_METRES) closest else null
            }
    }

    val clueCaches = remember(userLocation, allCaches) {
        val loc = userLocation ?: return@remember emptyList()
        allCaches
            .filter { it.CacheLatitude != 0.0 && it.CacheLongitude != 0.0 }
            .filter { it.CacheID !in foundCacheIds }
            .filter { it.CacheClue.isNotBlank() }
            .filter { cache ->
                distanceBetween(
                    loc.latitude, loc.longitude,
                    cache.CacheLatitude, cache.CacheLongitude
                ) <= CLUE_RADIUS_METRES
            }
    }

    LaunchedEffect(clueCaches) {
        clueCaches.forEach { cache ->
            if (cache.CacheID !in sessionClueUnlocked) {
                GeoQuestNotificationHelper.sendClueNotification(
                    context = context,
                    cacheTitle = cache.CacheName,
                    clue = cache.CacheClue,
                    cacheId = cache.CacheID
                )
                mapViewModel.markClueUnlocked(cache.CacheID)
            }
        }
    }

    LaunchedEffect(Unit) { mapViewModel.loadAllCaches() }

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
                        selectedCache = marker.tag as? Cache
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
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                            hasMovedCamera = true
                        }
                    }
                    cacheSnapshot.forEach { cache ->
                        if (cache.CacheLatitude != 0.0 && cache.CacheLongitude != 0.0) {
                            val position = LatLng(cache.CacheLatitude, cache.CacheLongitude)
                            val isFound  = cache.CacheID in foundCacheIds
                            val isNearby = nearbyCache?.CacheID == cache.CacheID
                            val hue = when {
                                isFound  -> BitmapDescriptorFactory.HUE_AZURE
                                isNearby -> BitmapDescriptorFactory.HUE_YELLOW
                                else     -> BitmapDescriptorFactory.HUE_RED
                            }
                            val marker: Marker? = googleMap.addMarker(
                                MarkerOptions()
                                    .position(position)
                                    .title(cache.CacheName)
                                    .snippet(when {
                                        isFound  -> "Already found"
                                        isNearby -> "You are in range — tap to log"
                                        else     -> "Get within 50m to unlock"
                                    })
                                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
                            )
                            marker?.tag = cache
                        }
                    }
                    googleMap.setOnMarkerClickListener { marker ->
                        selectedCache = marker.tag as? Cache
                        true
                    }
                }
            }
        )

        // checks how many caches have loded in
        CacheCountOverlay(
            cacheCount = allCaches.size,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
        )

            //Compass for maps (helps with clues)
            Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapLegendOverlay()
            MapCompassOverlay()
        }

        logFindResult?.let { msg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                action = {
                    TextButton(onClick = { mapViewModel.clearLogFindMessage() }) { Text("OK") }
                }
            ) { Text(msg) }
        }
    }

    selectedCache?.let { cache ->
        CacheBottomSheet(
            cache = cache,
            isInRange = nearbyCache?.CacheID == cache.CacheID,
            isFound = cache.CacheID in foundCacheIds,
            clueUnlocked = cache.CacheID in sessionClueUnlocked,
            onDismiss = { selectedCache = null },
            onLogFind = { selectedCache = null; cacheToLog = cache }
        )
    }

    //Message to give user option for photo choice
    cacheToLog?.let { cache ->
        LogFindChoiceDialog(
            cacheName = cache.CacheName,
            onTakePhoto = { cacheToLog = null; onOpenCamera(cache) },
            onSkipPhoto = {
                cacheToLog = null
                mapViewModel.logFind(cache = cache, imageUrl = "")
            },
            onDismiss = { cacheToLog = null }
        )
    }
}