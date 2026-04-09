package com.dan.mad_project_geoquest.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.components.settings.AccountManagementSection
import com.dan.mad_project_geoquest.components.settings.DeleteAccountDialog
import com.dan.mad_project_geoquest.components.settings.PreferencesSection
import com.dan.mad_project_geoquest.components.settings.SettingsHome
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.viewmodel.HomeViewModel
import com.dan.mad_project_geoquest.viewmodel.LeaderboardViewModel
import com.dan.mad_project_geoquest.viewmodel.LoginViewModel
import com.dan.mad_project_geoquest.viewmodel.StatsViewModel
import kotlinx.coroutines.launch

private enum class SettingsSection { HOME, ACCOUNT, PREFERENCES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    loginViewModel: LoginViewModel,
    homeViewModel: HomeViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    statsViewModel: StatsViewModel,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit
) {
    val user = SessionManager.currentUser
    val scope = rememberCoroutineScope()
    var section           by remember { mutableStateOf(SettingsSection.HOME) }
    var showLogoutDialog  by remember { mutableStateOf(false) }
    var showDeleteDialog  by remember { mutableStateOf(false) }

    val topBarTitle = when (section) {
        SettingsSection.HOME        -> "Settings"
        SettingsSection.ACCOUNT     -> "Account Management"
        SettingsSection.PREFERENCES -> "Preferences"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(topBarTitle, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    if (section != SettingsSection.HOME) {
                        IconButton(onClick = { section = SettingsSection.HOME }) {
                            Icon(Icons.Filled.Close, contentDescription = "Back", tint = Cream)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBrown,
                    titleContentColor = Cream,
                    actionIconContentColor = Cream
                )
            )
        }
    ) { innerPadding ->
        when (section) {
            SettingsSection.HOME -> SettingsHome(
                modifier = Modifier.padding(innerPadding),
                onAccountClick = { section = SettingsSection.ACCOUNT },
                onPreferencesClick = { section = SettingsSection.PREFERENCES },
                onLogoutClick = { showLogoutDialog = true },
                onDeleteClick = { showDeleteDialog = true }
            )
            SettingsSection.ACCOUNT -> AccountManagementSection(
                modifier = Modifier.padding(innerPadding)
            )
            SettingsSection.PREFERENCES -> PreferencesSection(
                modifier = Modifier.padding(innerPadding),
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        SessionManager.clear()
                        loginViewModel.clear()
                        homeViewModel.clear()
                        leaderboardViewModel.clear()
                        statsViewModel.clear()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirmed = {
                scope.launch {
                    try {
                        val userId = user?.UserID ?: return@launch
                        val response = RetrofitClient.instance.deleteUser(userId)
                        if (response.isSuccessful) {
                            showDeleteDialog = false
                            SessionManager.clear()
                            loginViewModel.clear()
                            homeViewModel.clear()
                            leaderboardViewModel.clear()
                            statsViewModel.clear()
                            onLogout()
                        }
                    } catch (_: Exception) {}
                }
            },
            correctPassword = user?.UserPassword ?: ""
        )
    }
}