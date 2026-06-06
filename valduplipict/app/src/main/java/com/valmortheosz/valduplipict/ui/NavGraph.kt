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

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "onboarding") {
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
            DashboardScreen(navController = navController)
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
    }
}
