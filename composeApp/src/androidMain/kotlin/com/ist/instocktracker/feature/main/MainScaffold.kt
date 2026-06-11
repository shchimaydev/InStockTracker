package com.ist.instocktracker.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.feature.billing.SubscriptionViewModel
import com.ist.instocktracker.data.SubscriptionTier
import com.ist.instocktracker.navigation.Route
import com.ist.instocktracker.services.ServiceLocator
import com.ist.instocktracker.utils.LocalNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(content: @Composable (paddingValue: PaddingValues) -> Unit) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val isAuthenticated by ServiceLocator.tokenStore.isAuthenticated().collectAsState(initial = false)
    val subVm = viewModel<SubscriptionViewModel>()
    val subState by subVm.subscriptionState.collectAsState()

    MainScaffoldContent(
        isAuthenticated = isAuthenticated,
        currentTier = subState.tier,
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
    isAuthenticated: Boolean,
    currentTier: SubscriptionTier,
    onLogout: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    content: @Composable (paddingValue: PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Drawer header
                Text(
                    text = "InStock Tracker",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))



                if (isAuthenticated) {
                    NavigationDrawerItem(
                        label = { 
                            Column {
                                Text(fontSize = 20.sp, fontWeight = FontWeight.Bold, text = "Subscribe")
                                if (currentTier != SubscriptionTier.FREE) {
                                    Text(
                                        text = "Current: ${currentTier.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Subscribe",
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onNavigateToPaywall()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout button
                Button(
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Log out")
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {},
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
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {},
            containerColor = Color.White
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

@Preview
@Composable
fun MainScaffoldPreview() {
    MainScaffoldContent(
        isAuthenticated = true,
        currentTier = SubscriptionTier.FREE,
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