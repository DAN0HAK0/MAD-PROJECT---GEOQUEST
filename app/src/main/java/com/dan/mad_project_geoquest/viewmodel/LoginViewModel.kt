package com.dan.mad_project_geoquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.Player
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.api.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username and password are required")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

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

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Invalid username or password"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Connection error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccess = false)
    }

    fun clear() {
        _uiState.value = LoginUiState()
    }
}