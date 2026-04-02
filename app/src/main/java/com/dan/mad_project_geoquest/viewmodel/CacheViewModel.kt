package com.dan.mad_project_geoquest.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.Find
import com.dan.mad_project_geoquest.api.Player
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.api.User
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── UI State data classes ────────────────────────────────────────

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val allCaches: List<Cache> = emptyList(),
    val myFinds: List<Find> = emptyList(),
    val activeEvents: List<Event> = emptyList(),
    val errorMessage: String? = null
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val findCount: Int,
    val isCurrentUser: Boolean = false
)

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val entries: List<LeaderboardEntry> = emptyList(),
    val errorMessage: String? = null
)

data class StatsUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val totalFinds: Int = 0,
    val totalPoints: Double = 0.0,
    val recentFinds: List<Find> = emptyList(),
    val errorMessage: String? = null
)

// ─── ViewModel ────────────────────────────────────────────────────

class CacheViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // ── Location ──────────────────────────────────────────────────
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    // ── Login ─────────────────────────────────────────────────────
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    // ── Home ──────────────────────────────────────────────────────
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    // ── Leaderboard ───────────────────────────────────────────────
    private val _leaderboardUiState = MutableStateFlow(LeaderboardUiState())
    val leaderboardUiState: StateFlow<LeaderboardUiState> = _leaderboardUiState.asStateFlow()

    // ── Stats ─────────────────────────────────────────────────────
    private val _statsUiState = MutableStateFlow(StatsUiState())
    val statsUiState: StateFlow<StatsUiState> = _statsUiState.asStateFlow()

    // All caches from API (used by map and home)
    private val _allCaches = MutableStateFlow<List<Cache>>(emptyList())
    val allCaches: StateFlow<List<Cache>> = _allCaches.asStateFlow()

    // Caches the current player has already found (for HomeScreen list)
    private val _foundCaches = MutableStateFlow<List<Cache>>(emptyList())
    val foundCaches: StateFlow<List<Cache>> = _foundCaches.asStateFlow()

    // ── Location updates ──────────────────────────────────────────

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            _userLocation.value = result.lastLocation
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

    // ── Login ─────────────────────────────────────────────────────

    fun onLoginUsernameChange(value: String) {
        _loginUiState.value = _loginUiState.value.copy(username = value, errorMessage = null)
    }

    fun onLoginPasswordChange(value: String) {
        _loginUiState.value = _loginUiState.value.copy(password = value, errorMessage = null)
    }

    fun login() {
        val state = _loginUiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _loginUiState.value = state.copy(errorMessage = "Username and password are required")
            return
        }

        _loginUiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val users: List<User> = RetrofitClient.instance.getUsers()
                val match = users.find { u ->
                    u.UserUsername.equals(state.username.trim(), ignoreCase = true) &&
                            u.UserPassword == state.password
                }

                if (match != null) {
                    SessionManager.currentUser = match

                    // Resolve player record for this user
                    try {
                        val players: List<Player> = RetrofitClient.instance.getPlayers()
                        SessionManager.currentPlayer = players.find { it.PlayerUserID == match.UserID }
                    } catch (_: Exception) {}

                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )

                    // Pre-load data immediately after login
                    loadAllCaches()
                    loadHomeData()

                } else {
                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        errorMessage = "Invalid username or password"
                    )
                }
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Connection error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetLoginSuccess() {
        _loginUiState.value = _loginUiState.value.copy(isLoginSuccess = false)
    }

    fun logout() {
        SessionManager.clear()
        _loginUiState.value = LoginUiState()
        _homeUiState.value = HomeUiState()
        _leaderboardUiState.value = LeaderboardUiState()
        _statsUiState.value = StatsUiState()
        _foundCaches.value = emptyList()
        _allCaches.value = emptyList()
    }

    // ── Caches ────────────────────────────────────────────────────

    fun loadAllCaches() {
        viewModelScope.launch {
            try {
                val caches = RetrofitClient.instance.getCaches()
                _allCaches.value = caches
            } catch (_: Exception) {}
        }
    }

    fun loadHomeData() {
        _homeUiState.value = _homeUiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val caches = RetrofitClient.instance.getCaches()
                val events = RetrofitClient.instance.getEvents()
                _allCaches.value = caches

                val player = SessionManager.currentPlayer
                val myFinds: List<Find> = if (player != null) {
                    try {
                        RetrofitClient.instance.getFindsByPlayer(player.PlayerID)
                    } catch (_: Exception) { emptyList() }
                } else emptyList()

                val foundCacheIds = myFinds.map { it.FindCacheID }.toSet()
                _foundCaches.value = caches.filter { it.CacheID in foundCacheIds }

                _homeUiState.value = _homeUiState.value.copy(
                    isLoading = false,
                    allCaches = caches,
                    myFinds = myFinds,
                    activeEvents = events
                )
            } catch (e: Exception) {
                _homeUiState.value = _homeUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data: ${e.localizedMessage}"
                )
            }
        }
    }

    // Called when player taps "Log This Find" on a cache in MapScreen
    fun logFind(cache: Cache, onResult: (Boolean) -> Unit) {
        val player = SessionManager.currentPlayer ?: run { onResult(false); return }

        viewModelScope.launch {
            try {
                // ISO 8601 datetime without requiring java.time (API 26+ safe)
                val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
                    .format(Date())

                val find = Find(
                    FindPlayerID = player.PlayerID,
                    FindCacheID = cache.CacheID,
                    FindDatetime = isoDate
                )
                RetrofitClient.instance.createFind(find)
                loadHomeData() // refresh found caches
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // ── Leaderboard ───────────────────────────────────────────────

    fun loadLeaderboard() {
        _leaderboardUiState.value = _leaderboardUiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val users: List<User> = RetrofitClient.instance.getUsers()
                val finds: List<Find> = RetrofitClient.instance.getFinds()
                val players: List<Player> = RetrofitClient.instance.getPlayers()
                val currentUserId = SessionManager.currentUser?.UserID

                val findCountByPlayer = finds.groupBy { it.FindPlayerID }
                    .mapValues { it.value.size }

                val entries = users
                    .map { user ->
                        val player = players.find { it.PlayerUserID == user.UserID }
                        val count = if (player != null) findCountByPlayer[player.PlayerID] ?: 0 else 0
                        Triple(user, player, count)
                    }
                    .sortedByDescending { it.third }
                    .mapIndexed { index, (user, _, count) ->
                        LeaderboardEntry(
                            rank = index + 1,
                            username = user.UserUsername,
                            findCount = count,
                            isCurrentUser = user.UserID == currentUserId
                        )
                    }

                _leaderboardUiState.value = _leaderboardUiState.value.copy(
                    isLoading = false,
                    entries = entries
                )
            } catch (e: Exception) {
                _leaderboardUiState.value = _leaderboardUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load leaderboard: ${e.localizedMessage}"
                )
            }
        }
    }

    // ── Stats ─────────────────────────────────────────────────────

    fun loadStats() {
        _statsUiState.value = _statsUiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val user = SessionManager.currentUser
                val player = SessionManager.currentPlayer

                val myFinds: List<Find> = if (player != null) {
                    try {
                        RetrofitClient.instance.getFindsByPlayer(player.PlayerID)
                    } catch (_: Exception) { emptyList() }
                } else emptyList()

                // Sum points from the Cache objects embedded in each Find
                val totalPoints = myFinds.sumOf { it.FindCache?.CachePoints ?: 0.0 }

                _statsUiState.value = _statsUiState.value.copy(
                    isLoading = false,
                    username = user?.UserUsername ?: "",
                    totalFinds = myFinds.size,
                    totalPoints = totalPoints,
                    recentFinds = myFinds.takeLast(5).reversed()
                )
            } catch (e: Exception) {
                _statsUiState.value = _statsUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load stats: ${e.localizedMessage}"
                )
            }
        }
    }

}