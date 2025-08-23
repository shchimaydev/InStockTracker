package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.services.ServiceProvider
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
        val linkItemRepository = ServiceProvider.linkItemRepository
        val id = call.parameters["id"] ?: return@delete call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing or invalid ID")
        )

        // Check if document exists
        val (exists, reference) = linkItemRepository.exists(id)

        if (!exists && reference == null) {
            return@delete call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Link item not found")
            )
        }

        // Delete the document
        withContext(Dispatchers.IO) {
            reference?.delete()?.get()
        }

        return@delete call.respond(HttpStatusCode.NoContent)
    }
}