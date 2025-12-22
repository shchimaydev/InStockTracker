package com.ist.instocktracker.feature.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ist.instocktracker.data.Platform
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.services.ServiceLocator
import com.ist.instocktracker.utils.LocalNavController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val sessionManager = ServiceLocator.sessionManager
    val api = ServiceLocator.api
    val deviceTokenManager = ServiceLocator.deviceTokenManager
    val tokenStore = ServiceLocator.tokenStore
    val coroutineScope = rememberCoroutineScope()

    val isAuthenticated by ServiceLocator.tokenStore.isAuthenticated().collectAsState(initial = false)
    if (isAuthenticated) {
        navController.navigate(AppRoutes.MAIN)
        return
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = "InStock",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Sign in button at the bottom
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        sessionManager.signIn()

                        //get and send device token
                        val storeToken = tokenStore.getDeviceToken().firstOrNull()
                        val finalToken = storeToken ?: deviceTokenManager.getCurrentToken()
                        finalToken?.let { api.sendDeviceToken(it, Platform.ANDROID) }

                    } catch (e: Exception) {
                        Log.e("AuthScreen", "Error during sign-in: ${e.message}")
                        Toast.makeText(
                            context,
                            "Sign-in failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text(
                text = "Continue with Google",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}