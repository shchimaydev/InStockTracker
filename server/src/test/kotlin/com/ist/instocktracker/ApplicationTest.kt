package com.ist.instocktracker

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.development" to "false"
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