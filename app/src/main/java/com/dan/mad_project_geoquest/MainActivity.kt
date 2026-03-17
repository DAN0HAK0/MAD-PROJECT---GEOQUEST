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
import com.dan.mad_project_geoquest.components.BottomNavBar
import com.dan.mad_project_geoquest.navigation.NavObjects
import com.dan.mad_project_geoquest.screens.*
import com.dan.mad_project_geoquest.ui.theme.MadProjectGEOQUESTTheme
import com.dan.mad_project_geoquest.viewmodel.CacheViewModel

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
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request permissions on launch
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

// Request notification permission separately after a short delay
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }, 1000L)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MadProjectGEOQUESTTheme {
                val backStack = remember { mutableStateListOf<Any>(NavObjects.Home) }
                val currentDestination = backStack.lastOrNull()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(
                            currentDestination = currentDestination,
                            onNavigate = { destination ->
                                backStack.add(destination)
                            }
                        )
                    }
                ) { paddingValues ->
                    NavDisplay(
                        modifier = Modifier.padding(paddingValues),
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { route ->
                            when (route) {
                                is NavObjects.Home ->
                                    NavEntry(route) { HomeScreen(cacheViewModel) }
                                is NavObjects.Blank ->
                                    NavEntry(route) { BlankScreen() }
                                is NavObjects.Map ->
                                    NavEntry(route) { MapScreen(cacheViewModel) }
                                is NavObjects.Stats ->
                                    NavEntry(route) { StatsScreen() }
                                is NavObjects.Settings ->
                                    NavEntry(route) { SettingsScreen() }
                                else -> error("Unknown route: $route")
                            }
                        }
                    )
                }
            }
        }
    }
}