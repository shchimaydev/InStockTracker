package com.ist.instocktracker.apiHandlers.linkItem

import com.google.cloud.firestore.SetOptions
import com.ist.instocktracker.data.ApiError
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
            ApiError(error = "Missing or invalid ID")
        )

        val linkItem = call.receive<LinkItem>()

        val docRef = linkItemRepository.collection.document(id)

        // Get the existing document to compare changes
        val existingSnapshot = withContext(Dispatchers.IO) {
            docRef.get().get()
        }

        if (!existingSnapshot.exists()) {
            call.respond(
                HttpStatusCode.NotFound,
                ApiError(error = "Link item not found")
            )
            return@put
        }

        val existingItem = existingSnapshot.toLinkItem()

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
            // Handle changes to schedule-related properties
            if (existingItem != null) {
                handleLinkItemChanges(existingItem, updatedItem)
            }
            call.respond(updatedItem)
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(error = "Failed to update link item")
            )
        }
    }
}

/**
 * Handles changes to LinkItem properties that affect the scheduled job.
 * Called after the LinkItem is successfully updated.
 *
 * @param oldItem The original LinkItem before the update
 * @param newItem The updated LinkItem after the update
 */
private fun handleLinkItemChanges(oldItem: LinkItem, newItem: LinkItem) {
    when {
        oldItem.isActive != newItem.isActive -> {
            handleIsActiveChange(newItem)
        }
        oldItem.startAt != newItem.startAt -> {
            handleStartAtChange(newItem)
        }
        oldItem.interval != newItem.interval -> {
            // TODO: Handle interval changes
        }
    }
}

/**
 * Handles changes to the startAt property by updating the scheduled job's next run time.
 *
 * @param linkItem The updated LinkItem with the new startAt value
 */
private fun handleStartAtChange(linkItem: LinkItem) {
    val scheduleJobId = linkItem.scheduleJobId ?: return
    val startAt = linkItem.startAt ?: return

    val schedulerService = ServiceProvider.schedulerService
    schedulerService.setScheduleTime(scheduleJobId, startAt)
}

/**
 * Handles changes to the isActive property by pausing or resuming the scheduled job.
 *
 * @param linkItem The updated LinkItem with the new isActive value
 */
private fun handleIsActiveChange(linkItem: LinkItem) {
    val scheduleJobId = linkItem.scheduleJobId ?: return

    val schedulerService = ServiceProvider.schedulerService

    if (linkItem.isActive) {
        schedulerService.resumeJob(scheduleJobId)
    } else {
        schedulerService.pauseJob(scheduleJobId)
    }
}