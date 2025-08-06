package com.ist.instocktracker.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ist.instocktracker.data.TokenDataStore
import kotlinx.coroutines.launch

/**
 * Auth screen with Google Sign-In button
 * @param onSignInSuccess Callback when sign-in is successful
 * @param tokenDataStore DataStore for storing the Google ID Token
 */
@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit,
    tokenDataStore: TokenDataStore
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        
        // Title in the middle
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
                // For demonstration purposes, we'll simulate a successful sign-in
                // In a real app, you would implement the Google Sign-In flow here
                // using the Credential Manager API as described in the documentation
                coroutineScope.launch {
                    // Simulate getting a token
                    val mockToken = "mock_google_id_token_for_demo"
                    
                    // Save token to DataStore
                    tokenDataStore.saveGoogleIdToken(mockToken)
                    
                    // Navigate to main screen
                    onSignInSuccess()
                    
                    Log.d("AuthScreen", "Signed in with mock token: $mockToken")
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
                text = "Sign in with Google",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}