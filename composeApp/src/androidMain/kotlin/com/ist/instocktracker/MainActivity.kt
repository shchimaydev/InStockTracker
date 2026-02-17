package com.ist.instocktracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ist.instocktracker.billing.RevenueCatManager
import com.ist.instocktracker.navigation.AppNavigation
import com.ist.instocktracker.services.ServiceLocator
import com.ist.instocktracker.utils.extractUrlFromText

class MainActivity : ComponentActivity() {
    val viewModel: MainActivityViewModel by viewModels()

    // Support pre-API 35 onNewIntent to handle notification taps across all devices
    override fun onNewIntent(intent: Intent) {
        Log.d("MainActivity", "onNewIntent: $intent")
        super.onNewIntent(intent)
        setIntent(intent)
        checkPushNotificationIntent(intent) { url -> viewModel.setDeepLink(url) }
        checkSharedUrlIntent(intent) { url -> viewModel.setSharedUrl(url) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // for cold start
        checkPushNotificationIntent(intent) { url -> viewModel.setDeepLink(url) }
        checkSharedUrlIntent(intent) { url -> viewModel.setSharedUrl(url) }

        ServiceLocator.init(applicationContext)

        // Initialize RevenueCat
        RevenueCatManager.initializeWithContext(application, BuildConfig.REVENUECAT_API_KEY)

        setContent {
            InStockApp()
            RequestPostNotificationsPermission()
        }
    }

    private fun checkSharedUrlIntent(intent: Intent, callback: (url: String) -> Unit) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                Log.d("MainActivity", "Share Intent. sharedText: $sharedText")
                val url = extractUrlFromText(sharedText)
                if (url != null) {
                    callback(url)
                }
            }
        }
    }

    private fun checkPushNotificationIntent(intent: Intent, callback: (url: String) -> Unit) {
        if (intent.hasExtra("linkItemId")) {
            val linkId = intent.getStringExtra("linkItemId")
            linkId?.let {
                callback(it)
            }
        }
    }
}

@Composable
fun InStockApp() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
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