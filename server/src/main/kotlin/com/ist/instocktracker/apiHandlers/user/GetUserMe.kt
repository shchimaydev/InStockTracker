package com.ist.instocktracker.apiHandlers.user

import com.ist.instocktracker.plugins.getUser
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Handler for GET /api/v1/user/me endpoint
 * Returns the current authenticated user's profile
 */
fun Route.getUserMe() {
    get("me") {
        val currentUser = call.getUser()
        call.respond(currentUser)
    }
}
