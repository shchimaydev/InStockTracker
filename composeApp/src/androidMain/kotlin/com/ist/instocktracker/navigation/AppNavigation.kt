package com.ist.instocktracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ist.instocktracker.data.TokenDataStore
import com.ist.instocktracker.screens.AuthScreen
import com.ist.instocktracker.screens.MainScreen

/**
 * Navigation routes for the app
 */
object AppRoutes {
    const val AUTH = "auth"
    const val MAIN = "main"
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
    tokenDataStore: TokenDataStore,
    startDestination: String = AppRoutes.AUTH
) {
    // Check if user is authenticated
    val isAuthenticated by tokenDataStore.isAuthenticated().collectAsState(initial = false)
    
    // If user is authenticated, navigate to main screen
    if (isAuthenticated && navController.currentDestination?.route == AppRoutes.AUTH) {
        navController.navigate(AppRoutes.MAIN) {
            popUpTo(AppRoutes.AUTH) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.AUTH) {
            AuthScreen(
                onSignInSuccess = {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                },
                tokenDataStore = tokenDataStore
            )
        }
        
        composable(AppRoutes.MAIN) {
            MainScreen(tokenDataStore = tokenDataStore)
        }
    }
}