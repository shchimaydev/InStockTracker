package com.ist.instocktracker

import com.ist.instocktracker.apiHandlers.linkItem.*
import com.ist.instocktracker.data.RunUpdateJobBody
import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.db.FirestoreProvider.db
import com.ist.instocktracker.db.FirestoreProvider.linksCollection
import com.ist.instocktracker.services.SchedulerService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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


    println("Project ID from environment: ${System.getenv("GAE_APPLICATION")}")
    val projectId = System.getenv("GAE_APPLICATION")?.split("~")[1] ?: "instocktracker-464721"

    val location = "europe-west3"
    val serverUrl = "https://$projectId.ey.r.appspot.com"


    // Initialize the SchedulerService
    val schedulerService = SchedulerService(
        location = location,     // Replace with your actual location
        serverBaseUrl = serverUrl // Replace with your actual server URL
    )

    routing {
        // Keep the original root endpoint for testing
        get("/") {
            val documents = db.collection(linksCollection).listDocuments()
            val snapshots = coroutineScope {
                documents.map { docRef ->
                    async {
                        withContext(Dispatchers.IO) {
                            docRef.get().get().toLinkItem()
                        }
                    }
                }.awaitAll()
            }

            println("Snapshot test - $snapshots")
            call.respondText("Snapshot test - $snapshots")
        }

        // API v1 routes
        route("api/v1") {
            route("link-item") {
                getLinkItem()
                postLinkItem(schedulerService)
                putLinkItem()
                deleteLinkItem()

                post("{id}/check") {
                    val id = call.parameters["id"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing or invalid ID")
                    )
                    // Empty handler for now
                    call.respond(HttpStatusCode.OK, mapOf("status" to "Job received", "linkItemId" to id))
                }
            }
            // Route for running update job

        }
    }
}
