package com.ist.instocktracker.services

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ist.instocktracker.Api
import com.ist.instocktracker.data.auth.RefreshTokenException
import com.ist.instocktracker.data.auth.SessionManager
import com.ist.instocktracker.data.interfaces.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class AndroidSessionManager(
    private val context: Context,
    private val tokenStore: TokenStore,
    private val api: Api
) : SessionManager {
    private val WEB_CLIENT_ID = "646354819394-mifsg27c40t85l8gh09su46si6tvcjai.apps.googleusercontent.com"

    //private val _isSignedIn = MutableStateFlow(false)
    override val isSignedIn = MutableStateFlow(false)

    private val credentialManager = CredentialManager.create(context)

    override suspend fun silentSignIn(): Boolean {
        return try {
//            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = WEB_CLIENT_ID)
//                .setAutoSelectEnabled(true) // THIS IS THE KEY FOR SILENT SIGN-IN
//                .build()

            val options = GetGoogleIdOption.Builder()
                .setAutoSelectEnabled(true)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(options)
                .build()

            // The 'context' passed here MUST be an Activity/Fragment context for UI prompts
            // if auto-select is not possible. Since this runs in the background, we hope it succeeds silently.
            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                // Verify the new token with your backend
                val jwt = api.verifyIdToken(googleIdToken.idToken)
                // Save the new JWT (access + refresh tokens)
                tokenStore.saveJwt(jwt)
                Log.d("AndroidSessionManager", "Silent sign-in successful.")
                true
            } else {
                Log.w("AndroidSessionManager", "Silent sign-in: Unsupported credential type.")
                false
            }
        } catch (e: GetCredentialException) {
            // This is expected if the user needs to manually re-authenticate.
            Log.e("AndroidSessionManager", "Silent sign-in failed: ${e.message}")
            tokenStore.clearJwt() // Clean up any lingering invalid tokens
            false
        }
    }

    override suspend fun signIn() {
        val option = GetSignInWithGoogleOption.Builder(serverClientId = WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        when (val credential = result.credential) {
            is CustomCredential -> {
                val credentials = GoogleIdTokenCredential.createFrom(credential.data)
                val token = credentials.idToken
                Log.d("Token", token ?: "No token found")
                tokenStore.saveGoogleIdToken(token)

                try {
                    val jwt = api.verifyIdToken(token)
                    tokenStore.saveJwt(jwt)
                    val tokenFromStore = tokenStore.getJwt().first()
                    isSignedIn.value = tokenFromStore != null
                } catch (e: Exception) {
                    Log.e("AndroidSessionManager", "Error during sign-in: ${e.message}")
                    throw e
                }
            }
        }
    }

    override suspend fun signOut() {
        tokenStore.clearGoogleIdToken()
        tokenStore.clearJwt()
        credentialManager.clearCredentialState(request = ClearCredentialStateRequest())
        isSignedIn.value = false
    }

    override suspend fun <T> runWithAuth(block: suspend (api: Api) -> T): T {
        return try {
            block(api)
        } catch (e: RefreshTokenException) {
            // The refresh token is dead. Try to get a new session silently.
            if (this.silentSignIn()) {
                // We have a new session and new tokens are in the store. Retry the original call.
                block(api)
            } else {
                // Silent sign-in failed. The user must log in manually.
                // Re-throw the exception so the UI layer can catch it and navigate to the login screen.
                throw e
            }
        }
    }

}