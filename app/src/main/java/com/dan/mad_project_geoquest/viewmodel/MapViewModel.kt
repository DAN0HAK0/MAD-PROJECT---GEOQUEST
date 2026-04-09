package com.dan.mad_project_geoquest.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.Find
import com.dan.mad_project_geoquest.api.FindPayload
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.utils.GeoQuestNotificationHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.location.Location
import com.dan.mad_project_geoquest.api.UserPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private val _allCaches = MutableStateFlow<List<Cache>>(emptyList())
    val allCaches: StateFlow<List<Cache>> = _allCaches.asStateFlow()

    private val _foundCaches = MutableStateFlow<List<Cache>>(emptyList())
    val foundCaches: StateFlow<List<Cache>> = _foundCaches.asStateFlow()


    private val _logFindMessage = MutableStateFlow<String?>(null)
    val logFindMessage: StateFlow<String?> = _logFindMessage.asStateFlow()

    private val _sessionClueUnlockedIds = MutableStateFlow<Set<Int>>(emptySet())
    val sessionClueUnlockedIds: StateFlow<Set<Int>> = _sessionClueUnlockedIds.asStateFlow()

    fun clearLogFindMessage() {
        _logFindMessage.value = null
    }

    /** Called when a cache enters the 200m clue radius for the first time this session */
    fun markClueUnlocked(cacheId: Int) {
        if (cacheId !in _sessionClueUnlockedIds.value) {
            _sessionClueUnlockedIds.value = _sessionClueUnlockedIds.value + cacheId
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation
            _userLocation.value = location
            location?.let { updateUserLocationInApi(it) }
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, null)
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateUserLocationInApi(location: Location) {
        val user = SessionManager.currentUser ?: return
        viewModelScope.launch {
            try {
                RetrofitClient.instance.updateUser(
                    id = user.UserID,
                    user = UserPayload(
                        UserFirstname = user.UserFirstname,
                        UserLastname = user.UserLastname,
                        UserPhone = user.UserPhone,
                        UserUsername = user.UserUsername,
                        UserPassword = user.UserPassword,
                        UserLatitude = location.latitude,
                        UserLongitude = location.longitude,
                        UserTimestamp = location.time.toDouble(),
                        UserImageURL = user.UserImageURL
                    )
                )
            } catch (_: Exception) {}
        }
    }

    fun loadAllCaches() {
        viewModelScope.launch {
            try {
                val currentUser = SessionManager.currentUser

                val allEvents = RetrofitClient.instance.getEvents()


                val ownedPrivateEventIds = allEvents
                    .filter { !it.EventIspublic && it.EventOwnerID == currentUser?.UserID }
                    .map { it.EventID }
                    .toSet()

                val allCaches = RetrofitClient.instance.getCaches()


                val visibleCaches = allCaches.filter { it.CacheEventID !in ownedPrivateEventIds }
                _allCaches.value = visibleCaches

                val allPlayers = RetrofitClient.instance.getPlayers()
                val myPlayers = allPlayers.filter { it.PlayerUserID == currentUser?.UserID }

                val myFinds = mutableListOf<Find>()
                myPlayers.forEach { player ->
                    try {
                        myFinds.addAll(RetrofitClient.instance.getFindsByPlayer(player.PlayerID))
                    } catch (_: Exception) {}
                }

                val foundCacheIds = myFinds.map { it.FindCacheID }.toSet()
                _foundCaches.value = visibleCaches.filter { it.CacheID in foundCacheIds }
            } catch (_: Exception) {}
        }
    }

    fun logFind(
        cache: Cache,
        imageUrl: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        val currentUser = SessionManager.currentUser ?: run {
            _logFindMessage.value = "Not logged in — please sign in again"
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val players = RetrofitClient.instance.getPlayers()
                val player = players.find {
                    it.PlayerUserID == currentUser.UserID &&
                            it.PlayerEventID == cache.CacheEventID
                } ?: run {
                    _logFindMessage.value = "You haven't joined this event"
                    onResult(false)
                    return@launch
                }

                val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
                    .format(Date())

                val finalImageUrl = imageUrl.ifBlank { cache.CacheImageURL }

                val response = RetrofitClient.instance.createFind(
                    FindPayload(
                        FindPlayerID = player.PlayerID,
                        FindCacheID = cache.CacheID,
                        FindDatetime = isoDate,
                        FindImageURL = finalImageUrl
                    )
                )

                if (response.isSuccessful) {
                    GeoQuestNotificationHelper.sendFoundNotification(
                        context = getApplication(),
                        cacheTitle = cache.CacheName,
                        points = cache.CachePoints.toInt(),
                        cacheId = cache.CacheID
                    )
                    _foundCaches.value = _foundCaches.value + cache
                    _logFindMessage.value = "${cache.CacheName} found! +${cache.CachePoints.toInt()} pts"
                    loadAllCaches()
                    onResult(true)
                } else {
                    _logFindMessage.value = "Failed to log find — please try again"
                    onResult(false)
                }
            } catch (e: Exception) {
                _logFindMessage.value = "Failed to log find — please try again"
                onResult(false)
            }
        }
    }
}