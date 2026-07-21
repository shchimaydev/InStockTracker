package com.ist.instocktracker.apiHandlers.linkItem.check

import com.google.cloud.firestore.DocumentReference
import com.ist.instocktracker.apiHandlers.linkItem.check.precheck.CheckPipelineResult
import com.ist.instocktracker.data.*
import com.ist.instocktracker.services.ServiceProvider
import com.ist.instocktracker.services.db.FirestoreProvider.db
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.util.*

/**
 * Handler for POST /api/v1/link-items/{id}/check endpoint.
 * Runs the item's URL through [ServiceProvider.checkPipeline] (cheap structured-data pre-checks
 * before ever falling back to a Browserless screenshot + Gemini vision call), notifies the user on
 * a newly-matched status, and persists the result.
 */
fun Route.postCheck() {
    post("{id}/check") {
        try {
            val linkItemRepository = ServiceProvider.linkItemRepository
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiError(error = "Missing or invalid ID")
            )

            val docRef = linkItemRepository.getReference(id)
            val linkItem = linkItemRepository.get(id) ?: return@post call.respond(
                HttpStatusCode.NotFound,
                ApiError(error = "Link item was not found")
            )

            // Random delay to spread load across scheduled jobs.
            delay((1000..5000).random().toLong())

            val pipelineResult = ServiceProvider.checkPipeline.run(linkItem)

            if (pipelineResult.result && linkItem.lastCheckResult != null && linkItem.lastCheckResult == false) {
                val user = ServiceProvider.userRepository.get(linkItem.userId)
                    ?: return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiError(error = "Item does not have user id")
                    )
                val payload = PushPayload(
                    title = "Availability matched: ${linkItem.mode.name}",
                    body = (linkItem.label ?: linkItem.link),
                    linkItemId = id
                )
                ServiceProvider.notificationService.sendToUser(user, payload)
            }

            val timestamp = Clock.System.now().toString()
            persistCheckResult(docRef, linkItem, pipelineResult, timestamp)

            call.respond(
                HttpStatusCode.OK,
                CheckResponse(
                    status = "success",
                    linkItemId = id,
                    result = pipelineResult.result,
                    timestamp = timestamp
                )
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(error = "Failed to check link item: ${e.message}")
            )
        }
    }
}

private suspend fun persistCheckResult(
    docRef: DocumentReference,
    linkItem: LinkItem,
    pipelineResult: CheckPipelineResult,
    timestamp: String
) {
    val batch = db.batch()

    batch.update(
        docRef,
        mapOf(
            "lastCheckResult" to pipelineResult.result,
            "lastCheckDate" to timestamp
        )
    )

    val logDocRef = docRef.collection("logs").document(UUID.randomUUID().toString())
    batch.set(
        logDocRef,
        mapOf(
            "timestamp" to timestamp,
            "jobStatus" to true,
            "mode" to linkItem.mode.name,
            "result" to pipelineResult.result,
            "resolvedVia" to pipelineResult.resolvedVia.name,
            "preCheckSource" to pipelineResult.preCheckSource?.name
        )
    )

    withContext(Dispatchers.IO) {
        batch.commit().get()
    }
}
