package com.ist.instocktracker.apiHandlers.linkItem

import com.google.cloud.firestore.SetOptions
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.mappers.toLinkItem
import com.ist.instocktracker.plugins.getUser
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
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
        val currentUser = call.getUser()
        val linkItemRepository = ServiceProvider.linkItemRepository
        val id = call.parameters["id"] ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing or invalid ID")
        )

        val linkItem = call.receive<LinkItem>()


        val docRef = linkItemRepository.collection.document(id)

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
            val updatedLinkItem = linkItem.copy(userId = currentUser.id)
            docRef.set(updatedLinkItem.toMap(), SetOptions.merge()).get()
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