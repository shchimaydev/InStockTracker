package com.ist.instocktracker

import com.ist.instocktracker.apiHandlers.linkItem.*
import com.ist.instocktracker.apiHandlers.linkItem.check.postCheck
import com.ist.instocktracker.apiHandlers.user.putUserSyncLimits
import com.ist.instocktracker.apiHandlers.user.getUserMe
import com.ist.instocktracker.apiHandlers.postDeviceToken
import com.ist.instocktracker.apiHandlers.postGoogleIdTokenVerification
import com.ist.instocktracker.apiHandlers.postTokenRefresh
import com.ist.instocktracker.authentication.AuthNames
import com.ist.instocktracker.authentication.jwtGoogleOidc
import com.ist.instocktracker.authentication.jwtMainAuth
import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.plugins.UserFromPrincipal
import com.ist.instocktracker.services.ServiceProvider
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
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
                ApiError(error = cause.message ?: "Unknown error")
            )
        }
    }

    ServiceProvider.init(application = this)

    val idTokenVerifierService = ServiceProvider.idTokenVerifierService

    install(Authentication) {
        jwtMainAuth(AuthNames.JWT_MAIN)
        jwtGoogleOidc(AuthNames.JWT_GOOGLE_OIDC)
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
                    postLinkItem()
                    putLinkItem()
                    deleteLinkItem()
                }

                // Secured with Google OIDC
                //authenticate("google-oidc") {
                postCheck()
                //}
            }

            authenticate("auth-jwt") {
                install(UserFromPrincipal)
                postDeviceToken()
                route("user") {
                    getUserMe()
                    putUserSyncLimits()
                }
            }

            postGoogleIdTokenVerification(idTokenVerifierService)
            postTokenRefresh()
        }
    }

    monitor.subscribe(ApplicationStopped) {
        log.info("Application stopped")
        ServiceProvider.stop()
    }
}
