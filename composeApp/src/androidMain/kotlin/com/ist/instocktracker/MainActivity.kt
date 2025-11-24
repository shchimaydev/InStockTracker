package com.ist.instocktracker

import android.Manifest
import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ist.instocktracker.navigation.AppNavigation
import com.ist.instocktracker.services.ServiceLocator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val deepLink = MutableStateFlow<String?>(null)

    // Support pre-API 35 onNewIntent to handle notification taps across all devices
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkIntent(intent)
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        setIntent(intent)
        checkIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // for cold start
        checkIntent(intent)

        ServiceLocator.init(applicationContext)

        setContent {
            val link by deepLink.collectAsState()
            fun handleNewDeepLink(action: (String?) -> Unit) {
                intent.removeExtra("linkItemId") // remove linkItemId (is don't know if it is needed
                action(link)
                deepLink.value = null
            }

            InStockApp(deepLink)
            RequestPostNotificationsPermission()
        }
    }

    private fun checkIntent(intent: Intent) {
        if (intent.hasExtra("linkItemId")) {
            val linkId = intent.getStringExtra("linkItemId")
            linkId?.let {
                deepLink.value = it
            }
        }
    }
}

@Composable
fun InStockApp(deepLink: Flow<String?>) {
    //val navController = rememberNavController()

    //val isAuthenticated by tokenStore.isAuthenticated().collectAsState(initial = null)

//    if (isAuthenticated == null) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator()
//        }
//    }
//
//    LaunchedEffect(isAuthenticated) {
//        Log.d("AppNavigation", "isAuthenticated: ${tokenStore.getJwt().first()}")
//        isAuthenticated?.let { finalIsAuthenticated ->
//            if (finalIsAuthenticated && navController.currentDestination?.route == AppRoutes.AUTH) {
//                navController.navigate(AppRoutes.MAIN) {
//                    popUpTo(AppRoutes.AUTH) { inclusive = true }
//                }
//            } else if (!finalIsAuthenticated && navController.currentDestination?.route != AppRoutes.MAIN) {
//                navController.navigate(AppRoutes.AUTH)
//            }
//        }
//
//    }

//    LaunchedEffect(Unit) {
//        handleNewDeepLink { linkId ->
//            linkId?.let { navController.navigate("${AppRoutes.ADD_EDIT_LINK_ITEM}?linkItemId=${linkId}") }
//        }
//    }


    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(deepLink = deepLink)
        }
    }
}

@Composable
private fun RequestPostNotificationsPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted -> no-op for now */ }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}