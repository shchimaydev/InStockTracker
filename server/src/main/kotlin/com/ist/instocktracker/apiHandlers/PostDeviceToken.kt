package com.ist.instocktracker.apiHandlers

import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.data.DeviceToken
import com.ist.instocktracker.data.addTokenIfPresent
import com.ist.instocktracker.data.hasDeviceToken
import com.ist.instocktracker.plugins.getUser
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postDeviceToken() {
    post("/device-token") {
        try {
            val deviceToken = call.receive<DeviceToken>()
            val user = call.getUser()

            val userRepository = ServiceProvider.userRepository

            if (user.hasDeviceToken(deviceToken.token)) {
                return@post call.respond(HttpStatusCode.NoContent, "Device token already exists")
            }

            val updatedUser = user.addTokenIfPresent(deviceToken)
            println("Updated user: $updatedUser")
            userRepository.save(updatedUser)

            call.respond(HttpStatusCode.NoContent, "Device token added successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(error = "Could not set device token for a user")
            )
        }

    }
}