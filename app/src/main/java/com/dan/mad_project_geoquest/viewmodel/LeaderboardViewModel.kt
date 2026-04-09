package com.dan.mad_project_geoquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.Cache
import com.dan.mad_project_geoquest.api.Event
import com.dan.mad_project_geoquest.api.Find
import com.dan.mad_project_geoquest.api.Player
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.api.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val findCount: Int,
    val totalPoints: Double = 0.0,
    val isCurrentUser: Boolean = false
)

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val entries: List<LeaderboardEntry> = emptyList(),
    val errorMessage: String? = null,
    val isPublicTab: Boolean = true,
    val availableEvents: List<Event> = emptyList(),
    val selectedEventId: Int? = null,
    val sortByPoints: Boolean = false
)

class LeaderboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    fun onTabChanged(isPublic: Boolean) {
        _uiState.update { it.copy(isPublicTab = isPublic, selectedEventId = null) }
        loadLeaderboard()
    }

    fun onEventSelected(eventId: Int?) {
        _uiState.update { it.copy(selectedEventId = eventId) }
        loadLeaderboard()
    }

    fun onSortChanged(byPoints: Boolean) {
        _uiState.update { it.copy(sortByPoints = byPoints) }
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val users: List<User> = RetrofitClient.instance.getUsers()
                val allFinds: List<Find> = RetrofitClient.instance.getFinds()
                val allPlayers: List<Player> = RetrofitClient.instance.getPlayers()
                val allCaches: List<Cache> = RetrofitClient.instance.getCaches()
                val allEvents: List<Event> = RetrofitClient.instance.getEvents()
                val currentUser = SessionManager.currentUser

                val isPublic = _uiState.value.isPublicTab

                val tabEvents = if (isPublic) {
                    allEvents.filter { it.EventIspublic }
                } else {
                    allEvents.filter { event ->
                        !event.EventIspublic && (
                                event.EventOwnerID == currentUser?.UserID ||
                                        allPlayers.any {
                                            it.PlayerUserID == currentUser?.UserID &&
                                                    it.PlayerEventID == event.EventID
                                        }
                                )
                    }
                }

                val selectedEventId = _uiState.value.selectedEventId
                val scopedEvents = if (selectedEventId != null)
                    tabEvents.filter { it.EventID == selectedEventId }
                else tabEvents

                val scopedEventIds = scopedEvents.map { it.EventID }.toSet()

                val scopedCaches = allCaches.filter { it.CacheEventID in scopedEventIds }
                val scopedCacheIds = scopedCaches.map { it.CacheID }.toSet()
                val cachePointsMap = scopedCaches.associate { it.CacheID to it.CachePoints }

                val scopedPlayerIds = allPlayers
                    .filter { it.PlayerEventID in scopedEventIds }
                    .map { it.PlayerID }
                    .toSet()

                val playerIdToUserId = allPlayers
                    .filter { it.PlayerID in scopedPlayerIds }
                    .associate { it.PlayerID to it.PlayerUserID }

                // Group finds per user
                val findsPerUser = allFinds
                    .filter {
                        it.FindPlayerID in scopedPlayerIds &&
                                it.FindCacheID in scopedCacheIds
                    }
                    .groupBy { playerIdToUserId[it.FindPlayerID] }

                val findCountByUser = findsPerUser.mapValues { it.value.size }
                val pointsByUser = findsPerUser.mapValues { (_, finds) ->
                    finds.sumOf { cachePointsMap[it.FindCacheID] ?: 0.0 }
                }

                val sortByPoints = _uiState.value.sortByPoints

                val entries = users
                    .mapNotNull { user ->
                        val count = findCountByUser[user.UserID] ?: return@mapNotNull null
                        val points = pointsByUser[user.UserID] ?: 0.0
                        Triple(user, count, points)
                    }
                    .sortedByDescending { if (sortByPoints) it.third else it.second.toDouble() }
                    .mapIndexed { index, (user, count, points) ->
                        LeaderboardEntry(
                            rank = index + 1,
                            username = user.UserUsername,
                            findCount = count,
                            totalPoints = points,
                            isCurrentUser = user.UserID == currentUser?.UserID
                        )
                    }

                _uiState.update { it.copy(
                    isLoading = false,
                    entries = entries,
                    availableEvents = tabEvents
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load leaderboard: ${e.localizedMessage}"
                ) }
            }
        }
    }

    fun clear() {
        _uiState.value = LeaderboardUiState()
    }
}