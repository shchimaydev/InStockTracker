package com.ist.instocktracker

import com.ist.instocktracker.apiHandlers.linkItem.*
import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.db.FirestoreProvider.db
import com.ist.instocktracker.db.FirestoreProvider.linksCollection
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
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
                postLinkItem()
                putLinkItem()
                deleteLinkItem()
            }
        }
    }
}
