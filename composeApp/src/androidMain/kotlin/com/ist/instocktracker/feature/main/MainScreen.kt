package com.ist.instocktracker.feature.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.components.LinkItemCard
import com.ist.instocktracker.services.ServiceLocator

/**
 * Main screen that displays the Google ID Token
 * @param tokenDataStore DataStore for retrieving the Google ID Token
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(paddingValues: PaddingValues) {
    Log.d("MainScreen", "MainScreen called")
    val scope = rememberCoroutineScope()

    val tokenStore = ServiceLocator.tokenStore
    val googleIdToken by tokenStore.getGoogleIdToken().collectAsState(initial = "")

    val token by tokenStore.getJwt().collectAsState(initial = null)
    val mainVm = viewModel<MainVIewModel> { MainVIewModel(ServiceLocator.api) }

    val linkItems by mainVm.linkItems.collectAsState(initial = emptyList())

    LaunchedEffect(linkItems) {
        Log.d("MainScreen", "linkItems: $linkItems")
    }

    LaunchedEffect(mainVm) {
        mainVm.getLinkItems()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Main Screen",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        linkItems.forEach { LinkItemCard(it) }

    }


}