package com.ist.instocktracker

import com.ist.instocktracker.apiHandlers.linkItem.check.postCheck
import com.ist.instocktracker.apiHandlers.linkItem.deleteLinkItem
import com.ist.instocktracker.apiHandlers.linkItem.getLinkItem
import com.ist.instocktracker.apiHandlers.linkItem.postLinkItem
import com.ist.instocktracker.apiHandlers.linkItem.putLinkItem
import com.ist.instocktracker.apiHandlers.postGoogleIdTokenVerification
import com.ist.instocktracker.services.GenAI
import com.ist.instocktracker.services.IdTokenVerifierService
import com.ist.instocktracker.services.SchedulerService
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.module() {
    // Install content negotiation for JSON
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            prettyPrint = true
            isLenient = true
        })
    }

    // Install status pages for error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }

    println("Gemini api key: ${this.environment.config.property("app.gemini.apiKey").getString()}")
    // Initialize AI model with application config variables
    GenAI.init(this)


    println("Project ID from environment: ${System.getenv("GAE_APPLICATION")}")
    val projectId = System.getenv("GAE_APPLICATION")?.split("~")[1] ?: "instocktracker-464721"

    val location = "europe-west3"
    val serverUrl = "https://$projectId.ey.r.appspot.com"


    // Initialize the SchedulerService
    val schedulerService = SchedulerService(
        location = location,     // Replace with your actual location
        serverBaseUrl = serverUrl // Replace with your actual server URL
    )
    val idTokenVerifierService = IdTokenVerifierService()

    routing {
        // Keep the original root endpoint for testing
        get("/") {
            val collectionIds = FirestoreProvider.checkConnection()
//            val snapshots = coroutineScope {
//                documents.map { docRef ->
//                    async {
//                        withContext(Dispatchers.IO) {
//                            docRef.get().get().toLinkItem()
//                        }
//                    }
//                }.awaitAll()
//            }

            //println("Snapshot test - $snapshots")
            call.respond(HttpStatusCode.OK, "Server is running")
        }

        // API v1 routes
        route("api/v1") {
            route("link-item") {
                getLinkItem()
                postLinkItem(schedulerService)
                putLinkItem()
                deleteLinkItem()

                postCheck()
            }

            postGoogleIdTokenVerification(idTokenVerifierService)
        }
    }
}
