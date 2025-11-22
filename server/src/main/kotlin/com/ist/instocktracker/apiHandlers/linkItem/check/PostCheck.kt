package com.ist.instocktracker.apiHandlers.linkItem.check

import com.google.genai.types.Content
import com.google.genai.types.GenerateContentResponse
import com.google.genai.types.Part
import com.ist.instocktracker.data.CheckResponse
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.PushPayload
import com.ist.instocktracker.services.ServiceProvider
import com.ist.instocktracker.services.db.FirestoreProvider.db
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Handler for POST /api/v1/link-item/{id}/check endpoint
 * Checks the status of a link item by scraping the page and evaluating with AI
 */
fun Route.postCheck() {
    post("{id}/check") {
        try {
            val linkItemRepository = ServiceProvider.linkItemRepository
            // Get the link item ID from the path parameters
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing or invalid ID")
            )

            val docRef = linkItemRepository.getReference(id)
            val linkItem = linkItemRepository.get(id) ?: return@post call.respond(
                HttpStatusCode.NotFound,
                "Link item was not found"
            )


            println("Checking link item: $linkItem")

            // Get the HTML content of the page using Bright Data Browser API
            val (html, image, imageBytes) = scrapePageWithBrightData(linkItem.link)

            // Evaluate the HTML with GenAI
            val aiResult = evaluateWithAI(imageBytes, linkItem)

            // Send push notification
            if (aiResult && linkItem.lastCheckResult != null && linkItem.lastCheckResult == false) {
                val user = ServiceProvider.userRepository.get(linkItem.userId)
                    ?: return@post call.respond(HttpStatusCode.InternalServerError, "Item does not have user id")
                val payload = PushPayload(
                    title = "Availability matched: ${linkItem.mode.name}",
                    body = (linkItem.label ?: linkItem.link),
                    linkItemId = id
                )
                ServiceProvider.notificationService.sendToUser(user, payload)
            }


            // Get current timestamp
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

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
            withContext(Dispatchers.IO) {
                batch.commit().get()
            }

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
                mapOf("error" to "Failed to check link item: ${e.message}")
            )
        }
    }
}

/**
 * Scrapes a web page using Bright Data Browser API
 */
data class ScrapePageResponse(val html: String?, val image: String?, val imageBytes: ByteArray)

suspend fun scrapePageWithBrightData(url: String, saveScreenshot: Boolean = false): ScrapePageResponse {
    return withContext(Dispatchers.IO) {
        try {
            // Configure Chrome options for Bright Data
            val options = ChromeOptions()

            // Connect to Bright Data Browser API
            val webDriverUrl =
                URI.create("https://brd-customer-hl_b2c0135c-zone-scraping_browser1:tn4mq8z2kbxz@brd.superproxy.io:9515")
                    .toURL()
            val driver = RemoteWebDriver(webDriverUrl, options)

            try {
                // Navigate to the URL
                driver.get(url)

                // Wait for the page to load (you might need to adjust this)
                Thread.sleep(500)

                var screenshotBytes: ByteArray
                var imagePath: String? = null

                if (saveScreenshot) {
                    //do a screenshot
                    val screenshotFile = driver.getScreenshotAs(OutputType.FILE)
                    val destinationPath =
                        Paths.get("./server/resources/screenshots/screenshot-${System.currentTimeMillis()}.png")
                    // Create parent directories if they don't exist
                    Files.createDirectories(destinationPath.parent)
                    // Move file into /resources/screenshots
                    Files.move(screenshotFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING)
                    println("Original path of a screenshot: ${screenshotFile.toPath()}")
                    println("Screenshot saved to: $destinationPath")

                    imagePath = destinationPath.toString()
                    screenshotBytes = screenshotFile.readBytes()
                } else {
                    screenshotBytes = driver.getScreenshotAs(OutputType.BYTES)
                }


                ScrapePageResponse(html = null, image = imagePath, imageBytes = screenshotBytes)
            } finally {
                // Make sure to close the driver
                driver.quit()
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to scrape page: ${e.message}", e)
        }
    }
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
                I'll provide you with the HTML of a product page, and I need you to determine if the product is in the following state: ${linkItem.mode.name}.
                
                Here are the possible states:
                - IN_STOCK: The product is available for immediate purchase
                - PRE_ORDER: The product is available for pre-order but not yet released
                - OUT_OF_STOCK: The product is currently unavailable for purchase
                
                Please analyze the following attached image and identify if an item that is being sold in this page is in the state ${linkItem.mode.name}. Respond with ONLY "true" if the product availability match ${linkItem.mode.name}  or "false" if it doesn't match.
            """.trimIndent()

            //val imageBytes = Files.readAllBytes(Paths.get(imagePath))
            val contents =
                Content.builder().parts(Part.fromBytes(imageBytes, "image/png"), Part.fromText(prompt)).build()
            val response: GenerateContentResponse = ServiceProvider.genAi.geminiClient.models.generateContent(
                "gemini-2.5-flash",
                contents, null
            )
            val responseText = response.text()

            println("GenAI response: $responseText")
            responseText == "true"
        } catch (e: Exception) {
            throw RuntimeException("Failed to evaluate with AI: ${e.message}", e)
        }
    }
}