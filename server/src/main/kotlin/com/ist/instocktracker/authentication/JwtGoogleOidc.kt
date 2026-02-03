package com.ist.instocktracker.authentication

import com.auth0.jwk.UrlJwkProvider
import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.net.URL

fun AuthenticationConfig.jwtGoogleOidc(name: String) {
    val appConfig = ServiceProvider.config
    jwt(name) {
        val jwkProvider = UrlJwkProvider(URL("https://www.googleapis.com/oauth2/v3/certs"))
        verifier(jwkProvider, "https://accounts.google.com") {
            withAudience(appConfig.serverUrl)
            withIssuer("https://accounts.google.com", "accounts.google.com")
        }
        validate { credential ->
            val email = credential.payload.getClaim("email").asString()
            if (email == appConfig.schedulerCallerSa) {
                JWTPrincipal(credential.payload)
            } else {
                println("Google OIDC validation failed: email $email does not match expected ${appConfig.schedulerCallerSa}")
                null
            }
        }
        challenge { _, _ ->
            val failureCause = call.authentication.allFailures.joinToString("\n") { it.toString() }
            println("Invalid or missing Google OIDC token. Cause: $failureCause")
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiError(error = "Invalid or missing Google OIDC token")
            )
        }
    }
}
