package com.ist.instocktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ist.instocktracker.navigation.AppNavigation
import com.ist.instocktracker.services.ServiceLocator

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ServiceLocator.init(applicationContext)

        setContent {
            InStockApp()
        }
    }
}

@Composable
fun InStockApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // This is just a preview, so we can't use the actual TokenDataStore
    // In a real app, you would use a mock or fake implementation for previews
    // For now, we'll just show a simplified version of the app
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Simple preview content
            androidx.compose.material3.Text(
                text = "InStock App Preview",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}