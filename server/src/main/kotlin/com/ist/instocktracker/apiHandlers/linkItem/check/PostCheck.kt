package com.ist.instocktracker.apiHandlers.linkItem.check

import com.google.genai.types.Content
import com.google.genai.types.GenerateContentResponse
import com.google.genai.types.Part
import com.ist.instocktracker.data.*
import com.ist.instocktracker.services.ServiceProvider
import com.ist.instocktracker.services.ServiceProvider.httpClient
import com.ist.instocktracker.services.db.FirestoreProvider.db
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Handler for POST /api/v1/link-items/{id}/check endpoint
 * Checks the status of a link item by scraping the page and evaluating with AI
 */
fun Route.postCheck() {
    post("{id}/check") {
        try {
            val linkItemRepository = ServiceProvider.linkItemRepository
            // Get the link item ID from the path parameters
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiError(error = "Missing or invalid ID")
            )

            val docRef = linkItemRepository.getReference(id)
            val linkItem = linkItemRepository.get(id) ?: return@post call.respond(
                HttpStatusCode.NotFound,
                ApiError(error = "Link item was not found")
            )


            println("Checking link item: $linkItem")

            // Random delay between 1 and 60 seconds to spread the load
            val randomDelayMillis = (1000..5000).random().toLong()
            println("Delaying for $randomDelayMillis ms...")
            val delayStart = System.currentTimeMillis()
            delay(randomDelayMillis)
            println("Delay took ${System.currentTimeMillis() - delayStart} ms")

            // Get the HTML content of the page using Bright Data Browser API
            val scrapeStart = System.currentTimeMillis()
            val (mage, imageBytes) = scrapePage(linkItem.link, true)
            println("scrapePage took ${System.currentTimeMillis() - scrapeStart} ms")

            // Evaluate the HTML with GenAI
            val aiStart = System.currentTimeMillis()
            val aiResult = evaluateWithAI(imageBytes, linkItem)
            println("evaluateWithAI took ${System.currentTimeMillis() - aiStart} ms")

            // Send push notification
            if (aiResult && linkItem.lastCheckResult != null && linkItem.lastCheckResult == false) {
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


            // Get current timestamp
            val timestamp = Clock.System.now().toString()

            // 1. Get a new batch instance
            val batch = db.batch()

            // 2. Queue the update for the main document
            val mainDocumentUpdate = mapOf(
                "lastCheckResult" to aiResult,
                "lastCheckDate" to timestamp
            )
            batch.update(docRef, mainDocumentUpdate)

            // 3. Queue the creation of the new log entry in the subcollection
            val logDocRef = docRef.collection("logs").document(UUID.randomUUID().toString())
            val logEntry = mapOf(
                "timestamp" to timestamp,
                "jobStatus" to true,
                "mode" to linkItem.mode.name,
                "result" to aiResult
            )
            batch.set(logDocRef, logEntry)

            // 4. Commit the batch to execute all writes at once
            val dbStart = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                batch.commit().get()
            }
            println("Firestore batch commit took ${System.currentTimeMillis() - dbStart} ms")

            // Respond with success
            call.respond(
                HttpStatusCode.OK,
                CheckResponse(
                    status = "success",
                    linkItemId = id,
                    result = aiResult,
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

/**
 * Scrapes a web page using Bright Data Browser API
 */
data class ScrapePageResponse(val imagePath: String?, val imageBytes: ByteArray)

suspend fun scrapePage(url: String, saveScreenshot: Boolean = false): ScrapePageResponse {

    try {

        println("Doing a request to browserless: $url")
        val networkStart = System.currentTimeMillis()
        val response = httpClient.post("https://production-sfo.browserless.io/screenshot") {
            parameter("token", ServiceProvider.config.browserlessToken)
            contentType(ContentType.Application.Json)
            accept(ContentType.Image.PNG)
            setBody<BrowserlessScreenshotBody>(
                BrowserlessScreenshotBody(
                    url = url,
                    viewport = BrowserlessViewport(width = 1920, height = 1080),
                    options = BrowserlessScreenshotOptions(
                        fullPage = true,
                        type = "png"
                    )
                )
            )
        }

        val ct = response.headers[HttpHeaders.ContentType] ?: "<missing>"
        println("Browserless network request took ${System.currentTimeMillis() - networkStart} ms")
        require(ct.startsWith("image/png")) { "Expected image/png but got $ct" }

        val bytes = response.body<ByteArray>()
        println("waiting for response with bytes ${bytes}")


        var imagePath: Path? = null
        if (saveScreenshot) {
            // Create parent directories if they don't exist
            imagePath =
                Paths.get("./server/src/main/resources/screenshots/screenshot-${System.currentTimeMillis()}.png")
            Files.createDirectories(imagePath.parent)
            Files.write(imagePath, bytes)
        }

        return ScrapePageResponse(imagePath?.toString(), bytes)

    } catch (e: Exception) {
        throw RuntimeException("Failed to scrape page: ${e.message}", e)
    }
//    }
}

/**
 * Evaluates the HTML content with GenAI to determine if the item status matches the expected mode
 */
suspend fun evaluateWithAI(imageBytes: ByteArray, linkItem: LinkItem): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            // Create a prompt for the AI
            val prompt = """
                You are an expert at analyzing e-commerce product pages. 
                I'll provide you with the screenshot of a product page, and I need you to determine if the product is in the following state: ${linkItem.mode.name}.
                
                Here are the possible states:
                - IN_STOCK: The product is available for immediate purchase
                - PRE_ORDER: The product is available for pre-order but not yet released
                - OUT_OF_STOCK: The product is currently unavailable for purchase
                
                Please analyze the attached image and identify if an item that is being sold in this page is in the state ${linkItem.mode.name}. Respond with ONLY "true" if the product availability match ${linkItem.mode.name}  or "false" if it doesn't match.
            """.trimIndent()

            //val imageBytes = Files.readAllBytes(Paths.get(imagePath))
            val contents =
                Content.builder().parts(Part.fromBytes(imageBytes, "image/png"), Part.fromText(prompt)).build()

            val geminiStart = System.currentTimeMillis()
            val response: GenerateContentResponse = ServiceProvider.genAi.geminiClient.models.generateContent(
                "gemini-2.5-flash-lite",
                contents, null
            )
            println("Gemini API call took ${System.currentTimeMillis() - geminiStart} ms")
            val responseText = response.text()

            println("GenAI response: $responseText")
            responseText == "true"
        } catch (e: Exception) {
            throw RuntimeException("Failed to evaluate with AI: ${e.message}", e)
        }
    }
}