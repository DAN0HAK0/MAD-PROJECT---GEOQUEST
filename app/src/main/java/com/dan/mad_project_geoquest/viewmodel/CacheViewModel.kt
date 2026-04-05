package com.dan.mad_project_geoquest.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.CachePayload
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.EventPayload
import com.dan.mad_project_geoquest.api.Find
import com.dan.mad_project_geoquest.api.FindPayload
import com.dan.mad_project_geoquest.api.Player
import com.dan.mad_project_geoquest.api.PlayerPayload
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.api.User
import com.dan.mad_project_geoquest.api.UserPayload
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

data class RegisterUiState(
    val firstname: String = "",
    val lastname: String = "",
    val phone: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSuccess: Boolean = false
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val allCaches: List<Cache> = emptyList(),
    val myFinds: List<Find> = emptyList(),
    val activeEvents: List<Event> = emptyList(),
    val allPlayers: List<Player> = emptyList(),
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

    // ── Register ──────────────────────────────────────────────────
    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    // ── Home ──────────────────────────────────────────────────────
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    // ── Leaderboard ───────────────────────────────────────────────
    private val _leaderboardUiState = MutableStateFlow(LeaderboardUiState())
    val leaderboardUiState: StateFlow<LeaderboardUiState> = _leaderboardUiState.asStateFlow()

    // ── Stats ─────────────────────────────────────────────────────
    private val _statsUiState = MutableStateFlow(StatsUiState())
    val statsUiState: StateFlow<StatsUiState> = _statsUiState.asStateFlow()

    // All caches from API
    private val _allCaches = MutableStateFlow<List<Cache>>(emptyList())
    val allCaches: StateFlow<List<Cache>> = _allCaches.asStateFlow()

    // Caches the current player has already found
    private val _foundCaches = MutableStateFlow<List<Cache>>(emptyList())
    val foundCaches: StateFlow<List<Cache>> = _foundCaches.asStateFlow()

    // ── Location updates ──────────────────────────────────────────

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation
            _userLocation.value = location

            // Update user location in API
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

    // ── Update user location in API ───────────────────────────────

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

                    try {
                        val players: List<Player> = RetrofitClient.instance.getPlayers()
                        SessionManager.currentPlayer =
                            players.find { it.PlayerUserID == match.UserID }
                    } catch (_: Exception) {}

                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )

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
        _registerUiState.value = RegisterUiState()
        _homeUiState.value = HomeUiState()
        _leaderboardUiState.value = LeaderboardUiState()
        _statsUiState.value = StatsUiState()
        _foundCaches.value = emptyList()
        _allCaches.value = emptyList()
    }

    // ── Register ──────────────────────────────────────────────────

    fun onRegisterFirstnameChange(v: String) {
        _registerUiState.value = _registerUiState.value.copy(firstname = v, errorMessage = null)
    }

    fun onRegisterLastnameChange(v: String) {
        _registerUiState.value = _registerUiState.value.copy(lastname = v, errorMessage = null)
    }

    fun onRegisterPhoneChange(v: String) {
        _registerUiState.value = _registerUiState.value.copy(phone = v, errorMessage = null)
    }

    fun onRegisterUsernameChange(v: String) {
        _registerUiState.value = _registerUiState.value.copy(username = v, errorMessage = null)
    }

    fun onRegisterPasswordChange(v: String) {
        _registerUiState.value = _registerUiState.value.copy(password = v, errorMessage = null)
    }

    fun register() {
        val state = _registerUiState.value

        if (state.firstname.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _registerUiState.value = state.copy(errorMessage = "First name, username and password are required")
            return
        }
        if (state.username.length < 8) {
            _registerUiState.value = state.copy(errorMessage = "Username must be at least 8 characters")
            return
        }
        if (state.phone.length < 12) {
            _registerUiState.value = state.copy(errorMessage = "Phone must be at least 12 characters e.g. 07700000000")
            return
        }

        _registerUiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.createUser(
                    UserPayload(
                        UserFirstname = state.firstname,
                        UserLastname = state.lastname,
                        UserPhone = state.phone,
                        UserUsername = state.username,
                        UserPassword = state.password,
                        UserLatitude = 0.0,
                        UserLongitude = 0.0,
                        UserTimestamp = 0.0,
                        UserImageURL = "https://static.generated.photos/vue-static/face-generator/landing/wall/1.jpg"
                    )
                )
                if (response.isSuccessful) {
                    _registerUiState.value = _registerUiState.value.copy(
                        isLoading = false,
                        successMessage = "Account created! You can now sign in.",
                        isSuccess = true
                    )
                } else {
                    _registerUiState.value = _registerUiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed: HTTP ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("REGISTER", "Error: ${e.localizedMessage}")
                _registerUiState.value = _registerUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetRegisterState() {
        _registerUiState.value = RegisterUiState()
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
                val players = RetrofitClient.instance.getPlayers()
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
                    activeEvents = events,
                    allPlayers = players
                )
            } catch (e: Exception) {
                _homeUiState.value = _homeUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data: ${e.localizedMessage}"
                )
            }
        }
    }

    fun logFind(cache: Cache, onResult: (Boolean) -> Unit) {
        val player = SessionManager.currentPlayer ?: run { onResult(false); return }

        viewModelScope.launch {
            try {
                val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
                    .format(Date())

                val response = RetrofitClient.instance.createFind(
                    FindPayload(
                        FindPlayerID = player.PlayerID,
                        FindCacheID = cache.CacheID,
                        FindDatetime = isoDate
                    )
                )
                if (response.isSuccessful) {
                    loadHomeData()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // ── Join Event ────────────────────────────────────────────────

    fun joinEvent(event: Event, onResult: (Boolean, String) -> Unit) {
        val currentUser = SessionManager.currentUser ?: run {
            onResult(false, "You must be logged in")
            return
        }

        viewModelScope.launch {
            try {
                val players = RetrofitClient.instance.getPlayers()
                val alreadyJoined = players.any {
                    it.PlayerUserID == currentUser.UserID &&
                            it.PlayerEventID == event.EventID
                }

                if (alreadyJoined) {
                    onResult(false, "Already joined this event")
                    return@launch
                }

                val response = RetrofitClient.instance.createPlayer(
                    PlayerPayload(
                        PlayerUserID = currentUser.UserID,
                        PlayerEventID = event.EventID
                    )
                )

                if (response.isSuccessful) {
                    val updatedPlayers = RetrofitClient.instance.getPlayers()
                    SessionManager.currentPlayer = updatedPlayers.find {
                        it.PlayerUserID == currentUser.UserID
                    }
                    loadHomeData()
                    onResult(true, "Joined ${event.EventName}!")
                } else {
                    onResult(false, "Failed to join: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
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