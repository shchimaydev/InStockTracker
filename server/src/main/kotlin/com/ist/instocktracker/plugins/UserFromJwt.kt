package com.ist.instocktracker.plugins

import com.ist.instocktracker.data.User
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.*

// 1. Create a type-safe key to store and retrieve the User from the call's attributes
val UserAttributeKey = AttributeKey<User>("user")

// 2. This is our custom plugin. It's a "Route Scoped Plugin"
val UserFromPrincipal = createRouteScopedPlugin("UserFromPrincipal") {
    on(AuthenticationChecked) { call ->
        val authHeader = call.request.headers["Authorization"]
        println("Bearer: $authHeader")
        val pr = call.principal<JWTPrincipal>()
        println("Principal: $pr")

//        call.respond(
//            HttpStatusCode.Unauthorized,
//            mapOf("error" to "Missing authentication principal")
//        )
        call.principal<JWTPrincipal>()?.let {
            val user = ServiceProvider.userRepository.get(it.payload.subject) ?: return@on call.respond(
                HttpStatusCode.Forbidden,
                mapOf("error" to "User not found or inactive")
            )

            println("User from JWT: $user")
            call.attributes.put(UserAttributeKey, user)
        }

    }

//    onCall { call ->
//        val authHeader = call.request.headers["Authorization"]
//        println("Bearer: $authHeader")
//        val pr = call.principal<JWTPrincipal>()
//        println("Principal: $pr")
//
////        call.respond(
////            HttpStatusCode.Unauthorized,
////            mapOf("error" to "Missing authentication principal")
////        )
//        call.principal<JWTPrincipal>()?.let {
//            val user = ServiceProvider.userRepository.get(it.payload.subject) ?: return@onCall call.respond(
//                HttpStatusCode.Forbidden,
//                mapOf("error" to "User not found or inactive")
//            )
//
//            println("User from JWT: $user")
//            call.attributes.put(UserAttributeKey, user)
//        }
//
//
//    }
}


// 3. (Optional but highly recommended) Create a helper extension function for easy access
// This makes retrieving the user in your route handlers much cleaner.
fun ApplicationCall.getUser(): User {
    // Retrieve the user from attributes. The 'get' will throw an exception if the key is not found,
    // which is what we want, because this function should only be called within a protected route.
    val user = attributes[UserAttributeKey]
    return user
}