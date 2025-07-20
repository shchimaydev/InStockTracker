package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.services.db.FirestoreProvider.db
import com.ist.instocktracker.services.db.FirestoreProvider.linksCollection
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handler for DELETE /api/v1/link-item/{id} endpoint
 * Deletes a link item
 */
fun Route.deleteLinkItem() {
    delete("{id}") {
        val id = call.parameters["id"] ?: return@delete call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing or invalid ID")
        )

        val docRef = db.collection(linksCollection).document(id)

        // Check if document exists
        val exists = withContext(Dispatchers.IO) {
            docRef.get().get().exists()
        }

        if (!exists) {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Link item not found")
            )
            return@delete
        }

        // Delete the document
        withContext(Dispatchers.IO) {
            docRef.delete().get()
        }

        call.respond(HttpStatusCode.NoContent)
    }
}