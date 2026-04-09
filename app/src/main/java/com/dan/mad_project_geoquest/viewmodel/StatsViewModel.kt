package com.dan.mad_project_geoquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.Find
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val totalFinds: Int = 0,
    val totalPoints: Double = 0.0,
    val recentFinds: List<Find> = emptyList(),
    val errorMessage: String? = null
)

class StatsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun loadStats() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val user = SessionManager.currentUser
                val allPlayers = RetrofitClient.instance.getPlayers()
                val myPlayers = allPlayers.filter { it.PlayerUserID == user?.UserID }

                val myFinds = mutableListOf<Find>()
                myPlayers.forEach { player ->
                    try {
                        myFinds.addAll(RetrofitClient.instance.getFindsByPlayer(player.PlayerID))
                    } catch (_: Exception) {}
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    username = user?.UserUsername ?: "",
                    totalFinds = myFinds.size,
                    totalPoints = myFinds.sumOf { it.FindCache?.CachePoints ?: 0.0 },
                    recentFinds = myFinds.takeLast(5).reversed()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load stats: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clear() {
        _uiState.value = StatsUiState()
    }
}