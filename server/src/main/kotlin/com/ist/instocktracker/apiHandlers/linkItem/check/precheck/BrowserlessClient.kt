package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.services.ServiceProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import java.util.Base64

data class RenderedPage(val html: String, val screenshotBytes: ByteArray)

/**
 * Wraps Browserless's `/function` endpoint: a single billed browser session that navigates to a
 * page, lets its JavaScript execute, and returns both the fully-rendered HTML and a full-page
 * screenshot in one response — the same cost as today's screenshot-only call, but with the HTML
 * available for deterministic parsing before ever paying for a Gemini vision call.
 */
fun interface BrowserlessClient {
    suspend fun renderWithScreenshot(url: String): RenderedPage
}

class BrowserlessHttpClient(
    private val httpClient: HttpClient = ServiceProvider.httpClient,
    private val token: () -> String = { ServiceProvider.config.browserlessToken }
) : BrowserlessClient {

    override suspend fun renderWithScreenshot(url: String): RenderedPage {
        val response = httpClient.post("https://production-sfo.browserless.io/function") {
            parameter("token", token())
            contentType(ContentType.Application.Json)
            setBody(
                BrowserlessFunctionBody(
                    code = RENDER_WITH_SCREENSHOT_SCRIPT,
                    context = BrowserlessFunctionContext(url = url)
                )
            )
        }
        val payload: BrowserlessRenderPayload = response.body()
        val screenshotBytes = Base64.getDecoder().decode(payload.screenshotBase64)
        return RenderedPage(html = payload.html, screenshotBytes = screenshotBytes)
    }

    companion object {
        // language=JavaScript
        private const val RENDER_WITH_SCREENSHOT_SCRIPT = """
            export default async function ({ page, context }) {
              await page.goto(context.url, { waitUntil: 'networkidle2', timeout: 30000 });
              const html = await page.content();
              const screenshotBuffer = await page.screenshot({ fullPage: true, type: 'png' });
              return {
                data: { html, screenshotBase64: screenshotBuffer.toString('base64') },
                type: 'application/json'
              };
            }
        """
    }
}

@Serializable
private data class BrowserlessFunctionContext(val url: String)

@Serializable
private data class BrowserlessFunctionBody(val code: String, val context: BrowserlessFunctionContext)

@Serializable
private data class BrowserlessRenderPayload(val html: String, val screenshotBase64: String)
