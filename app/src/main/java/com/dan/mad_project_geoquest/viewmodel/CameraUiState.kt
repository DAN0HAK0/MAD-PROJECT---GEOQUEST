package com.dan.mad_project_geoquest.viewmodel

sealed class CameraUiState {
    data object Idle : CameraUiState()
    data object Capturing : CameraUiState()
    data object Uploading : CameraUiState()
    data class Success(val imageUrl: String) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}