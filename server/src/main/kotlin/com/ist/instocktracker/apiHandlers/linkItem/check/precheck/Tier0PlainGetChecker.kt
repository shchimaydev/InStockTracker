package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import org.jsoup.Jsoup

fun interface Tier0Checker {
    suspend fun check(linkItem: LinkItem): Tier0Outcome
}

/**
 * Tier 0: a plain, browser-less HTTP GET. Tries structured data first (works even on pages whose
 * body is otherwise a JS shell, since many sites still server-render `<head>` metadata for SEO),
 * then a conservative text heuristic — but only on pages that don't look like a JS shell, where
 * text heuristics are unreliable. Never throws: any failure is reported as inconclusive so the
 * caller can escalate to Tier 1.
 */
class Tier0PlainGetChecker(
    private val httpClient: HttpClient = ServiceProvider.httpClient
) : Tier0Checker {

    override suspend fun check(linkItem: LinkItem): Tier0Outcome {
        val document = try {
            val response = httpClient.get(linkItem.link) {
                header(HttpHeaders.UserAgent, USER_AGENT)
                timeout { requestTimeoutMillis = FETCH_TIMEOUT_MILLIS }
            }
            if (!response.status.isSuccess()) {
                return Tier0Outcome.Inconclusive("non_success_status_${response.status.value}")
            }
            Jsoup.parse(response.body<String>(), linkItem.link)
        } catch (e: Exception) {
            return Tier0Outcome.Inconclusive("fetch_failed: ${e.message}")
        }

        if (AvailabilityExtractor.looksLikeBlockedPage(document, statusCode = 200)) {
            return Tier0Outcome.Inconclusive("blocked_page")
        }

        val structured = AvailabilityExtractor.extractStructured(document)
        if (structured != null) {
            return Tier0Outcome.Conclusive(structured.mode == linkItem.mode, structured.source)
        }

        if (AvailabilityExtractor.looksLikeJsShell(document)) {
            return Tier0Outcome.Inconclusive("js_shell")
        }

        val fromText = AvailabilityExtractor.extractFromText(document)
            ?: return Tier0Outcome.Inconclusive("no_signal_found")

        return Tier0Outcome.Conclusive(fromText.mode == linkItem.mode, fromText.source)
    }

    companion object {
        private const val FETCH_TIMEOUT_MILLIS = 6_000L
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Safari/537.36"
    }
}
