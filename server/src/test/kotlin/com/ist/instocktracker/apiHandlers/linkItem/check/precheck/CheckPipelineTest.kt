package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckPipelineTest {

    private val linkItem = LinkItem(userId = "user-1", link = "https://example.com/product", mode = Mode.IN_STOCK)
    private val screenshotBytes = byteArrayOf(7, 7, 7)

    private val neverCalledTier1 = Tier1Checker { error("Tier 1 should not have been called") }
    private val neverCalledTier2 = Tier2Checker { _, _ -> error("Tier 2 should not have been called") }

    @Test
    fun `stops at Tier 0 when it is conclusive`() = runBlocking {
        val pipeline = CheckPipeline(
            tier0 = Tier0Checker { Tier0Outcome.Conclusive(true, PreCheckSource.JSON_LD) },
            tier1 = neverCalledTier1,
            tier2 = neverCalledTier2
        )

        val result = pipeline.run(linkItem)

        assertEquals(CheckPipelineResult(true, ResolvedVia.PRECHECK_TIER0, PreCheckSource.JSON_LD), result)
    }

    @Test
    fun `escalates to Tier 1 when Tier 0 is inconclusive, and stops there if conclusive`() = runBlocking {
        val pipeline = CheckPipeline(
            tier0 = Tier0Checker { Tier0Outcome.Inconclusive("js_shell") },
            tier1 = Tier1Checker { Tier1Outcome.Conclusive(false, PreCheckSource.OPEN_GRAPH) },
            tier2 = neverCalledTier2
        )

        val result = pipeline.run(linkItem)

        assertEquals(CheckPipelineResult(false, ResolvedVia.PRECHECK_TIER1, PreCheckSource.OPEN_GRAPH), result)
    }

    @Test
    fun `falls through to Tier 2 using Tier 1's already-fetched screenshot when both tiers are inconclusive`() =
        runBlocking {
            var tier2ReceivedBytes: ByteArray? = null
            val pipeline = CheckPipeline(
                tier0 = Tier0Checker { Tier0Outcome.Inconclusive("no_signal_found") },
                tier1 = Tier1Checker { Tier1Outcome.Inconclusive("no_signal_found", screenshotBytes) },
                tier2 = Tier2Checker { bytes, _ ->
                    tier2ReceivedBytes = bytes
                    true
                }
            )

            val result = pipeline.run(linkItem)

            assertEquals(CheckPipelineResult(true, ResolvedVia.FULL_PIPELINE, null), result)
            assertTrue(tier2ReceivedBytes.contentEquals(screenshotBytes))
        }
}
