package com.ist.instocktracker.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.ist.instocktracker.AppBackgroundGradient
import com.ist.instocktracker.Ink
import com.ist.instocktracker.feature.main.components.MainDrawerContent
import com.ist.instocktracker.navigation.Route
import com.ist.instocktracker.services.ServiceLocator
import com.ist.instocktracker.utils.LocalNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(content: @Composable (paddingValue: PaddingValues) -> Unit) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    MainScaffoldContent(
        onLogout = {
            scope.launch {
                ServiceLocator.sessionManager.signOut()
                navController.navigate(Route.Auth) {
                    popUpTo(Route.MainList) { inclusive = true }
                }
            }
        },
        onNavigateToPaywall = {
            navController.navigate(Route.Paywall)
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffoldContent(
    onLogout: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    content: @Composable (paddingValue: PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Closes the drawer, then runs an optional action once it has settled.
    fun closeThen(action: () -> Unit = {}) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent(
                onDashboardClick = { closeThen() }, // already on the dashboard
                onSubscribeClick = { closeThen(onNavigateToPaywall) },
                onNotificationsClick = { closeThen() }, // TODO: wire up once a Notifications route exists
                onSettingsClick = { closeThen() },      // TODO: wire up once a Settings route exists
                onLogoutClick = { closeThen(onLogout) }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(AppBackgroundGradient)),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Links",
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Ink
                    )
                )
            },
            floatingActionButton = {},
            containerColor = Color.Transparent
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

@Preview
@Composable
fun MainScaffoldPreview() {
    MainScaffoldContent(
        onLogout = {},
        onNavigateToPaywall = {},
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Main Content")
            }
        }
    )
}
