package com.ist.instocktracker.apiHandlers.linkItem

import com.google.cloud.firestore.SetOptions
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.db.FirestoreProvider.db
import com.ist.instocktracker.db.FirestoreProvider.linksCollection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handler for PUT /api/v1/link-item/{id} endpoint
 * Updates an existing link item
 */
fun Route.putLinkItem() {
    put("{id}") {
        val id = call.parameters["id"] ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing or invalid ID")
        )

        val linkItem = call.receive<LinkItem>()

        // Create a map of the LinkItem fields
        val itemData = mapOf(
            "link" to linkItem.link,
            "mode" to linkItem.mode.toString(),
            "additionalInstructions" to (linkItem.additionalInstructions ?: ""),
            "isActive" to linkItem.isActive,
            "interval" to linkItem.interval
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
            return@put
        }

        // Update the document
        withContext(Dispatchers.IO) {
            docRef.set(itemData, SetOptions.merge()).get()
        }

        // Get the updated document to return
        val updatedSnapshot = withContext(Dispatchers.IO) {
            docRef.get().get()
        }

        val updatedItem = updatedSnapshot.toLinkItem()
        if (updatedItem != null) {
            call.respond(updatedItem)
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Failed to update link item")
            )
        }
    }
}