package com.ist.instocktracker

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.db.FirestoreProvider
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val db = FirestoreProvider.db
    routing {
        get("/") {
            val documents = db.collection("links").listDocuments()
            //val linkItem: LinkItem? = document.toLinkItem()
            val snapshopts = coroutineScope {
                documents.map { docRef ->
                    // async launches a new coroutine for each database call.
                    // All of these will run in parallel.
                    async {
                        // .get() returns an ApiFuture, .await() suspends until it's ready.
                        docRef.get().get().toLinkItem()
                    }
                }.awaitAll()
            }


            println("Snapshot test - $snapshopts")

            call.respondText("Snapshot test - $snapshopts")
            //call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}