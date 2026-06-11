package com.ist.instocktracker.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.ist.instocktracker.MainActivityViewModel
import com.ist.instocktracker.feature.auth.AuthScreen
import com.ist.instocktracker.feature.billing.PaywallRoute
import com.ist.instocktracker.feature.billing.PaywallScreen
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


/**
 * Main navigation component for the app
 * @param navController The NavHostController to use for navigation
 * @param startDestination The starting destination route
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: MainActivityViewModel = viewModel { MainActivityViewModel() }
) {
    val isAuthenticated by tokenStore.isAuthenticated().collectAsState(initial = null)
    val deepLinkState by viewModel.deepLink.collectAsState()
    val sharedUrlValue by viewModel.sharedUrl.collectAsState()

    val currentSharedUrl = sharedUrlValue
    val currentDeepLink = deepLinkState

    LaunchedEffect(isAuthenticated, sharedUrlValue, deepLinkState) {
        isAuthenticated?.let { authed ->
            println("AppNavigation: AppNavigation isAuthenticated: $authed")

//            if (authed && currentSharedUrl != null) {
//                println("AppNavigation: Navigate to ${Route.AddFromShare(currentSharedUrl)}")
//                navController.navigate(Route.AddFromShare(currentSharedUrl)) {
//                    popUpTo(Route.Auth) { inclusive = true }  // Clear everything
//                }
//                viewModel.consumeSharedUrl()
//                viewModel.consumedDeepLink() // in case both exist, consume all but process only shared url intent
//                return@LaunchedEffect
//            }

            if (authed) {
                if (currentSharedUrl != null) {
                    println("AppNavigation: Navigate to ${Route.AddFromShare(currentSharedUrl)}")
                    navController.navigate(Route.AddFromShare(currentSharedUrl)) {
                        popUpTo(Route.Auth) { inclusive = true }  // Clear everything
                    }
                    viewModel.consumeSharedUrl()
                    viewModel.consumedDeepLink() // in case both exist, consume all but process only shared url intent
                    return@LaunchedEffect
                }
                if (currentDeepLink != null) {
                    navController.navigate(Route.LinkItemDetails(currentDeepLink))
                    viewModel.consumedDeepLink()
                }
            } else {
                println("AppNavigation: Navigate to auth screen if auth is false")
                navController.navigate(Route.Auth) {
                    popUpTo(0) { inclusive = true }
                }
            }

        }
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = Route.Auth
        ) {
            composable<Route.Auth> {
                AuthScreen()
            }

            composable<Route.AddFromShare> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.AddFromShare>()
                val url = args.url

                println("AppNavigation: ADD_FROM_SHARE composable navigation with type: $url")
                AddFromShareScreen(shareUrl = "")

            }

            navigation<Route.Main>(startDestination = Route.MainList) {

                composable<Route.MainList> {
                    MainScaffold { paddingValue -> MainListScreen(paddingValue) }
                }



                composable<Route.LinkItemDetails> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.LinkItemDetails>()
                    LinkItemDetailsScreen(linkItemId = args.linkItemId)
                }

                // Editor routes - using functions with default path params
                composable<Route.EditLabel> { backStackEntry ->
                    val args = backStackEntry.toRoute<Route.EditLabel>()
                    val linkItemId = args.linkItemId
                    Text("Edit Label")
                }
                composable<Route.EditLink> { backStackEntry ->
                    val linkItemId = backStackEntry.toRoute<Route.EditLink>().linkItemId
                    EditLinkScreen(linkItemId = linkItemId)
                }
                composable<Route.EditMode> { backStackEntry ->
                    val linkItemId = backStackEntry.toRoute<Route.EditMode>().linkItemId
                    EditModeScreen(linkItemId = linkItemId)
                }
                composable<Route.EditStartAt> { backStackEntry ->
                    val linkItemId = backStackEntry.toRoute<Route.EditStartAt>().linkItemId
                    EditStartAtScreen(linkItemId = linkItemId)
                }
                composable<Route.EditInterval> { backStackEntry ->
                    val linkItemId = backStackEntry.toRoute<Route.EditInterval>().linkItemId
                    EditIntervalScreen(linkItemId = linkItemId)
                }
                composable<Route.EditStatus> { backStackEntry ->
                    val linkItemId = backStackEntry.toRoute<Route.EditStatus>().linkItemId
                    Text("Edit Status")
                }
                composable<Route.EditImage> { backStackEntry ->
                    val linkItemId = backStackEntry.toRoute<Route.EditImage>().linkItemId
                    Text("Edit Image")
                }
                composable<Route.EditInstructions> { backStackEntry ->
                    val linkItemId = backStackEntry.toRoute<Route.EditInstructions>().linkItemId
                    Text("Edit Instructions")
                }

            }

            composable<Route.Paywall> {
                PaywallRoute(
                    onDismiss = { navController.popBackStack() },
                    onPurchaseSuccess = { navController.popBackStack() },
                    onRestoreSuccess = { navController.popBackStack() }
                )
            }

        }
    }

}
