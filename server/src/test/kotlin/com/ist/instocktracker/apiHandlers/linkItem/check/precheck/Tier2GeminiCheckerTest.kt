package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class Tier2GeminiCheckerTest {

    private val linkItem = LinkItem(userId = "user-1", link = "https://example.com/product", mode = Mode.IN_STOCK)
    private val screenshotBytes = byteArrayOf(9, 9, 9)

    @Test
    fun `delegates to the injected AiEvaluator and returns its result`() = runBlocking {
        var receivedBytes: ByteArray? = null
        var receivedItem: LinkItem? = null
        val checker = Tier2GeminiChecker(
            AiEvaluator { bytes, item ->
                receivedBytes = bytes
                receivedItem = item
                true
            }
        )

        val result = checker.check(screenshotBytes, linkItem)

        assertEquals(true, result)
        assertEquals(linkItem, receivedItem)
        assertEquals(screenshotBytes, receivedBytes)
    }

    @Test
    fun `passes through a false result unchanged`() = runBlocking {
        val checker = Tier2GeminiChecker(AiEvaluator { _, _ -> false })

        val result = checker.check(screenshotBytes, linkItem)

        assertEquals(false, result)
    }
}
