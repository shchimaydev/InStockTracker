package com.ist.instocktracker.apiHandlers.linkItem.check

import com.google.genai.types.GenerateContentResponse
import com.ist.instocktracker.data.CheckResponse
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.LinkItemLog
import com.ist.instocktracker.data.toLinkItem
import com.ist.instocktracker.services.GenAI
import com.ist.instocktracker.services.db.FirestoreProvider.db
import com.ist.instocktracker.services.db.FirestoreProvider.linksCollection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URI
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
            // Get the link item ID from the path parameters
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing or invalid ID")
            )

            // Fetch the link item from Firestore
            val docRef = db.collection(linksCollection).document(id)
            val snapshot = withContext(Dispatchers.IO) {
                docRef.get().get()
            }

            // Convert the snapshot to a LinkItem
            val linkItem = snapshot.toLinkItem()
            if (linkItem == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Link item not found")
                )
                return@post
            }

            println("Checking link item: $linkItem")

            // Get the HTML content of the page using Bright Data Browser API
            val html = scrapePageWithBrightData(linkItem.link)

            // Evaluate the HTML with GenAI
            val aiResult = evaluateWithAI(html, linkItem)

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
suspend fun scrapePageWithBrightData(url: String): String {
    return withContext(Dispatchers.IO) {
        try {
            // Configure Chrome options for Bright Data
            val options = ChromeOptions()
            
            // Connect to Bright Data Browser API
            val webDriverUrl = URI.create("https://brd-customer-hl_b2c0135c-zone-scraping_browser1:tn4mq8z2kbxz@brd.superproxy.io:9515").toURL()
            val driver = RemoteWebDriver(webDriverUrl, options)
            
            try {
                // Navigate to the URL
                driver.get(url)
                
                // Wait for the page to load (you might need to adjust this)
                Thread.sleep(500)
                
                // Get the page source
                val pageSource = driver.getPageSource()
                
                pageSource
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
suspend fun evaluateWithAI(html: String, linkItem: LinkItem): Boolean {
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
                
                Please analyze the following HTML and respond with ONLY "true" if the product is in the state ${linkItem.mode.name}, or "false" if it's in any other state.
                
                HTML:
                ${html.take(15000)} // Limit HTML size to avoid token limits
            """.trimIndent()

            val response: GenerateContentResponse = GenAI.geminiClient.models.generateContent("gemini-2.5-flash", prompt, null)
            val responseText = response.text()

            println("GenAI response: $responseText")
            responseText == "true"
        } catch (e: Exception) {
            throw RuntimeException("Failed to evaluate with AI: ${e.message}", e)
        }
    }
}