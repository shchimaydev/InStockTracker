package com.ist.instocktracker.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
    val credentialManager = remember { CredentialManager.create(context) }

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
                coroutineScope.launch {
                    try {
                        // Create a credential request for Google Sign-In
                        // Note: In a production app, you would use the client ID provided:
                        // 646354819394-mifsg27c40t85l8gh09su46si6tvcjai.apps.googleusercontent.com
                        val WEB_CLIENT_ID = "646354819394-mifsg27c40t85l8gh09su46si6tvcjai.apps.googleusercontent.com"
                        val googleIdOption: GetSignInWithGoogleOption =
                            GetSignInWithGoogleOption.Builder(serverClientId = WEB_CLIENT_ID)
                                .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        // Start the sign-in flow
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )

                        Log.d("AuthScreen", "Credential result: $result")

                        // Handle the result
                        when (val credential = result.credential) {
                            is CustomCredential -> {
                                // Extract the token from the credential
                                Log.d("AuthScreen", "Credential data: ${credential.data}")
                                val googleIdTokenCredential = GoogleIdTokenCredential
                                    .createFrom(credential.data)

                                // Save token to DataStore
                                tokenDataStore.saveGoogleIdToken(googleIdTokenCredential.idToken)

                                // Log success
                                Log.d("AuthScreen", "Successfully signed in with Google: $token")

                                // Navigate to main screen
                                onSignInSuccess()
                            }

                            else -> {
                                // Handle other credential types
                                val token = "credential_type_${credential.javaClass.simpleName}"
                                tokenDataStore.saveGoogleIdToken(token)

                                Log.d(
                                    "AuthScreen",
                                    "Signed in with credential type: ${credential.javaClass.simpleName}"
                                )

                                // Navigate to main screen
                                onSignInSuccess()
                            }
                        }

                    } catch (e: GetCredentialException) {
                        // Handle sign-in errors
                        Log.e("AuthScreen", "Error during sign-in: ${e.message}")
                        Toast.makeText(
                            context,
                            "Sign-in failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // For development purposes, we'll still navigate to the main screen
                        // In a production app, you would handle the error appropriately
//                        val mockToken = "mock_token_after_error"
//                        tokenDataStore.saveGoogleIdToken(mockToken)
//                        onSignInSuccess()
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
                text = "Sign in with Google",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}