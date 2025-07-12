package com.ist.instocktracker

import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.db.FirestoreProvider
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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


            println("!napshot test - $snapshopts")

            call.respondText("Snapshot test - $snapshopts")
            //call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}