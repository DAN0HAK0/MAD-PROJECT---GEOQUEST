package com.dan.mad_project_geoquest

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.components.BottomNavBar
import com.dan.mad_project_geoquest.navigation.NavObjects
import com.dan.mad_project_geoquest.screens.*
import com.dan.mad_project_geoquest.ui.theme.MadProjectGEOQUESTTheme
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel
import com.dan.mad_project_geoquest.screens.AdminScreen

class MainActivity : ComponentActivity() {

    private val cacheViewModel: CacheViewModel by viewModels()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            cacheViewModel.refreshLocationUpdates()
        }
    }

    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }, 1000L)
        }

        setContent {
            MadProjectGEOQUESTTheme {
                val backStack = remember { mutableStateListOf<Any>(NavObjects.Login) }
                val currentDestination = backStack.lastOrNull()
                val isAdmin = SessionManager.isAdmin

                val showBottomBar = currentDestination !is NavObjects.Login &&
                        currentDestination !is NavObjects.Register

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                currentDestination = currentDestination,
                                isAdmin = isAdmin,
                                onNavigate = { destination ->
                                    if (backStack.lastOrNull()?.javaClass != destination.javaClass) {
                                        backStack.add(destination)
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    NavDisplay(
                        modifier = Modifier.padding(paddingValues),
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { route ->
                            when (route) {
                                is NavObjects.Login ->
                                    NavEntry(route) {
                                        LoginScreen(
                                            cacheViewModel = cacheViewModel,
                                            onLoginSuccess = {
                                                backStack.clear()
                                                if (SessionManager.isAdmin) {
                                                    backStack.add(NavObjects.Admin)
                                                } else {
                                                    backStack.add(NavObjects.Home)
                                                }
                                            },
                                            onNavigateToRegister = {
                                                backStack.add(NavObjects.Register)
                                            }
                                        )
                                    }
                                is NavObjects.Register ->
                                    NavEntry(route) {
                                        RegisterScreen(
                                            cacheViewModel = cacheViewModel,
                                            onRegisterSuccess = {
                                                backStack.removeLastOrNull()
                                            },
                                            onBackToLogin = {
                                                backStack.removeLastOrNull()
                                            }
                                        )
                                    }
                                is NavObjects.Home ->
                                    NavEntry(route) { HomeScreen(cacheViewModel) }
                                is NavObjects.Blank ->
                                    NavEntry(route) { LeaderboardScreen(cacheViewModel) }
                                is NavObjects.Map ->
                                    NavEntry(route) { MapScreen(cacheViewModel) }
                                is NavObjects.Stats ->
                                    NavEntry(route) { StatsScreen(cacheViewModel) }
                                is NavObjects.Settings ->
                                    NavEntry(route) {
                                        SettingsScreen(
                                            cacheViewModel = cacheViewModel,
                                            onLogout = {
                                                backStack.clear()
                                                backStack.add(NavObjects.Login)
                                            }
                                        )
                                    }
                                is NavObjects.Admin ->
                                    NavEntry(route) {
                                        AdminScreen(
                                            cacheViewModel = cacheViewModel,
                                            onLogout = {
                                                backStack.clear()
                                                backStack.add(NavObjects.Login)
                                            }
                                        )
                                    }
                                else -> error("Unknown route: $route")
                            }
                        }
                    )
                }
            }
        }
    }
}