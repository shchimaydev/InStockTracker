package com.ist.instocktracker.plugins

import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.data.User
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.coroutines.isActive

val UserAttributeKey = AttributeKey<User>("user")
val UserFromPrincipal = createRouteScopedPlugin("UserFromPrincipal") {
    on(AuthenticationChecked) { call ->
        val authHeader = call.request.headers["Authorization"]
        println("Bearer: $authHeader")
        val principal = call.principal<JWTPrincipal>()
        println("Principal: $principal")

        principal?.let {
            println("call job active? ${call.coroutineContext.isActive}")
            val user = ServiceProvider.userRepository.get(it.payload.subject) ?: return@on call.respond(
                HttpStatusCode.Forbidden,
                ApiError(error = "User not found or inactive")
            )

            println("User from JWT: $user")
            call.attributes.put(UserAttributeKey, user)
        }

    }
}


fun ApplicationCall.getUser(): User {
    val user = attributes[UserAttributeKey]
    return user
}