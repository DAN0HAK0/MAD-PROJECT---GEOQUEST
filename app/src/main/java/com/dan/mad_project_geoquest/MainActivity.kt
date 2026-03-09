package com.dan.mad_project_geoquest

import com.dan.mad_project_geoquest.ui.theme.MadProjectGEOQUESTTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.dan.mad_project_geoquest.navigation.NavObjects
import com.dan.mad_project_geoquest.screens.*
import com.dan.mad_project_geoquest.components.BottomNavBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                    NavEntry(route) { HomeScreen() }

                                is NavObjects.Blank ->
                                    NavEntry(route) { BlankScreen() }

                                is NavObjects.Map ->
                                    NavEntry(route) { MapScreen() }

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