package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.db.FirestoreProvider.db
import com.ist.instocktracker.db.FirestoreProvider.linksCollection
import com.ist.instocktracker.services.SchedulerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handler for POST /api/v1/link-item endpoint
 * Adds a new link item and creates a schedule for it
 */
fun Route.postLinkItem(
    schedulerService: SchedulerService
) {
    post {
        val linkItem = call.receive<LinkItem>()

        // Create a document reference in Firestore
        val docRef = if (linkItem.id.isBlank()) {
            // Auto-generate ID if not provided
            db.collection(linksCollection).document()
        } else {
            // Use provided ID
            db.collection(linksCollection).document(linkItem.id)
        }

        // Create a copy of the LinkItem with the document ID
        val linkItemWithId = if (linkItem.id.isBlank()) {
            linkItem.copy(id = docRef.id)
        } else {
            linkItem
        }

        // Create a schedule for the LinkItem
        val scheduleJobId = schedulerService.createSchedule(linkItemWithId)

        // Update the LinkItem with the schedule job ID
        val updatedLinkItem = linkItemWithId.copy(scheduleJobId = scheduleJobId)

        // Create a map of the updated LinkItem fields
        val itemData = updatedLinkItem.toMap()

        // Save the updated LinkItem to Firestore
        withContext(Dispatchers.IO) {
            docRef.set(itemData).get()
        }

        // Get the created document to return
        val createdSnapshot = withContext(Dispatchers.IO) {
            docRef.get().get()
        }

        val createdItem = createdSnapshot.toLinkItem()
        if (createdItem != null) {
            call.respond(HttpStatusCode.Created, createdItem)
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Failed to create link item")
            )
        }
    }
}
