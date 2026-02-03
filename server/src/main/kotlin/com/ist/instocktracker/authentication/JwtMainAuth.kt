package com.ist.instocktracker.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.services.ServiceProvider.jwtConfig
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun AuthenticationConfig.jwtMainAuth(name: String) {
    jwt(name) {
        realm = jwtConfig.realm
        verifier(
            JWT
                .require(Algorithm.HMAC256(jwtConfig.secret))
                .withAudience(jwtConfig.audience)
                .withIssuer(jwtConfig.issuer)
                .build()
        )
        validate { credential ->
            println("Validating JWT token ${credential.payload}")
            if (credential.payload
                    .getClaim("sub")
                    .asString()
                    .isNullOrBlank()
            ) {
                null
            } else {
                JWTPrincipal(credential.payload)
            }
        }
        challenge { _, _ ->
            val failureCause = call.authentication.allFailures.joinToString("\n") { it.toString() }
            println("Invalid or missing token in authentication header. Cause: $failureCause")
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiError(error = "Invalid or missing token")
            )
        }
    }
}