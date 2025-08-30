package com.ist.instocktracker.apiHandlers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ist.instocktracker.config.JwtConfig
import com.ist.instocktracker.data.auth.RefreshTokenRequest
import com.ist.instocktracker.data.auth.TokenResponse
import com.ist.instocktracker.plugins.UserFromPrincipal
import com.ist.instocktracker.plugins.getUser
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.postTokenRefresh(jwtConfig: JwtConfig) {
    authenticate("auth-jwt") {
        install(UserFromPrincipal)

        post("/token-refresh") {
            try {
                // Get the refresh token from request body
                val (refreshToken) = call.receive<RefreshTokenRequest>()

                // Get user from JWT principal (this validates the access token)
                val user = call.getUser()

                // Verify user still exists in database
                val userRepository = ServiceProvider.userRepository
                val userExists = userRepository.exists(user.id)
                if (!userExists) {
                    return@post call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "User not found")
                    )
                }

                // TODO: In a production system, you would validate the refresh token here
                // For now, we assume if the access token is valid and user exists, we can refresh

                // Generate new tokens
                val algorithm = Algorithm.HMAC256(jwtConfig.secret)
                val now = System.currentTimeMillis()

                val newAccessToken = JWT.create()
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .withSubject(user.id)
                    .withClaim("email", user.email)
                    .withClaim("name", user.name)
                    .withIssuedAt(Date(now))
                    .withExpiresAt(Date(now + jwtConfig.accessTokenTtlSec * 1000))
                    .sign(algorithm)

                val newRefreshToken = JWT.create()
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .withSubject(user.id)
                    .withClaim("type", "refresh")
                    .withIssuedAt(Date(now))
                    .withExpiresAt(Date(now + jwtConfig.refreshTokenTtlSec * 25000))
                    .sign(algorithm)

                call.respond(
                    HttpStatusCode.OK,
                    TokenResponse(
                        accessToken = newAccessToken,
                        refreshToken = newRefreshToken,
                        expiresIn = jwtConfig.accessTokenTtlSec
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Could not refresh token")
                )
            }
        }
    }
}