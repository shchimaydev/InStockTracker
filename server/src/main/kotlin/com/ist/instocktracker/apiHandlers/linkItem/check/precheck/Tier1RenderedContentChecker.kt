package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.LinkItem
import org.jsoup.Jsoup

fun interface Tier1Checker {
    suspend fun check(linkItem: LinkItem): Tier1Outcome
}

/**
 * Tier 1: Browserless `/function` renders the page's JavaScript, so the same extraction chain as
 * Tier 0 can now succeed on SPA shells that defeated a plain GET. No JS-shell check is needed here
 * since the content is already fully rendered.
 *
 * If the Browserless call itself fails, there's no screenshot to fall back on for Tier 2, so that
 * failure is left to propagate — identical to how today's pipeline fails when Browserless errors.
 * Anything after a successful fetch (HTML parsing, extraction) is caught internally and downgraded
 * to inconclusive, carrying the already-fetched screenshot bytes forward regardless.
 */
class Tier1RenderedContentChecker(
    private val browserlessClient: BrowserlessClient = BrowserlessHttpClient()
) : Tier1Checker {

    override suspend fun check(linkItem: LinkItem): Tier1Outcome {
        val rendered = browserlessClient.renderWithScreenshot(linkItem.link)

        return try {
            val document = Jsoup.parse(rendered.html, linkItem.link)

            if (AvailabilityExtractor.looksLikeBlockedPage(document, statusCode = 200)) {
                return Tier1Outcome.Inconclusive("blocked_page", rendered.screenshotBytes)
            }

            val extracted = AvailabilityExtractor.extractStructured(document)
                ?: AvailabilityExtractor.extractFromText(document)

            if (extracted != null) {
                Tier1Outcome.Conclusive(extracted.mode == linkItem.mode, extracted.source)
            } else {
                Tier1Outcome.Inconclusive("no_signal_found", rendered.screenshotBytes)
            }
        } catch (e: Exception) {
            Tier1Outcome.Inconclusive("parse_failed: ${e.message}", rendered.screenshotBytes)
        }
    }
}
