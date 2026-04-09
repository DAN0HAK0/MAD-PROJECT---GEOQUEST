package com.dan.mad_project_geoquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.UserPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFirstnameChange(v: String) {
        _uiState.value = _uiState.value.copy(firstname = v, errorMessage = null)
    }

    fun onLastnameChange(v: String) {
        _uiState.value = _uiState.value.copy(lastname = v, errorMessage = null)
    }

    fun onPhoneChange(v: String) {
        _uiState.value = _uiState.value.copy(phone = v, errorMessage = null)
    }

    fun onUsernameChange(v: String) {
        _uiState.value = _uiState.value.copy(username = v, errorMessage = null)
    }

    fun onPasswordChange(v: String) {
        _uiState.value = _uiState.value.copy(password = v, errorMessage = null)
    }

    fun register() {
        val state = _uiState.value

        if (state.firstname.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "First name, username and password are required")
            return
        }
        if (state.username.length < 8) {
            _uiState.value = state.copy(errorMessage = "Username must be at least 8 characters")
            return
        }
        if (state.phone.length < 12) {
            _uiState.value = state.copy(errorMessage = "Phone must be at least 12 characters e.g. 07700000000")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Account created! You can now sign in.",
                        isSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed: HTTP ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}