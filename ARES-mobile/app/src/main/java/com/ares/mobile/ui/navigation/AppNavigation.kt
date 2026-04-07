package com.ares.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ares.mobile.AresApplication
import com.ares.mobile.ui.screens.ChatScreen
import com.ares.mobile.ui.screens.FirstRunScreen
import com.ares.mobile.ui.screens.MemoryScreen
import com.ares.mobile.ui.screens.SettingsScreen
import com.ares.mobile.ui.screens.TasksScreen
import com.ares.mobile.viewmodel.AresViewModelFactory
import com.ares.mobile.viewmodel.ChatViewModel
import com.ares.mobile.viewmodel.MemoryViewModel
import com.ares.mobile.viewmodel.SettingsViewModel
import com.ares.mobile.viewmodel.TasksViewModel

private data class DestinationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val mainDestinations = listOf(
    DestinationItem("chat", "Chat", Icons.Default.Chat),
    DestinationItem("memory", "Memoria", Icons.Default.Memory),
    DestinationItem("tasks", "Tareas", Icons.Default.Schedule),
    DestinationItem("settings", "Config", Icons.Default.Settings),
)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
) {
    val application = LocalContext.current.applicationContext as AresApplication
    val viewModelFactory = remember(application) { AresViewModelFactory(application.appContainer) }

    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val chatViewModel: ChatViewModel = viewModel(factory = viewModelFactory)
    val memoryViewModel: MemoryViewModel = viewModel(factory = viewModelFactory)
    val tasksViewModel: TasksViewModel = viewModel(factory = viewModelFactory)

    val navController = rememberNavController()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val showBottomBar = currentDestination?.route in mainDestinations.map { it.route }

    LaunchedEffect(settingsState.firstRunCompleted) {
        val targetRoute = if (settingsState.firstRunCompleted) "chat" else "first-run"
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    mainDestinations.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = if (settingsState.firstRunCompleted) "chat" else "first-run",
        ) {
            composable("first-run") {
                FirstRunScreen(
                    onContinue = {
                        navController.navigate("chat") {
                            popUpTo("first-run") { inclusive = true }
                        }
                    },
                    viewModel = settingsViewModel,
                )
            }
            composable("chat") { ChatScreen(viewModel = chatViewModel) }
            composable("memory") { MemoryScreen(viewModel = memoryViewModel) }
            composable("tasks") { TasksScreen(viewModel = tasksViewModel) }
            composable("settings") { SettingsScreen(viewModel = settingsViewModel) }
        }
    }
}