package com.ist.instocktracker.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ist.instocktracker.services.ServiceLocator

/**
 * Main screen that displays the Google ID Token
 * @param tokenDataStore DataStore for retrieving the Google ID Token
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(paddingValues: PaddingValues) {
    val scope = rememberCoroutineScope()

    val tokenStore = ServiceLocator.tokenStore
    val googleIdToken by tokenStore.getGoogleIdToken().collectAsState(initial = "")

    val token by tokenStore.getJwt().collectAsState(initial = null)



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

        // Display the Google ID Token
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Google ID Token:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = token?.accessToken ?: "No token found",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = googleIdToken ?: "No token found",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }


}