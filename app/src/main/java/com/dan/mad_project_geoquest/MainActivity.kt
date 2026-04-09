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
import com.dan.mad_project_geoquest.utils.GeoQuestNotificationHelper
import com.dan.mad_project_geoquest.viewmodel.CameraViewModel
import com.dan.mad_project_geoquest.viewmodel.HomeViewModel
import com.dan.mad_project_geoquest.viewmodel.LeaderboardViewModel
import com.dan.mad_project_geoquest.viewmodel.LoginViewModel
import com.dan.mad_project_geoquest.viewmodel.MapViewModel
import com.dan.mad_project_geoquest.viewmodel.RegisterViewModel
import com.dan.mad_project_geoquest.viewmodel.StatsViewModel

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val registerViewModel: RegisterViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val leaderboardViewModel: LeaderboardViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()
    private val statsViewModel: StatsViewModel by viewModels()
    private val cameraViewModel: CameraViewModel by viewModels()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            mapViewModel.refreshLocationUpdates()
        }
    }

    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    // Camera dialog dismissal triggers notification request so dialogs don't compete
    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channels before any notifications fire
        GeoQuestNotificationHelper.createChannels(this)

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        // Camera launches notification request in its callback above
        cameraPermissionRequest.launch(Manifest.permission.CAMERA)

        setContent {
            var darkTheme by remember { mutableStateOf(false) }

            MadProjectGEOQUESTTheme(darkTheme = darkTheme) {
                val backStack = remember { mutableStateListOf<Any>(NavObjects.Login) }
                val currentDestination = backStack.lastOrNull()
                val isAdmin = SessionManager.isAdmin

                val showBottomBar = currentDestination !is NavObjects.Login &&
                        currentDestination !is NavObjects.Register &&
                        currentDestination !is NavObjects.EventDetail &&
                        currentDestination !is NavObjects.Camera

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
                                            loginViewModel = loginViewModel,
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
                                            registerViewModel = registerViewModel,
                                            onRegisterSuccess = { backStack.removeLastOrNull() },
                                            onBackToLogin = { backStack.removeLastOrNull() }
                                        )
                                    }

                                is NavObjects.Home ->
                                    NavEntry(route) {
                                        HomeScreen(
                                            homeViewModel = homeViewModel,
                                            onNavigateToMyEvents = {
                                                backStack.add(NavObjects.MyEvents)
                                            },
                                            onNavigateToEventDetail = { eventId ->
                                                backStack.add(NavObjects.EventDetail(eventId))
                                            }
                                        )
                                    }

                                is NavObjects.EventDetail ->
                                    NavEntry(route) {
                                        EventDetailScreen(
                                            eventId = route.eventId,
                                            homeViewModel = homeViewModel,
                                            onBack = { backStack.removeLastOrNull() }
                                        )
                                    }

                                is NavObjects.MyEvents ->
                                    NavEntry(route) {
                                        MyEventsScreen(
                                            onBack = { backStack.removeLastOrNull() }
                                        )
                                    }

                                is NavObjects.Blank ->
                                    NavEntry(route) {
                                        LeaderboardScreen(leaderboardViewModel)
                                    }

                                is NavObjects.Map ->
                                    NavEntry(route) {
                                        MapScreen(
                                            mapViewModel = mapViewModel,
                                            onOpenCamera = { cache ->
                                                backStack.add(
                                                    NavObjects.Camera(
                                                        cacheId = cache.CacheID,
                                                        cacheName = cache.CacheName
                                                    )
                                                )
                                            }
                                        )
                                    }

                                is NavObjects.Camera ->
                                    NavEntry(route) {
                                        val cache = remember(route.cacheId) {
                                            mapViewModel.allCaches.value
                                                .find { it.CacheID == route.cacheId }
                                        }

                                        if (cache != null) {
                                            CameraScreen(
                                                cacheName = route.cacheName,
                                                cameraViewModel = cameraViewModel,
                                                onPhotoConfirmed = { imageUrl ->
                                                    mapViewModel.logFind(
                                                        cache = cache,
                                                        imageUrl = imageUrl
                                                    ) { _ ->
                                                        backStack.removeLastOrNull()
                                                    }
                                                },
                                                onBack = {
                                                    cameraViewModel.reset()
                                                    backStack.removeLastOrNull()
                                                }
                                            )
                                        } else {
                                            LaunchedEffect(Unit) { backStack.removeLastOrNull() }
                                        }
                                    }

                                is NavObjects.Stats ->
                                    NavEntry(route) {
                                        StatsScreen(statsViewModel)
                                    }

                                is NavObjects.Settings ->
                                    NavEntry(route) {
                                        SettingsScreen(
                                            loginViewModel = loginViewModel,
                                            homeViewModel = homeViewModel,
                                            leaderboardViewModel = leaderboardViewModel,
                                            statsViewModel = statsViewModel,
                                            darkTheme = darkTheme,
                                            onThemeToggle = { darkTheme = !darkTheme },
                                            onLogout = {
                                                backStack.clear()
                                                backStack.add(NavObjects.Login)
                                            }
                                        )
                                    }

                                is NavObjects.Admin ->
                                    NavEntry(route) {
                                        AdminScreen(
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