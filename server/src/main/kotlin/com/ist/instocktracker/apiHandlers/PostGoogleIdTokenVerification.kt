package com.ist.instocktracker.apiHandlers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ist.instocktracker.data.PostGoogleIdVerificationBody
import com.ist.instocktracker.data.User
import com.ist.instocktracker.data.auth.TokenResponse
import com.ist.instocktracker.services.IdTokenVerifierService
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun Route.postGoogleIdTokenVerification(idTokenVerifierService: IdTokenVerifierService) {
    post("/id-verification") {
        println("Are we hitting this ???")
        try {


            val userRepository = ServiceProvider.userRepository
            val (id) = call.receive<PostGoogleIdVerificationBody>()

            val payload = idTokenVerifierService.verify(id) ?: return@post call.respond(
                message = "Could not get payload from Google Id Token",
                status = HttpStatusCode.Unauthorized
            )

            println("Payload: $payload")

            val userId =
                payload.subject ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    "Missing subject in Google token"
                )
            val email = payload.email
            val name = payload["name"] as? String

            println("User id: $userId")


            val user = User(id = userId, name = name, email = email, googleIdToken = id)
            val userExists = userRepository.exists(userId)
            if (!userExists) {
                userRepository.save(user)
            }

            // Read JWT configuration
            val cfg = call.application.environment.config
            val jwtSecret =
                cfg.propertyOrNull("app.jwt.secret")?.getString() ?: System.getenv("JWT_SECRET")
                ?: "dev-secret-change-me"
            val jwtIssuer =
                cfg.propertyOrNull("app.jwt.issuer")?.getString() ?: System.getenv("JWT_ISSUER")
                ?: "instocktracker-server"
            val jwtAudience = cfg.propertyOrNull("app.jwt.audience")?.getString() ?: System.getenv("JWT_AUDIENCE")
            ?: "instocktracker-clients"
            val accessTtlSec =
                cfg.propertyOrNull("app.jwt.accessTokenTtlSec")?.getString()?.toLongOrNull() ?: (60 * 60L)
            val refreshTtlSec =
                cfg.propertyOrNull("app.jwt.refreshTokenTtlSec")?.getString()?.toLongOrNull() ?: 30L * 24 * 60 * 60

            val algorithm = Algorithm.HMAC256(jwtSecret)
            val now = System.currentTimeMillis()

            val accessToken = JWT.create()
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .withSubject(userId)
                .withClaim("email", email)
                .withClaim("name", name)
                .withIssuedAt(Date(now))
                .withExpiresAt(Date(now + accessTtlSec * 1000))
                .sign(algorithm)
            
            val refreshToken = JWT.create()
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .withSubject(userId)
                .withClaim("type", "refresh")
                .withIssuedAt(Date(now))
                .withExpiresAt(Date(now + refreshTtlSec * 1000))
                .sign(algorithm)

            println("Access token: $accessToken")

            call.respond(
                HttpStatusCode.OK,
                TokenResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = accessTtlSec
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Could not verify Google ID token")
        }
    }
}