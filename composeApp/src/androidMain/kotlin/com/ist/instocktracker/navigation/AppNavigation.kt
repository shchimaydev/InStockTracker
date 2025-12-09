package com.ist.instocktracker.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ist.instocktracker.MainActivityViewModel
import com.ist.instocktracker.feature.auth.AuthScreen
import com.ist.instocktracker.feature.linkitem.AddFromShareScreen
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditIntervalScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditLinkScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditModeScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditStartAtScreen
import com.ist.instocktracker.feature.main.MainScaffold
import com.ist.instocktracker.feature.main.MainScreen
import com.ist.instocktracker.services.ServiceLocator.tokenStore
import com.ist.instocktracker.utils.LocalNavController

/**
 * Navigation routes for the app
 */
object AppRoutes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val MAIN_LIST = "main/link_item"

    //    const val LINK_ITEM = "main/link_item"
    const val DETAILS_LINK_ITEM = "main/link_item_details"
    const val ADD_FROM_SHARE = "main/add_from_share?url={url}"

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
    viewModel: MainActivityViewModel = viewModel()
) {
//    val tokenStore = ServiceLocator.tokenStore
    val isAuthenticated by tokenStore.isAuthenticated().collectAsState(initial = null)
    val deepLinkState by viewModel.deepLink.collectAsState()
    val sharedUrlValue by viewModel.sharedUrl.collectAsState()
    var startDestinationState by remember { mutableStateOf(startDestination) }


    LaunchedEffect(isAuthenticated, sharedUrlValue, deepLinkState) {
        val auth = isAuthenticated ?: return@LaunchedEffect

        if (auth) {
            startDestinationState = AppRoutes.MAIN

            Log.d("AppNavigation", "AppNavigation sharedUrlValue: $sharedUrlValue")
            when {
                sharedUrlValue != null -> {
                    // Navigate to MAIN first (in back stack), then to share screen
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                    Log.d("AppNavigation", "Navigate to ${AppRoutes.ADD_FROM_SHARE}")
                    navController.navigate("${AppRoutes.ADD_FROM_SHARE}?url=${Uri.encode(sharedUrlValue)}")
                    viewModel.consumeSharedUrl()
                }

                deepLinkState != null -> {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                    navController.navigate(AppRoutes.linkItemDetails(deepLinkState!!))
                    viewModel.consumedDeepLink()
                }

                else -> {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                }
            }
        } else {
            startDestinationState = AppRoutes.AUTH
            navController.navigate(AppRoutes.AUTH) {
                popUpTo(0) { inclusive = true }
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
                composable(AppRoutes.ADD_FROM_SHARE) { backStackEntry ->
                    val url = backStackEntry.arguments?.getString("url")?.let { Uri.decode(it) }
                    Log.d("AppNavigation", "ADD_FROM_SHARE composable navigation with url: $url ")
                    AddFromShareScreen(shareUrl = url)

                }
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