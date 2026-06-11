package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.data.ApiError
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
        try {
            val linkItemRepository = ServiceProvider.linkItemRepository
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ApiError(error = "Missing or invalid ID")
            )

            // Check if document exists
            val (exists, reference) = linkItemRepository.exists(id)

            if (!exists && reference == null) {
                return@delete call.respond(
                    HttpStatusCode.NotFound,
                    ApiError(error = "Link item not found")
                )
            }

            // Delete scheduler job
            val linkItem = linkItemRepository.get(reference!!)
            linkItem!!.scheduleJobId?.let { ServiceProvider.schedulerService.deleteSchedule(it) }

            // Delete the document
            withContext(Dispatchers.IO) {
                reference.delete().get()
            }

            ServiceProvider.userRepository.updateTrackableItemsLeft(linkItem.userId, 1)

            return@delete call.respond(HttpStatusCode.NoContent)
        } catch (e: Exception) {
            return@delete call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(error = "Error deleting: ${e.message}")
            )
        }

    }
}