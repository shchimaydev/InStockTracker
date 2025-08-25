package com.ist.instocktracker.apiHandlers.linkItem

import com.ist.instocktracker.plugins.getUser
import com.ist.instocktracker.services.ServiceProvider.linkItemRepository
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getLinkItemsForUser() {
    get() {
        val currentUser = call.getUser()

        currentUser.id.let {
            val linkItems = linkItemRepository.getAll()
            println("Link items: $linkItems")
            call.respond(linkItems)
        }
    }
}