package com.ist.instocktracker

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ist.instocktracker.apiHandlers.linkItem.*
import com.ist.instocktracker.apiHandlers.linkItem.check.postCheck
import com.ist.instocktracker.apiHandlers.postGoogleIdTokenVerification
import com.ist.instocktracker.apiHandlers.postTokenRefresh
import com.ist.instocktracker.config.JwtConfig
import com.ist.instocktracker.plugins.UserFromPrincipal
import com.ist.instocktracker.services.IdTokenVerifierService
import com.ist.instocktracker.services.SchedulerService
import com.ist.instocktracker.services.ServiceProvider
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.module() {
    val cfg = environment.config
    //FirebaseApp.initializeApp()

    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            prettyPrint = true
            isLenient = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    println("Gemini api key: ${environment.config.property("app.gemini.apiKey").getString()}")


    ServiceProvider.init(application = this)

    println("Project ID from environment: ${System.getenv("GAE_APPLICATION")}")
    val projectId = System.getenv("GAE_APPLICATION")?.split("~")?.getOrNull(1) ?: "instocktracker-464721"
    val location = "europe-west3"
    val serverUrl = "https://$projectId.ey.r.appspot.com"
    val schedulerService = SchedulerService(
        location = location,
        serverBaseUrl = serverUrl
    )
    val idTokenVerifierService = IdTokenVerifierService()
    val jwtConfig = JwtConfig.fromEnvironment(environment)

//
//    install(Sessions) {
//        cookie<User>("user") {
//            cookie.httpOnly = true
//            cookie.secure = !(cfg.propertyOrNull("ktor.development")?.getString()?.toBoolean() ?: true)
//            cookie.maxAgeInSeconds = jwtConfig.refreshTokenTtlSec
//            cookie.path = "/"
//        }
//    }

    install(Authentication) {
        jwt("auth-jwt") {
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
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
            }
        }
    }


    routing {
        // Keep the original root endpoint for testing
        get("/") {
            val collectionIds = FirestoreProvider.checkConnection()
            call.respond(HttpStatusCode.OK, "Server is running")
        }

        // API v1 routes
        route("api/v1") {
            route("link-items") {
                authenticate("auth-jwt") {
                    install(UserFromPrincipal)

                    getLinkItem()
                    getLinkItemsForUser()
                    postLinkItem(schedulerService)
                    putLinkItem()
                    deleteLinkItem()
                }
                // Keep check endpoint public
                postCheck()
            }

            postGoogleIdTokenVerification(idTokenVerifierService)
            postTokenRefresh(jwtConfig)
        }
    }
}
