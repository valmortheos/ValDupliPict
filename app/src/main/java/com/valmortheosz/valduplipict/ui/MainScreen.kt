package com.valmortheosz.valduplipict.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Filled.Home, Icons.Outlined.Home)
    object Results : BottomNavItem("duplicates", "Results", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary)
    object Settings : BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences = context.getSharedPreferences("onboarding_prefs", android.content.Context.MODE_PRIVATE)
    val hasSeenOnboarding = sharedPreferences.getBoolean("has_seen_onboarding", false)
    val startDest = if (hasSeenOnboarding) "dashboard" else "onboarding"

    val isMainScreen = currentDestination?.route in listOf(
        BottomNavItem.Dashboard.route,
        BottomNavItem.Results.route,
        BottomNavItem.Settings.route
    )
    val isOnboarding = currentDestination?.route == "onboarding"

    Scaffold(
        topBar = {
            if (!isOnboarding) {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                currentDestination?.route == BottomNavItem.Dashboard.route -> "ValDupliPict"
                                currentDestination?.route == BottomNavItem.Results.route -> "Duplicate Results"
                                currentDestination?.route == BottomNavItem.Settings.route -> "Settings"
                                currentDestination?.route == "trash" -> "Recycle Bin"
                                currentDestination?.route?.startsWith("photo_viewer") == true -> "Photo Viewer"
                                else -> "ValDupliPict"
                            }
                        )
                    },
                    navigationIcon = {
                        if (!isMainScreen) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        // Dynamically inject actions if needed, for now we will handle most actions inside the screens
                    }
                )
            }
        },
        bottomBar = {
            if (isMainScreen && !isOnboarding) {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem.Dashboard,
                        BottomNavItem.Results,
                        BottomNavItem.Settings
                    )
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
