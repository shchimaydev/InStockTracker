package com.ist.instocktracker.navigation

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ist.instocktracker.feature.auth.AuthScreen
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditIntervalScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditLinkScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditModeScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditStartAtScreen
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
    const val MAIN_LIST = "main/link_item"

    //    const val LINK_ITEM = "main/link_item"
    const val DETAILS_LINK_ITEM = "main/link_item_details"

    fun linkItemDetails(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId"

    // Editor routes - functions for type-safe navigation
    fun editLabel(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_label"
    fun editLink(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_link"
    fun editMode(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_mode"
    fun editStartAt(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_start_at"
    fun editInterval(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_interval"
    fun editStatus(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_status"
    fun editImage(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_image"
    fun editInstructions(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/edit_instructions"
    fun viewLastCheck(linkItemId: String = "{linkItemId}") = "main/link_item/$linkItemId/view_last_check"
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
                    navController.navigate(AppRoutes.linkItemDetails(linkId))
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

                composable(AppRoutes.linkItemDetails()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    LinkItemDetailsScreen(linkItemId = linkItemId)
                }

                // Editor routes - using functions with default path params
                composable(AppRoutes.editLabel()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    Text("Edit Label")
                }
                composable(AppRoutes.editLink()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    EditLinkScreen(linkItemId = linkItemId)
                }
                composable(AppRoutes.editMode()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    EditModeScreen(linkItemId = linkItemId)
                }
                composable(AppRoutes.editStartAt()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    EditStartAtScreen(linkItemId = linkItemId)
                }
                composable(AppRoutes.editInterval()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    EditIntervalScreen(linkItemId = linkItemId)
                }
                composable(AppRoutes.editStatus()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    Text("Edit Status")
                }
                composable(AppRoutes.editImage()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    Text("Edit Image")
                }
                composable(AppRoutes.editInstructions()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    Text("Edit Instructions")
                }
                composable(AppRoutes.viewLastCheck()) { backStackEntry ->
                    val linkItemId = backStackEntry.arguments?.getString("linkItemId")
                    Text("View Last Check")
                }

            }

        }
    }

}