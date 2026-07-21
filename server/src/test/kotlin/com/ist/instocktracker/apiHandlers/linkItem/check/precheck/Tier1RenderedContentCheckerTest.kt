package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class Tier1RenderedContentCheckerTest {

    private val linkItem = LinkItem(userId = "user-1", link = "https://example.com/product", mode = Mode.IN_STOCK)
    private val fakeScreenshotBytes = byteArrayOf(1, 2, 3)

    @Test
    fun `resolves conclusively when rendered HTML has structured data`() = runBlocking {
        val html = """
            <html><body>
            <script type="application/ld+json">
            {"@type":"Product","offers":{"availability":"https://schema.org/InStock"}}
            </script>
            </body></html>
        """.trimIndent()
        val checker = Tier1RenderedContentChecker(
            BrowserlessClient { RenderedPage(html, fakeScreenshotBytes) }
        )

        val outcome = checker.check(linkItem)

        assertEquals(Tier1Outcome.Conclusive(true, PreCheckSource.JSON_LD), outcome)
    }

    @Test
    fun `resolves conclusively via text heuristic when no structured data is present`() = runBlocking {
        val html = "<html><body><p>" + "Add to cart today! ".repeat(20) + "</p></body></html>"
        val checker = Tier1RenderedContentChecker(
            BrowserlessClient { RenderedPage(html, fakeScreenshotBytes) }
        )

        val outcome = checker.check(linkItem)

        assertEquals(Tier1Outcome.Conclusive(true, PreCheckSource.TEXT_HEURISTIC), outcome)
    }

    @Test
    fun `is inconclusive and carries the screenshot bytes forward when no signal is found`() = runBlocking {
        val html = "<html><body><p>" + "Welcome to our store. ".repeat(20) + "</p></body></html>"
        val checker = Tier1RenderedContentChecker(
            BrowserlessClient { RenderedPage(html, fakeScreenshotBytes) }
        )

        val outcome = checker.check(linkItem)

        val inconclusive = outcome as? Tier1Outcome.Inconclusive ?: fail("expected Inconclusive, got $outcome")
        assertTrue(inconclusive.screenshotBytes.contentEquals(fakeScreenshotBytes))
    }

    @Test
    fun `is inconclusive on a bot-challenge page`() = runBlocking {
        val html = "<html><body><p>Just a moment... verifying you are human, please wait a bit longer here.</p></body></html>"
        val checker = Tier1RenderedContentChecker(
            BrowserlessClient { RenderedPage(html, fakeScreenshotBytes) }
        )

        val outcome = checker.check(linkItem)

        val inconclusive = outcome as? Tier1Outcome.Inconclusive ?: fail("expected Inconclusive, got $outcome")
        assertEquals("blocked_page", inconclusive.reason)
    }

    @Test(expected = IllegalStateException::class)
    fun `propagates a Browserless fetch failure since there is no screenshot to fall back on`(): Unit = runBlocking {
        val checker = Tier1RenderedContentChecker(
            BrowserlessClient { throw IllegalStateException("browserless unavailable") }
        )

        checker.check(linkItem)
        Unit
    }
}
