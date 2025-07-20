package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.services.db.FirestoreProvider.db
import com.ist.instocktracker.services.db.FirestoreProvider.linksCollection
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.jdk9.*

/**
 * Handler for GET /api/v1/link-item/{id} endpoint
 * Retrieves a link item by ID
 */
fun Route.getLinkItem() {
    get("{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest, 
            mapOf("error" to "Missing or invalid ID")
        )

        val docRef = db.collection(linksCollection).document(id)
        val snapshot = withContext(Dispatchers.IO) {
            docRef.get().get()
        }

        val linkItem = snapshot.toLinkItem()
        if (linkItem != null) {
            println("Retrieved item: $linkItem")
            call.respond(linkItem)
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Link item not found")
            )
        }
    }
}