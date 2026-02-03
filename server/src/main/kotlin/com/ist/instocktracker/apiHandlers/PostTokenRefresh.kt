package com.ist.instocktracker.apiHandlers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.data.auth.RefreshTokenRequest
import com.ist.instocktracker.data.auth.TokenResponse
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.postTokenRefresh() {
    val jwtConfig = ServiceProvider.jwtConfig

    post("/token-refresh") {
        try {
            println("Refresh token endpoint called")
            // Get the refresh token from request body
            val (refreshToken) = call.receive<RefreshTokenRequest>()

            val algorithm = Algorithm.HMAC256(jwtConfig.secret)
            val verifier = JWT.require(algorithm)
                .withAudience(jwtConfig.audience)
                .withIssuer(jwtConfig.issuer)
                .withClaim("type", "refresh") // Ensure it's a refresh token
                .build()

            val decodedRefreshToken = try {
                verifier.verify(refreshToken)
            } catch (e: JWTVerificationException) {
                // Refresh token is invalid or expired
                println("Refresh token verification failed: ${e.message}")
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiError(error = "Invalid or expired refresh token")
                )
            }


            // Get user from JWT principal (this validates the access token)
            val user = decodedRefreshToken.subject.let {
                if (it == null) {
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiError(error = "Refresh token does not contain user information")
                    )
                }
                // Verify user still exists in database
                val userRepository = ServiceProvider.userRepository
                val user = userRepository.get(it)
                if (user === null) {
                    return@post call.respond(
                        HttpStatusCode.Forbidden,
                        ApiError(error = "User not found")
                    )
                }

                return@let user
            }


            // TODO: In a production system, you would validate the refresh token here
            // For now, we assume if the access token is valid and user exists, we can refresh

            // Generate new tokens
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
                ApiError(error = "Could not refresh token")
            )
        }
    }
    //}
}