package com.ist.instocktracker.feature.auth

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
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
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.data.TokenDataStore
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController
import kotlinx.coroutines.launch

/**
 * Auth screen with Google Sign-In button
 * @param onSignInSuccess Callback when sign-in is successful
 * @param tokenDataStore DataStore for storing the Google ID Token
 */
@Composable
fun AuthScreen(
    //onSignInSuccess: () -> Unit,
    tokenDataStore: TokenDataStore
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val authVm = viewModel(viewModelStoreOwner = context as ComponentActivity) {
        AuthViewModel(
            tokenDataStore = tokenDataStore, appContext = context.applicationContext as Application
        )
    }
    val coroutineScope = rememberCoroutineScope()


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
                        authVm.signIn(context)
                        navController.navigate(AppRoutes.MAIN) {
                            popUpTo(AppRoutes.AUTH) { inclusive = true }
                        }
                    } catch (e: GetCredentialException) {
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
                text = "Sign in with Google",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}