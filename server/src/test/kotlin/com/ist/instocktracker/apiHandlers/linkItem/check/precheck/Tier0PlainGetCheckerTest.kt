package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Tier0PlainGetCheckerTest {

    private val linkItem = LinkItem(userId = "user-1", link = "https://example.com/product", mode = Mode.IN_STOCK)

    private fun clientReturning(html: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val engine = MockEngine { _ ->
            respond(
                content = html,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString())
            )
        }
        return HttpClient(engine) { install(HttpTimeout) }
    }

    @Test
    fun `resolves conclusively when JSON-LD matches the desired mode`() = runBlocking {
        val html = """
            <html><body>
            <script type="application/ld+json">
            {"@type":"Product","offers":{"availability":"https://schema.org/InStock"}}
            </script>
            </body></html>
        """.trimIndent()
        val checker = Tier0PlainGetChecker(clientReturning(html))

        val outcome = checker.check(linkItem)

        assertEquals(Tier0Outcome.Conclusive(true, PreCheckSource.JSON_LD), outcome)
    }

    @Test
    fun `resolves conclusively with a false result when structured data mismatches the mode`() = runBlocking {
        val html = """
            <html><body>
            <script type="application/ld+json">
            {"@type":"Product","offers":{"availability":"https://schema.org/OutOfStock"}}
            </script>
            </body></html>
        """.trimIndent()
        val checker = Tier0PlainGetChecker(clientReturning(html))

        val outcome = checker.check(linkItem)

        assertEquals(Tier0Outcome.Conclusive(false, PreCheckSource.JSON_LD), outcome)
    }

    @Test
    fun `escalates on network failure`() = runBlocking {
        val engine = MockEngine { throw java.io.IOException("connection refused") }
        val checker = Tier0PlainGetChecker(HttpClient(engine) { install(HttpTimeout) })

        val outcome = checker.check(linkItem)

        assertTrue(outcome is Tier0Outcome.Inconclusive)
    }

    @Test
    fun `escalates on a non-success status code`() = runBlocking {
        val engine = MockEngine { respondError(HttpStatusCode.Forbidden) }
        val checker = Tier0PlainGetChecker(HttpClient(engine) { install(HttpTimeout) })

        val outcome = checker.check(linkItem)

        assertTrue(outcome is Tier0Outcome.Inconclusive)
    }

    @Test
    fun `escalates on a bot-challenge page`() = runBlocking {
        val html = "<html><body><p>Just a moment... checking your browser.</p></body></html>"
        val checker = Tier0PlainGetChecker(clientReturning(html))

        val outcome = checker.check(linkItem)

        assertEquals(Tier0Outcome.Inconclusive("blocked_page"), outcome)
    }

    @Test
    fun `escalates on a JS shell page with no structured data`() = runBlocking {
        val html = """<html><body><div id="root"></div></body></html>"""
        val checker = Tier0PlainGetChecker(clientReturning(html))

        val outcome = checker.check(linkItem)

        assertEquals(Tier0Outcome.Inconclusive("js_shell"), outcome)
    }

    @Test
    fun `escalates when no structured or text signal is found`() = runBlocking {
        val html = "<html><body><p>" + "Welcome to our store. ".repeat(20) + "</p></body></html>"
        val checker = Tier0PlainGetChecker(clientReturning(html))

        val outcome = checker.check(linkItem)

        assertEquals(Tier0Outcome.Inconclusive("no_signal_found"), outcome)
    }

    @Test
    fun `resolves conclusively via the text heuristic when no structured data is present`() = runBlocking {
        val html = "<html><body><p>" + "Add to cart today! ".repeat(20) + "</p></body></html>"
        val checker = Tier0PlainGetChecker(clientReturning(html))

        val outcome = checker.check(linkItem)

        assertEquals(Tier0Outcome.Conclusive(true, PreCheckSource.TEXT_HEURISTIC), outcome)
    }
}
