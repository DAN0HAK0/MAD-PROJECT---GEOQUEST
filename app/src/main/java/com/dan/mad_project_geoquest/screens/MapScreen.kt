package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen(cacheViewModel: CacheViewModel) {
    val allCaches by cacheViewModel.allCaches.collectAsState()
    val userLocation by cacheViewModel.userLocation.collectAsState()
    val mapView = rememberMapViewWithLifecycle()

    var hasMovedCamera by remember { mutableStateOf(false) }
    var selectedCache by remember { mutableStateOf<Cache?>(null) }
    var logFindResult by remember { mutableStateOf<String?>(null) }

    val cacheSnapshot = remember(allCaches) { allCaches.toList() }

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
                            val marker: Marker? = googleMap.addMarker(
                                MarkerOptions()
                                    .position(position)
                                    .title(cache.CacheName)
                                    .snippet("${cache.CachePoints.toInt()} pts · ${cache.CacheClue}")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
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

    selectedCache?.let { cache ->
        CacheBottomSheet(
            cache = cache,
            onDismiss = { selectedCache = null },
            onLogFind = {
                cacheViewModel.logFind(cache) { success ->
                    logFindResult = if (success) "✓ Find logged! Points awarded." else "Could not log find — try again."
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
                .padding(24.dp)
        ) {
            Text(
                text = cache.CacheName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "📍 ${cache.CacheLatitude}, ${cache.CacheLongitude}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            cache.CacheEvent?.let {
                Text(
                    text = "🗓 ${it.EventName}",
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
                Text(text = "🔍 Clue: ${cache.CacheClue}", fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = "⭐ Points: ${cache.CachePoints.toInt()}",
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

            Spacer(Modifier.height(32.dp))
        }
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