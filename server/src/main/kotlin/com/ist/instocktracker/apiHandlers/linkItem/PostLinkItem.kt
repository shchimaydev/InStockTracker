package com.ist.instocktracker.apiHandlers.linkItem

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
 * Handler for POST /api/v1/link-item endpoint
 * Adds a new link item
 */
fun Route.postLinkItem() {
    post {
        val linkItem = call.receive<LinkItem>()

        // Create a map of the LinkItem fields
        val itemData = linkItem.toMap()
        // Add the document to Firestore
        val docRef = if (linkItem.id.isBlank()) {
            // Auto-generate ID if not provided
            db.collection(linksCollection).document()
        } else {
            // Use provided ID
            db.collection(linksCollection).document(linkItem.id)
        }

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