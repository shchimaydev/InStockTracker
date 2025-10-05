package com.ist.instocktracker

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import com.ist.instocktracker.module
import com.ist.instocktracker.services.db.FirestoreProvider
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After

class ApplicationTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testRoot() = testApplication {
        // Mock FirestoreProvider to prevent actual connection attempts
        mockkObject(FirestoreProvider)
        every { FirestoreProvider.checkConnection() } returns Unit
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
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Server is running", response.bodyAsText())
    }
}