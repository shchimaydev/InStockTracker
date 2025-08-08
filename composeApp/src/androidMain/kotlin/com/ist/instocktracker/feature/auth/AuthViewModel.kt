package com.ist.instocktracker.feature.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ist.instocktracker.data.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AuthViewModel(
    private val appContext: Application,
    private val tokenDataStore: TokenDataStore
) : AndroidViewModel(appContext) {


    private val credentialManager by lazy { CredentialManager.create(appContext) }
    private val WEB_CLIENT_ID = "646354819394-mifsg27c40t85l8gh09su46si6tvcjai.apps.googleusercontent.com"

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    // Call from UI (pass in the current context for UI-bound calls)
    suspend fun signIn(currentContext: Context) {
        val option = GetSignInWithGoogleOption.Builder(serverClientId = WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val result = credentialManager.getCredential(
            request = request,
            context = currentContext
        )

        when (val credential = result.credential) {
            is CustomCredential -> {
                val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                Log.d("Token", token ?: "No token found")
                tokenDataStore.saveGoogleIdToken(token)
                _isSignedIn.value = true
            }

            else -> {
                // Handle other credential types if needed
                _isSignedIn.value = true
            }
        }
    }

    suspend fun signOut() {
        tokenDataStore.clearGoogleIdToken()
        credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
        _isSignedIn.value = false
    }
}