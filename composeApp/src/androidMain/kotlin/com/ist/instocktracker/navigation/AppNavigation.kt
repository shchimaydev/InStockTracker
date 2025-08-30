package com.ist.instocktracker.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ist.instocktracker.feature.auth.AuthScreen
import com.ist.instocktracker.feature.linkitem.AddEditLinkItemScreen
import com.ist.instocktracker.feature.main.MainScaffold
import com.ist.instocktracker.feature.main.MainScreen
import com.ist.instocktracker.services.ServiceLocator.tokenStore
import com.ist.instocktracker.utils.LocalNavController
import kotlinx.coroutines.flow.first

/**
 * Navigation routes for the app
 */
object AppRoutes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val MAIN_LIST = "main/list"
    const val LINK_ITEM = "main/link_item"
    const val ADD_EDIT_LINK_ITEM = "main/add_edit_link_item"
}

/**
 * Main navigation component for the app
 * @param navController The NavHostController to use for navigation
 * @param startDestination The starting destination route
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoutes.AUTH
) {
//    val tokenStore = ServiceLocator.tokenStore
    val isAuthenticated by tokenStore.isAuthenticated().collectAsState(initial = false)


    LaunchedEffect(isAuthenticated) {
        Log.d("AppNavigation", "isAuthenticated: ${tokenStore.getJwt().first()}")
        if (isAuthenticated && navController.currentDestination?.route == AppRoutes.AUTH) {
            navController.navigate(AppRoutes.MAIN) {
                popUpTo(AppRoutes.AUTH) { inclusive = true }
            }
        } else if (!isAuthenticated && navController.currentDestination?.route != AppRoutes.MAIN) {
            navController.navigate(AppRoutes.AUTH)
        }
    }
    // If user is authenticated, navigate to main screen


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
                
                composable("${AppRoutes.ADD_EDIT_LINK_ITEM}?linkItemId={linkItemId}") { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    AddEditLinkItemScreen(linkItemId = linkItemId)
                }

            }

        }
    }

}