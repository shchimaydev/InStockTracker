package com.ist.instocktracker.apiHandlers

import com.ist.instocktracker.data.PostGoogleIdVerificationBody
import com.ist.instocktracker.services.IdTokenVerifierService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postGoogleIdTokenVerification(idTokenVerifierService: IdTokenVerifierService) {
    post("id-verification") {
        val (id) = call.receive<PostGoogleIdVerificationBody>()

        val payload = idTokenVerifierService.verify(id) ?: return@post call.respond(
            message = "Could not get payload from Google Id Token",
            status = HttpStatusCode.Unauthorized
        )

        val userId = payload.subject
        val email = payload.email
        val name = payload.get("name") as? String

        println("Payload: $payload")
        println("User props: $userId , $email , $name")
    }
}