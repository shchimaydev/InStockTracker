package com.ist.instocktracker.feature.linkitem

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController

@Composable
fun AddFromShareScreen(
    shareUrl: String?,
    viewModel: AddFromSharedViewModel = viewModel()
) {
    Log.d("AddFromShareScreen", "sharing url: $shareUrl")
    val navController = LocalNavController.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(shareUrl) {
        Log.d("AddFromShareScreen", "How many times this is called? sharing url: $shareUrl")
        shareUrl?.let {
            viewModel.createLinkItemFromShape(it) { linkItemId ->
                navController.navigate(AppRoutes.linkItemDetails(linkItemId)) {
                    popUpTo(AppRoutes.addFromShare(shareUrl)) { inclusive = true }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (uiState.isLoading) CircularProgressIndicator()

        uiState.error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
    }
}