package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.plugins.getUser
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Handler for POST /api/v1/link-item endpoint
 * Adds a new link item and creates a schedule for it
 */
fun Route.postLinkItem() {
    post {
        val linkItemRepository = ServiceProvider.linkItemRepository
        val schedulerService = ServiceProvider.schedulerService
        val linkItem = call.receive<LinkItem>()
        val currentUser = call.getUser()

        // Create a schedule for the LinkItem
        val scheduleJobId = schedulerService.createSchedule(linkItem)
        val updatedLinkItem = linkItem.copy(scheduleJobId = scheduleJobId, userId = currentUser.id)
        val linkItemFromDb = linkItemRepository.save(updatedLinkItem)

        if (linkItemFromDb == null) call.respond(
            HttpStatusCode.InternalServerError,
            mapOf("error" to "Failed to create link item")
        )

        if (linkItemFromDb == null) {
            return@post call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Failed to create link item")
            )
        }

        return@post call.respond(status = HttpStatusCode.Created, message = linkItemFromDb)
    }
}
