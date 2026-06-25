package com.valmortheosz.valduplipict.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.valmortheosz.valduplipict.ui.dashboard.DashboardScreen
import com.valmortheosz.valduplipict.ui.duplicates.DuplicateListScreen
import com.valmortheosz.valduplipict.ui.onboarding.OnboardingScreen
import com.valmortheosz.valduplipict.ui.settings.SettingsScreen
import com.valmortheosz.valduplipict.ui.trash.TrashScreen
import com.valmortheosz.valduplipict.ui.duplicates.PhotoViewerScreen

@Composable
fun NavGraph() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences = context.getSharedPreferences("onboarding_prefs", android.content.Context.MODE_PRIVATE)
    val hasSeenOnboarding = sharedPreferences.getBoolean("has_seen_onboarding", false)
    val startDest = if (hasSeenOnboarding) "dashboard" else "onboarding"

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDest) {
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen()
        }
        composable("duplicates") {
            DuplicateListScreen(navController = navController)
        }
        composable("trash") {
            TrashScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("photo_viewer/{encodedPath}") { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("encodedPath") ?: ""
            PhotoViewerScreen(navController = navController, encodedPath = encodedPath)
        }
    }
}
