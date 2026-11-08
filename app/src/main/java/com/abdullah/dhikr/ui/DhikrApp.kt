package com.abdullah.dhikr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abdullah.dhikr.features.counter.presentation.CounterScreen
import com.abdullah.dhikr.features.history.presentation.HistoryScreen
import com.abdullah.dhikr.features.qibla.presentation.QiblaScreen
import com.abdullah.dhikr.features.settings.presentation.SettingsScreen
import com.abdullah.dhikr.features.statistics.presentation.StatisticsScreen
import com.abdullah.dhikr.ui.navigation.DhikrDestinations

@Composable
fun DhikrApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Counter") },
                    selected = currentDestination == DhikrDestinations.COUNTER_ROUTE,
                    onClick = {
                        if (currentDestination != DhikrDestinations.COUNTER_ROUTE) {
                            navController.navigate(DhikrDestinations.COUNTER_ROUTE) {
                                popUpTo(DhikrDestinations.COUNTER_ROUTE) { inclusive = true }
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    label = { Text("Qibla") },
                    selected = currentDestination == DhikrDestinations.QIBLA_ROUTE,
                    onClick = {
                        if (currentDestination != DhikrDestinations.QIBLA_ROUTE) {
                            navController.navigate(DhikrDestinations.QIBLA_ROUTE)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History") },
                    selected = currentDestination == DhikrDestinations.HISTORY_ROUTE,
                    onClick = {
                        if (currentDestination != DhikrDestinations.HISTORY_ROUTE) {
                            navController.navigate(DhikrDestinations.HISTORY_ROUTE)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Stats") },
                    selected = currentDestination == DhikrDestinations.STATISTICS_ROUTE,
                    onClick = {
                        if (currentDestination != DhikrDestinations.STATISTICS_ROUTE) {
                            navController.navigate(DhikrDestinations.STATISTICS_ROUTE)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = currentDestination == DhikrDestinations.SETTINGS_ROUTE,
                    onClick = {
                        if (currentDestination != DhikrDestinations.SETTINGS_ROUTE) {
                            navController.navigate(DhikrDestinations.SETTINGS_ROUTE)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DhikrDestinations.COUNTER_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(DhikrDestinations.COUNTER_ROUTE) {
                CounterScreen()
            }
            composable(DhikrDestinations.QIBLA_ROUTE) {
                QiblaScreen()
            }
            composable(DhikrDestinations.HISTORY_ROUTE) {
                HistoryScreen(
                    onStartDhikrClick = {
                        navController.navigate(DhikrDestinations.COUNTER_ROUTE) {
                            popUpTo(DhikrDestinations.COUNTER_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            composable(DhikrDestinations.STATISTICS_ROUTE) {
                StatisticsScreen(
                    onStartDhikrClick = {
                        navController.navigate(DhikrDestinations.COUNTER_ROUTE) {
                            popUpTo(DhikrDestinations.COUNTER_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            composable(DhikrDestinations.SETTINGS_ROUTE) {
                SettingsScreen()
            }
        }
    }
}
