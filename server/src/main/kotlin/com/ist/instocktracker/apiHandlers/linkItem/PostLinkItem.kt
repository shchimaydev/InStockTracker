package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.data.ApiError
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


        try {
            val updatedLinkItem = linkItem.copy(userId = currentUser.id)
            val linkItemFromDb = linkItemRepository.save(updatedLinkItem)


            linkItemFromDb
                ?.let {
                    // Create a schedule for the LinkItem
                    val scheduleJobId = schedulerService.createSchedule(it)
                    linkItemRepository.save(it.copy(scheduleJobId = scheduleJobId))
                }
                ?: throw IllegalStateException("Failed to create link item")

            return@post call.respond(status = HttpStatusCode.Created, message = linkItemFromDb)
        } catch (e: Exception) {
            return@post call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(error = "Failed to create link item. Reason: ${e.message}")
            )
        }
    }
}
