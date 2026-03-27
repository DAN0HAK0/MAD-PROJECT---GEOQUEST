package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel

@Composable
fun MapScreen(cacheViewModel: CacheViewModel) {
    val foundCaches by cacheViewModel.foundCaches.collectAsState()
    val userLocation by cacheViewModel.userLocation.collectAsState()
    val mapView = rememberMapViewWithLifecycle()
    var hasMovedCamera by remember { mutableStateOf(false) }

    AndroidView(
        factory = { context ->
            mapView.apply {
                getMapAsync { googleMap ->
                    // Enable the native blue dot — permission must already be granted
                    try {
                        googleMap.isMyLocationEnabled = true
                        googleMap.uiSettings.isMyLocationButtonEnabled = true
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }

                    val kingston = LatLng(51.4123, -0.3007)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kingston, 15f))
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mv ->
            mv.getMapAsync { googleMap ->
                googleMap.clear()

                userLocation?.let { loc ->
                    val userLatLng = LatLng(loc.latitude, loc.longitude)

                    // Only animate to the user's location once, not on every recomposition
                    if (!hasMovedCamera) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                        hasMovedCamera = true
                    }
                }

                foundCaches.forEach { cache ->
                    val position = LatLng(cache.latitude, cache.longitude)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(cache.title)
                            .snippet("${cache.points} pts")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                }
            }
        }
    )
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(android.os.Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return mapView
}