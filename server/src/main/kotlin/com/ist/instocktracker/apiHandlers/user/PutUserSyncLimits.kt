package com.ist.instocktracker.apiHandlers.user

import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.plugins.getUser
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Handler for PUT /api/v1/user/sync-limits endpoint
 * Synchronizes the user's item limit with their current subscription tier
 */
fun Route.putUserSyncLimits() {
    put("sync-limits") {
        val userRepository = ServiceProvider.userRepository
        val linkItemRepository = ServiceProvider.linkItemRepository
        val currentUser = call.getUser()
        
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
            ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ApiError(error = "Missing or invalid 'limit' parameter")
            )

        try {
            val currentItemCount = linkItemRepository.count(currentUser.id)
            val remaining = limit - currentItemCount
            
            userRepository.setTrackableItemsLeft(currentUser.id, remaining)
            
            return@put call.respond(HttpStatusCode.OK, mapOf("trackableItemsLeft" to remaining))
        } catch (e: Exception) {
            return@put call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(error = "Failed to sync limits: ${e.message}")
            )
        }
    }
}
