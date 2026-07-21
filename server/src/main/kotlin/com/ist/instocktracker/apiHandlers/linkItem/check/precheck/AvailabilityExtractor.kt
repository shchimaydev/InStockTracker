package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.Mode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.jsoup.nodes.Document

/**
 * Pure, I/O-free parsing of stock availability out of an already-fetched HTML [Document].
 * Used identically by Tier 0 (plain-GET HTML) and Tier 1 (Browserless-rendered HTML).
 */
object AvailabilityExtractor {

    private const val MIN_BODY_TEXT_LENGTH = 200

    private val BLOCK_PAGE_MARKERS = listOf(
        "just a moment",
        "cf-browser-verification",
        "captcha",
        "access denied",
        "pardon our interruption",
        "are you a robot",
        "unusual traffic"
    )

    private val OUT_OF_STOCK_PHRASES = listOf(
        "sold out", "out of stock", "currently unavailable", "no longer available"
    )
    private val PRE_ORDER_PHRASES = listOf(
        "pre-order", "preorder", "available for pre-order"
    )
    private val IN_STOCK_PHRASES = listOf(
        "add to cart", "add to bag", "add to basket", "buy now"
    )

    /** Tries JSON-LD, then microdata, then OpenGraph, in that priority order. */
    fun extractStructured(document: Document): ExtractedAvailability? =
        extractFromJsonLd(document) ?: extractFromMicrodata(document) ?: extractFromOpenGraph(document)

    fun extractFromJsonLd(document: Document): ExtractedAvailability? {
        for (script in document.select("script[type=application/ld+json]")) {
            val raw = script.data().ifBlank { script.html() }
            if (raw.isBlank()) continue
            val json = runCatching { Json.parseToJsonElement(raw) }.getOrNull() ?: continue
            val token = findAvailabilityToken(json) ?: continue
            val mode = mapSchemaAvailabilityToken(token) ?: continue
            return ExtractedAvailability(mode, PreCheckSource.JSON_LD)
        }
        return null
    }

    fun extractFromMicrodata(document: Document): ExtractedAvailability? {
        val element = document.selectFirst("[itemprop=availability]") ?: return null
        val raw = element.attr("href").ifBlank { element.attr("content") }.ifBlank { element.text() }
        if (raw.isBlank()) return null
        val mode = mapSchemaAvailabilityToken(raw) ?: return null
        return ExtractedAvailability(mode, PreCheckSource.MICRODATA)
    }

    fun extractFromOpenGraph(document: Document): ExtractedAvailability? {
        val element = document.selectFirst(
            "meta[property=product:availability], meta[property=og:availability]"
        ) ?: return null
        val raw = element.attr("content")
        if (raw.isBlank()) return null
        val mode = mapOpenGraphAvailability(raw) ?: return null
        return ExtractedAvailability(mode, PreCheckSource.OPEN_GRAPH)
    }

    /**
     * Only trusted when exactly one availability signal appears with no conflicting phrase —
     * ambiguous or conflicting text returns null rather than guessing.
     */
    fun extractFromText(document: Document): ExtractedAvailability? {
        val text = document.body().text().lowercase()
        val hasOutOfStock = OUT_OF_STOCK_PHRASES.any { text.contains(it) }
        val hasPreOrder = PRE_ORDER_PHRASES.any { text.contains(it) }
        val hasInStock = IN_STOCK_PHRASES.any { text.contains(it) }
        val signalCount = listOf(hasOutOfStock, hasPreOrder, hasInStock).count { it }
        if (signalCount != 1) return null
        val mode = when {
            hasOutOfStock -> Mode.OUT_OF_STOCK
            hasPreOrder -> Mode.PRE_ORDER
            else -> Mode.IN_STOCK
        }
        return ExtractedAvailability(mode, PreCheckSource.TEXT_HEURISTIC)
    }

    /** A near-empty body usually means the real content is injected by client-side JS. */
    fun looksLikeJsShell(document: Document): Boolean =
        document.body().text().trim().length < MIN_BODY_TEXT_LENGTH

    fun looksLikeBlockedPage(document: Document, statusCode: Int): Boolean {
        if (statusCode == 403 || statusCode == 429 || statusCode == 503) return true
        val text = document.text().lowercase()
        return BLOCK_PAGE_MARKERS.any { text.contains(it) }
    }

    private fun findAvailabilityToken(element: JsonElement): String? = when (element) {
        is JsonObject -> {
            val direct = (element["availability"] as? JsonPrimitive)?.contentOrNull
            direct ?: element.values.firstNotNullOfOrNull { findAvailabilityToken(it) }
        }

        is JsonArray -> element.firstNotNullOfOrNull { findAvailabilityToken(it) }
        else -> null
    }

    private fun mapSchemaAvailabilityToken(raw: String): Mode? =
        when (raw.substringAfterLast('/').trim()) {
            "InStock", "LimitedAvailability", "InStoreOnly", "OnlineOnly" -> Mode.IN_STOCK
            "OutOfStock", "SoldOut", "Discontinued" -> Mode.OUT_OF_STOCK
            "PreOrder", "PreSale", "BackOrder" -> Mode.PRE_ORDER
            // "Reserved" and anything unrecognized is deliberately not mapped — ambiguous.
            else -> null
        }

    private fun mapOpenGraphAvailability(raw: String): Mode? =
        when (raw.trim().lowercase().replace(" ", "").replace("_", "")) {
            "instock" -> Mode.IN_STOCK
            "outofstock" -> Mode.OUT_OF_STOCK
            "preorder", "availablefororder" -> Mode.PRE_ORDER
            else -> null
        }
}
