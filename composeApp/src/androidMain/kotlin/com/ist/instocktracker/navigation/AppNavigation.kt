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
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsScreen
import androidx.compose.material3.Text
import com.ist.instocktracker.feature.main.MainScaffold
import com.ist.instocktracker.feature.main.MainScreen
import com.ist.instocktracker.services.ServiceLocator.tokenStore
import com.ist.instocktracker.utils.LocalNavController
import kotlinx.coroutines.flow.Flow
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
    const val DETAILS_LINK_ITEM = "main/link_item_details"

    // Editor routes
    const val EDIT_LABEL = "main/edit_label"
    const val EDIT_LINK = "main/edit_link"
    const val EDIT_MODE = "main/edit_mode"
    const val EDIT_START_AT = "main/edit_start_at"
    const val EDIT_INTERVAL = "main/edit_interval"
    const val EDIT_STATUS = "main/edit_status"
    const val EDIT_IMAGE = "main/edit_image"
    const val EDIT_INSTRUCTIONS = "main/edit_instructions"
    const val VIEW_LAST_CHECK = "main/view_last_check"
}

/**
 * Main navigation component for the app
 * @param navController The NavHostController to use for navigation
 * @param startDestination The starting destination route
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoutes.AUTH,
    deepLink: Flow<String?>
) {
//    val tokenStore = ServiceLocator.tokenStore
    val isAuthenticated by tokenStore.isAuthenticated().collectAsState(initial = null)
    val deepLinkState by deepLink.collectAsState(initial = null)
    var startDestinationState by remember { mutableStateOf(startDestination) }


    LaunchedEffect(isAuthenticated) {
        Log.d("AppNavigation", "isAuthenticated: ${tokenStore.getJwt().first()}")
        isAuthenticated?.let { finalIsAuthenticated ->
            if (finalIsAuthenticated) {
                startDestinationState = AppRoutes.MAIN

                navController.navigate(AppRoutes.MAIN) {
                    popUpTo(AppRoutes.AUTH) { inclusive = true }
                }

                // 2. Then, immediately check for Deep Link.
                // If it exists, navigate to it. This pushes the Item screen ON TOP of Main.
                Log.d("AppNavigation", "DeepLinkState: $deepLinkState")
                deepLinkState?.let { linkId ->
                    navController.navigate("${AppRoutes.ADD_EDIT_LINK_ITEM}?linkItemId=${linkId}")
                }
            }

            if (!finalIsAuthenticated) {
                startDestinationState = AppRoutes.AUTH
                navController.navigate(AppRoutes.AUTH)
            }
        }

    }

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestinationState
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

                composable("${AppRoutes.DETAILS_LINK_ITEM}?linkItemId={linkItemId}") { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    LinkItemDetailsScreen(linkItemId = linkItemId)
                }

                // Placeholders for editors
                composable("${AppRoutes.EDIT_LABEL}?linkItemId={linkItemId}") { Text("Edit Label") }
                composable("${AppRoutes.EDIT_LINK}?linkItemId={linkItemId}") { Text("Edit Link") }
                composable("${AppRoutes.EDIT_MODE}?linkItemId={linkItemId}") { Text("Edit Mode") }
                composable("${AppRoutes.EDIT_START_AT}?linkItemId={linkItemId}") { Text("Edit Start At") }
                composable("${AppRoutes.EDIT_INTERVAL}?linkItemId={linkItemId}") { Text("Edit Interval") }
                composable("${AppRoutes.EDIT_STATUS}?linkItemId={linkItemId}") { Text("Edit Status") }
                composable("${AppRoutes.EDIT_IMAGE}?linkItemId={linkItemId}") { Text("Edit Image") }
                composable("${AppRoutes.EDIT_INSTRUCTIONS}?linkItemId={linkItemId}") { Text("Edit Instructions") }
                composable("${AppRoutes.VIEW_LAST_CHECK}?linkItemId={linkItemId}") { Text("View Last Check") }

            }

        }
    }

}