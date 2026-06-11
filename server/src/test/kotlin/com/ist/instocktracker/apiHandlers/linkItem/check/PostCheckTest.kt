package com.ist.instocktracker.apiHandlers.linkItem.check

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.*
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.module
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.After
import kotlin.test.Test
import kotlin.test.assertEquals


class PostCheckTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testCheckEndpointSuccess() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.development" to "false",
                "app.jwt.secret" to "test-secret-key",
                "app.jwt.issuer" to "test-issuer",
                "app.jwt.audience" to "test-audience",
                "app.jwt.accessTokenTtlSec" to "3600",
                "app.jwt.refreshTokenTtlSec" to "86400",
                "app.gemini.apiKey" to "test-api-key"
            )
        }
        application { module() }

        // --- 1. Define Test Data ---
        val testId = "test-item-123"
        val fakeLinkItem = LinkItem(
            id = testId,
            userId = "test-user",
            link = "https://example.com/product/123",
            mode = Mode.IN_STOCK,
        )
        val fakeHtmlContent = "<html><body><h1>In Stock!</h1></body></html>"
        val fakeAiResult = "The item is in stock."

        // --- 2. Mock External Dependencies ---

        // Mock static functions for scraping and AI.
        mockkStatic("com.ist.instocktracker.apiHandlers.linkItem.check.PostCheckKt")

        // Create a mock ScrapePageResponse with the fake HTML content
        val fakeImageBytes = ByteArray(0) // Empty byte array for testing
        val fakeScrapeResponse = ScrapePageResponse(
            imagePath = null,
            imageBytes = fakeImageBytes
        )

        // Mock the scrapePage function to return the fake response
        coEvery { scrapePage(any(), any()) } returns fakeScrapeResponse

        // Mock the evaluateWithAI function to return true
        coEvery { evaluateWithAI(any(), any()) } returns true

        // Mock the FirestoreProvider singleton and the chain of Firestore calls
        mockkObject(FirestoreProvider)
        val mockFirestore = mockk<Firestore>()
        val mockCollectionRef = mockk<CollectionReference>()
        val mockDocRef = mockk<DocumentReference>()
        val mockSnapshotFuture = mockk<ApiFuture<DocumentSnapshot>>()
        val mockSnapshot = mockk<DocumentSnapshot>()

        // Stub the behavior for reading the document
        every { FirestoreProvider.db } returns mockFirestore
        every { mockFirestore.collection(any()) } returns mockCollectionRef
        every { mockCollectionRef.document(testId) } returns mockDocRef
        every { mockDocRef.get() } returns mockSnapshotFuture
        every { mockSnapshotFuture.get() } returns mockSnapshot

        // Configure the mock snapshot to return our fake data.
        // This allows your real `toLinkItem()` extension function to work on the mock.
        every { mockSnapshot.exists() } returns true
        every { mockSnapshot.toObject(LinkItem::class.java) } returns fakeLinkItem // Assumes toLinkItem uses toObject

        // Mock the write operations (update and log) to prevent real DB calls
        val mockWriteFuture = mockk<ApiFuture<WriteResult>>(relaxed = true)
        every { mockDocRef.update(any<Map<String, Any>>()) } returns mockWriteFuture
        every { mockDocRef.collection("logs") } returns mockCollectionRef
        every { mockCollectionRef.document(any<String>()) } returns mockDocRef
        every { mockDocRef.set(any()) } returns mockWriteFuture

        // --- 3. Execute the Request ---
        val response = client.post("/api/v1/link-item/$testId/check")

        // --- 4. Assert the Results ---
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assert(responseBody.contains(fakeAiResult))
        assert(responseBody.contains("\"status\":\"success\""))
    }


    @Test
    fun testCheckEndpointWithInvalidId() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.development" to "false",
                "app.jwt.secret" to "test-secret-key",
                "app.jwt.issuer" to "test-issuer",
                "app.jwt.audience" to "test-audience",
                "app.jwt.accessTokenTtlSec" to "3600",
                "app.jwt.refreshTokenTtlSec" to "86400",
                "app.gemini.apiKey" to "test-api-key"
            )
        }
        // Configure the application
        application {
            module()
        }

        // Test with an invalid ID
        val response = client.post("/api/v1/link-item/invalid-id/check")

        // Since we can't mock the Firestore database, this will likely return an error
        // The exact status code will depend on how the application handles non-existent documents
        // It could be NotFound (404) or InternalServerError (500)
        // We'll just check that it's not OK (200)
        assert(response.status != HttpStatusCode.OK)
    }

    @Test
    fun testCheckEndpointWithMissingId() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.development" to "false",
                "app.jwt.secret" to "test-secret-key",
                "app.jwt.issuer" to "test-issuer",
                "app.jwt.audience" to "test-audience",
                "app.jwt.accessTokenTtlSec" to "3600",
                "app.jwt.refreshTokenTtlSec" to "86400",
                "app.gemini.apiKey" to "test-api-key"
            )
        }
        // Configure the application
        application {
            module()
        }

        // Test with a missing ID
        val response = client.post("/api/v1/link-item//check")

        // This should return a BadRequest (400) status
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // Note: We can't effectively test the successful case without mocking
    // the Firestore database, Bright Data scraping, and GenAI client.
    // A more comprehensive test would require setting up proper mocks
    // for these dependencies.
}