package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.Mode

/** Which parsing strategy produced an [ExtractedAvailability]. */
enum class PreCheckSource {
    JSON_LD,
    MICRODATA,
    OPEN_GRAPH,
    TEXT_HEURISTIC
}

/** Which stage of the check pipeline actually produced the persisted result. */
enum class ResolvedVia {
    PRECHECK_TIER0,
    PRECHECK_TIER1,
    FULL_PIPELINE
}

data class ExtractedAvailability(
    val mode: Mode,
    val source: PreCheckSource
)

/** Outcome of Tier 0 (plain GET, no browser). */
sealed class Tier0Outcome {
    data class Conclusive(val result: Boolean, val source: PreCheckSource) : Tier0Outcome()
    data class Inconclusive(val reason: String) : Tier0Outcome()
}

/** Outcome of Tier 1 (Browserless `/function`: rendered HTML + screenshot). */
sealed class Tier1Outcome {
    data class Conclusive(val result: Boolean, val source: PreCheckSource) : Tier1Outcome()

    /** Carries the screenshot already fetched in this session, so Tier 2 never re-fetches. */
    data class Inconclusive(val reason: String, val screenshotBytes: ByteArray) : Tier1Outcome()
}

data class CheckPipelineResult(
    val result: Boolean,
    val resolvedVia: ResolvedVia,
    val preCheckSource: PreCheckSource? = null
)
