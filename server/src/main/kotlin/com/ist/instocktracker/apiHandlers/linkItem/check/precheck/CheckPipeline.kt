package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.LinkItem

/**
 * Orchestrates the three check tiers, stopping at the first conclusive one. Every item runs the
 * same Tier 0 -> Tier 1 -> Tier 2 sequence; Tier 2 is today's exact pipeline, so the worst case is
 * identical to current production behavior — there's nothing to opt out of.
 */
class CheckPipeline(
    private val tier0: Tier0Checker = Tier0PlainGetChecker(),
    private val tier1: Tier1Checker = Tier1RenderedContentChecker(),
    private val tier2: Tier2Checker = Tier2GeminiChecker()
) {
    suspend fun run(linkItem: LinkItem): CheckPipelineResult {
        when (val outcome = tier0.check(linkItem)) {
            is Tier0Outcome.Conclusive ->
                return CheckPipelineResult(outcome.result, ResolvedVia.PRECHECK_TIER0, outcome.source)

            is Tier0Outcome.Inconclusive -> Unit
        }

        return when (val outcome = tier1.check(linkItem)) {
            is Tier1Outcome.Conclusive ->
                CheckPipelineResult(outcome.result, ResolvedVia.PRECHECK_TIER1, outcome.source)

            is Tier1Outcome.Inconclusive -> {
                val result = tier2.check(outcome.screenshotBytes, linkItem)
                CheckPipelineResult(result, ResolvedVia.FULL_PIPELINE)
            }
        }
    }
}
