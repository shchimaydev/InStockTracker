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
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ist.instocktracker.MainActivityViewModel
import com.ist.instocktracker.feature.auth.AuthScreen
import com.ist.instocktracker.feature.linkitem.AddFromShareScreen
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditIntervalScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditLinkScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditModeScreen
import com.ist.instocktracker.feature.linkitem.editScreens.EditStartAtScreen
import com.ist.instocktracker.feature.main.MainListScreen
import com.ist.instocktracker.feature.main.MainScaffold
import com.ist.instocktracker.services.ServiceLocator.tokenStore
import com.ist.instocktracker.utils.LocalNavController

/**
 * Navigation routes for the app
 */
object AppRoutes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val MAIN_LIST = "main/link_items"

    //    const val LINK_ITEM = "main/link_item"
    const val DETAILS_LINK_ITEM = "main/link_item_details"

    fun addFromShare(url: String = "{url}") = "add_from_share?url=$url"

    fun linkItemDetails(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId"

    // Editor routes - functions for type-safe navigation
    fun editLabel(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_label"
    fun editLink(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_link"
    fun editMode(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_mode"
    fun editStartAt(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_start_at"
    fun editInterval(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_interval"
    fun editStatus(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_status"
    fun editImage(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_image"
    fun editInstructions(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/edit_instructions"
    fun viewLastCheck(linkItemId: String = "{linkItemId}") = "$MAIN_LIST/$linkItemId/view_last_check"
}

/**
 * Main navigation component for the app
 * @param navController The NavHostController to use for navigation
 * @param startDestination The starting destination route
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: MainActivityViewModel = viewModel()
) {
    val isAuthenticated by tokenStore.isAuthenticated().collectAsState(initial = null)
    val deepLinkState by viewModel.deepLink.collectAsState()
    val sharedUrlValue by viewModel.sharedUrl.collectAsState()
//    var startDestinationState by remember { mutableStateOf(AppRoutes.AUTH) }

    LaunchedEffect(isAuthenticated, sharedUrlValue, deepLinkState) {
        isAuthenticated?.let { authed ->
            Log.d("AppNavigation", "AppNavigation isAuthenticated: $authed")

            if (authed && sharedUrlValue != null) {
                Log.d("AppNavigation", "Navigate to ${AppRoutes.addFromShare()}")
                navController.navigate(AppRoutes.addFromShare(Uri.encode(sharedUrlValue))) {
                    popUpTo(AppRoutes.AUTH) { inclusive = true }  // Clear everything
                }
                viewModel.consumeSharedUrl()
                viewModel.consumedDeepLink() // in case both exist, consume all but process only shared url intent
                return@LaunchedEffect
            }

            if (authed) {
                //  startDestinationState = AppRoutes.MAIN

                if (deepLinkState != null) {
                    navController.navigate(AppRoutes.linkItemDetails(deepLinkState!!))
                    viewModel.consumedDeepLink()
                }
            } else {
                Log.d("AppNavigation", "Navigate to auth screen if auth is false")
                navController.navigate(AppRoutes.AUTH) {
                    popUpTo(0) { inclusive = true }
                }
            }

        }
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.AUTH
        ) {
            composable(AppRoutes.AUTH) {
                AuthScreen()
            }

            composable(
                route = AppRoutes.addFromShare(),
                arguments = listOf(
                    navArgument("url") { nullable = false }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url")?.let { Uri.decode(it) }
                Log.d("AppNavigation", "ADD_FROM_SHARE composable navigation with url: $url ")
                AddFromShareScreen(shareUrl = url)

            }

            navigation(route = AppRoutes.MAIN, startDestination = AppRoutes.MAIN_LIST) {

                composable(AppRoutes.MAIN_LIST) {
                    MainScaffold { paddingValue -> MainListScreen(paddingValue) }
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