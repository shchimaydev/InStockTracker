package com.ist.instocktracker.services

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import java.util.*

/**
 * Service to verify Google ID tokens.
 */
class IdTokenVerifierService {

    companion object {
        // IMPORTANT: Replace with your Google Client ID
        private const val CLIENT_ID = "646354819394-mifsg27c40t85l8gh09su46si6tvcjai.apps.googleusercontent.com"
    }

    private val verifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(Collections.singletonList(CLIENT_ID))
        .build()

    /**
     * Verifies a Google ID token and returns the payload if valid.
     *
     * @param idTokenString The ID token received from the client.
     * @return The token's payload if the token is valid, otherwise null.
     */
    fun verify(idTokenString: String): GoogleIdToken.Payload? {
        return try {
            val idToken = verifier.verify(idTokenString)
            idToken?.payload
        } catch (e: Exception) {
            // Log the exception in a real application
            System.err.println("Error verifying token: ${e.message}")
            null
        }
    }
}