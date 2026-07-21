package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.google.genai.types.Content
import com.google.genai.types.Part
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.services.GenAI
import com.ist.instocktracker.services.ServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Raw wrapper around the Gemini vision call, kept separate from tier orchestration semantics. */
fun interface AiEvaluator {
    suspend fun evaluate(screenshotBytes: ByteArray, linkItem: LinkItem): Boolean
}

class GeminiAiEvaluator(
    private val genAi: GenAI = ServiceProvider.genAi
) : AiEvaluator {

    override suspend fun evaluate(screenshotBytes: ByteArray, linkItem: LinkItem): Boolean =
        withContext(Dispatchers.IO) {
            val prompt = """
                You are an expert at analyzing e-commerce product pages.
                I'll provide you with the screenshot of a product page, and I need you to determine if the product is in the following state: ${linkItem.mode.name}.

                Here are the possible states:
                - IN_STOCK: The product is available for immediate purchase
                - PRE_ORDER: The product is available for pre-order but not yet released
                - OUT_OF_STOCK: The product is currently unavailable for purchase

                Please analyze the attached image and identify if an item that is being sold in this page is in the state ${linkItem.mode.name}. Respond with ONLY "true" if the product availability match ${linkItem.mode.name}  or "false" if it doesn't match.
            """.trimIndent()

            val contents = Content.builder()
                .parts(Part.fromBytes(screenshotBytes, "image/png"), Part.fromText(prompt))
                .build()

            val response = genAi.geminiClient.models.generateContent("gemini-2.5-flash-lite", contents, null)
            response.text() == "true"
        }
}

/** Tier 2: today's exact pipeline. Always the floor — never escalates further. */
fun interface Tier2Checker {
    suspend fun check(screenshotBytes: ByteArray, linkItem: LinkItem): Boolean
}

class Tier2GeminiChecker(
    private val aiEvaluator: AiEvaluator = GeminiAiEvaluator()
) : Tier2Checker {

    override suspend fun check(screenshotBytes: ByteArray, linkItem: LinkItem): Boolean =
        aiEvaluator.evaluate(screenshotBytes, linkItem)
}
