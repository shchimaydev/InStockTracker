package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Handler for GET /api/v1/link-item/{id} endpoint
 * Retrieves a link item by ID
 */
fun Route.getLinkItem() {
    get("{id}") {
        val linkItemRepository = ServiceProvider.linkItemRepository
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Missing or invalid ID")
        )

        val linkItem = linkItemRepository.get(id)
        if (linkItem == null) {
            return@get call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Link item not found")
            )
        }

        return@get call.respond(linkItem)
    }
}