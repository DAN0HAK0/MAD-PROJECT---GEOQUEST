package com.dan.mad_project_geoquest.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.database.Cache
import com.dan.mad_project_geoquest.database.CacheRepository
import com.dan.mad_project_geoquest.utils.GeoQuestNotificationHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CacheViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CacheRepository(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    companion object {
        const val CLUE_RADIUS_METERS = 200f
        const val FOUND_RADIUS_METERS = 50f
    }

    // Only found caches shown on map and home screen
    val foundCaches: StateFlow<List<Cache>> = repository.foundCaches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All caches used internally for proximity checks
    val allCaches: StateFlow<List<Cache>> = repository.allCaches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userLocation = MutableStateFlow<Location?>(null)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                userLocation.value = location
                checkProximity(location)
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            // Small delay before starting location to let the UI settle
            kotlinx.coroutines.delay(2000L)
            GeoQuestNotificationHelper.createChannels(getApplication())
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        val context = getApplication<Application>()
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000L
        ).setMinUpdateIntervalMillis(8000L).build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun checkProximity(userLocation: Location) {
        val context = getApplication<Application>()
        val currentCaches = allCaches.value

        currentCaches.forEach { cache ->
            val cacheLocation = Location("cache").apply {
                latitude = cache.latitude
                longitude = cache.longitude
            }

            val distance = userLocation.distanceTo(cacheLocation)

            when {
                // Within 50m — unlock the cache
                distance <= FOUND_RADIUS_METERS && !cache.isFound -> {
                    viewModelScope.launch {
                        repository.markAsFound(cache.id)
                        GeoQuestNotificationHelper.sendFoundNotification(
                            context, cache.title, cache.points, cache.id
                        )
                    }
                }
                // Within 200m but not yet found — send clue notification once
                distance <= CLUE_RADIUS_METERS && !cache.isFound && !cache.notificationSent -> {
                    viewModelScope.launch {
                        repository.markNotificationSent(cache.id)
                        GeoQuestNotificationHelper.sendClueNotification(
                            context, cache.title, cache.clue, cache.id
                        )
                    }
                }
            }
        }
    }

    fun refreshLocationUpdates() {
        startLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}