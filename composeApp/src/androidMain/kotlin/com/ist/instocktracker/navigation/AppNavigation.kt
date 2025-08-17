package com.ist.instocktracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ist.instocktracker.feature.auth.AuthScreen
import com.ist.instocktracker.feature.main.MainScaffold
import com.ist.instocktracker.feature.main.MainScreen
import com.ist.instocktracker.services.ServiceLocator
import com.ist.instocktracker.utils.LocalNavController

/**
 * Navigation routes for the app
 */
object AppRoutes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val MAIN_LIST = "main/list"
}

/**
 * Main navigation component for the app
 * @param navController The NavHostController to use for navigation
 * @param tokenDataStore The TokenDataStore to use for authentication
 * @param startDestination The starting destination route
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoutes.AUTH
) {
    val tokenStore = ServiceLocator.tokenStore
    val isAuthenticated by tokenStore.isAuthenticated().collectAsState(initial = false)

    // If user is authenticated, navigate to main screen
    if (isAuthenticated && navController.currentDestination?.route == AppRoutes.AUTH) {
        navController.navigate(AppRoutes.MAIN) {
            popUpTo(AppRoutes.AUTH) { inclusive = true }
        }
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(AppRoutes.AUTH) {
                AuthScreen()
            }

            navigation(startDestination = AppRoutes.MAIN_LIST, route = AppRoutes.MAIN) {
                composable(AppRoutes.MAIN_LIST) {
                    MainScaffold { paddingValue -> MainScreen(paddingValue) }
                }

            }

        }
    }

}