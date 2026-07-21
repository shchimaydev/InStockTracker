package com.ist.instocktracker.apiHandlers.linkItem.check

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.*
import com.ist.instocktracker.apiHandlers.linkItem.check.precheck.CheckPipeline
import com.ist.instocktracker.apiHandlers.linkItem.check.precheck.CheckPipelineResult
import com.ist.instocktracker.apiHandlers.linkItem.check.precheck.PreCheckSource
import com.ist.instocktracker.apiHandlers.linkItem.check.precheck.ResolvedVia
import com.ist.instocktracker.data.CheckResponse
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.module
import com.ist.instocktracker.services.ServiceProvider
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.json.Json
import org.junit.After
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
                "app.gemini.apiKey" to "test-api-key",
                "storage.location" to "europe-west3"
            )
        }
        application { module() }
        // Overrides the real CheckPipeline that module() -> ServiceProvider.init() just built,
        // so this test never touches Browserless/Gemini/HTTP.
        val fakePipeline = mockk<CheckPipeline>()
        coEvery { fakePipeline.run(any()) } returns
            CheckPipelineResult(result = true, resolvedVia = ResolvedVia.PRECHECK_TIER0, preCheckSource = PreCheckSource.JSON_LD)
        application { ServiceProvider.checkPipeline = fakePipeline }

        // --- 1. Define Test Data ---
        val testId = "test-item-123"
        val fakeLinkItem = LinkItem(
            id = testId,
            userId = "test-user",
            link = "https://example.com/product/123",
            mode = Mode.IN_STOCK,
        )

        // --- 2. Mock the FirestoreProvider singleton and the chain of Firestore calls ---
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

        // Configure the mock snapshot to return our fake data. `toLinkItem()` maps each field
        // individually (getString/getBoolean/getLong), it does not use toObject().
        every { mockSnapshot.exists() } returns true
        every { mockSnapshot.id } returns testId
        every { mockSnapshot.getString("userId") } returns fakeLinkItem.userId
        every { mockSnapshot.getString("label") } returns fakeLinkItem.label
        every { mockSnapshot.getString("link") } returns fakeLinkItem.link
        every { mockSnapshot.getString("startAt") } returns fakeLinkItem.startAt
        every { mockSnapshot.getString("mode") } returns fakeLinkItem.mode.name
        every { mockSnapshot.getString("additionalInstructions") } returns fakeLinkItem.additionalInstructions
        every { mockSnapshot.getBoolean("isActive") } returns fakeLinkItem.isActive
        every { mockSnapshot.get("interval") } returns null
        every { mockSnapshot.getString("scheduleJobId") } returns fakeLinkItem.scheduleJobId
        every { mockSnapshot.getBoolean("lastCheckResult") } returns fakeLinkItem.lastCheckResult
        every { mockSnapshot.getString("lastCheckDate") } returns fakeLinkItem.lastCheckDate
        every { mockSnapshot.getString("placeholderImage") } returns fakeLinkItem.placeholderImage
        every { mockSnapshot.getLong("updatedAt") } returns fakeLinkItem.updatedAt
        every { mockSnapshot.getBoolean("isFrozen") } returns fakeLinkItem.isFrozen

        // Mock the write operations to prevent real DB calls. Persistence goes through a
        // WriteBatch (db.batch().update(...)/.set(...).commit()), not direct docRef writes.
        every { mockDocRef.collection("logs") } returns mockCollectionRef
        every { mockCollectionRef.document(any<String>()) } returns mockDocRef

        val mockBatch = mockk<WriteBatch>(relaxed = true)
        val mockCommitFuture = mockk<ApiFuture<List<WriteResult>>>(relaxed = true)
        every { mockFirestore.batch() } returns mockBatch
        every { mockBatch.commit() } returns mockCommitFuture

        // --- 3. Execute the Request ---
        val response = client.post("/api/v1/link-items/$testId/check")

        // --- 4. Assert the Results ---
        assertEquals(HttpStatusCode.OK, response.status)
        val checkResponse = Json.decodeFromString<CheckResponse>(response.bodyAsText())
        assertEquals("success", checkResponse.status)
        assertEquals(true, checkResponse.result)
        assertEquals(testId, checkResponse.linkItemId)

        // The pipeline result (and its resolvedVia/preCheckSource) must reach Firestore.
        verify {
            mockBatch.set(
                mockDocRef,
                match<Map<String, Any?>> {
                    it["resolvedVia"] == "PRECHECK_TIER0" && it["preCheckSource"] == "JSON_LD"
                }
            )
        }
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
                "app.gemini.apiKey" to "test-api-key",
                "storage.location" to "europe-west3"
            )
        }
        // Configure the application
        application {
            module()
        }

        // Test with an invalid ID
        val response = client.post("/api/v1/link-items/invalid-id/check")

        // Since we can't mock the Firestore database, this will likely return an error
        // The exact status code will depend on how the application handles non-existent documents
        // It could be NotFound (404) or InternalServerError (500)
        // We'll just check that it's not OK (200)
        assertTrue(response.status != HttpStatusCode.OK)
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
                "app.gemini.apiKey" to "test-api-key",
                "storage.location" to "europe-west3"
            )
        }
        // Configure the application
        application {
            module()
        }

        // Test with a missing ID
        val response = client.post("/api/v1/link-items//check")

        // This should return a BadRequest (400) status
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // Note: We can't effectively test the successful case without mocking
    // the Firestore database and ServiceProvider.checkPipeline.
    // A more comprehensive test would require setting up proper mocks
    // for these dependencies.
}
