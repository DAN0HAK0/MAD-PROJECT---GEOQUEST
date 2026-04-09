package com.dan.mad_project_geoquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.Find
import com.dan.mad_project_geoquest.api.Player
import com.dan.mad_project_geoquest.api.PlayerPayload
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val allCaches: List<Cache> = emptyList(),
    val myFinds: List<Find> = emptyList(),
    val activeEvents: List<Event> = emptyList(),
    val allPlayers: List<Player> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val caches = RetrofitClient.instance.getCaches()
                val events = RetrofitClient.instance.getEvents()
                val players = RetrofitClient.instance.getPlayers()

                val currentUser = SessionManager.currentUser
                val myPlayers = players.filter { it.PlayerUserID == currentUser?.UserID }

                val myFinds = mutableListOf<Find>()
                myPlayers.forEach { player ->
                    try {
                        myFinds.addAll(RetrofitClient.instance.getFindsByPlayer(player.PlayerID))
                    } catch (_: Exception) {}
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allCaches = caches,
                        myFinds = myFinds,
                        activeEvents = events,
                        allPlayers = players
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load data: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

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
                        it.PlayerUserID == currentUser.UserID &&
                                it.PlayerEventID == event.EventID
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

    fun leaveEvent(event: Event, onResult: (Boolean, String) -> Unit) {
        val currentUser = SessionManager.currentUser ?: run {
            onResult(false, "You must be logged in")
            return
        }

        viewModelScope.launch {
            try {
                // Find the player record for this user + event
                val players = RetrofitClient.instance.getPlayers()
                val playerRecord = players.find {
                    it.PlayerUserID == currentUser.UserID &&
                            it.PlayerEventID == event.EventID
                }

                if (playerRecord == null) {
                    onResult(false, "You are not joined to this event")
                    return@launch
                }

                val response = RetrofitClient.instance.deletePlayer(playerRecord.PlayerID)

                if (response.isSuccessful) {

                    if (SessionManager.currentPlayer?.PlayerID == playerRecord.PlayerID) {
                        SessionManager.currentPlayer = null
                    }
                    loadHomeData()
                    onResult(true, "Left ${event.EventName}")
                } else {
                    onResult(false, "Failed to leave: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun clear() {
        _uiState.value = HomeUiState()
    }
}