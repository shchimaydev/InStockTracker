package com.ist.instocktracker.apiHandlers.linkItem.check.precheck

import com.ist.instocktracker.data.Mode
import org.jsoup.Jsoup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AvailabilityExtractorTest {

    private fun parse(html: String) = Jsoup.parse(html)

    @Test
    fun `extractFromJsonLd reads a single Product object`() {
        val html = """
            <html><body>
            <script type="application/ld+json">
            {"@context":"https://schema.org","@type":"Product","name":"Widget",
             "offers":{"@type":"Offer","availability":"https://schema.org/InStock"}}
            </script>
            </body></html>
        """.trimIndent()

        val result = AvailabilityExtractor.extractFromJsonLd(parse(html))

        assertEquals(ExtractedAvailability(Mode.IN_STOCK, PreCheckSource.JSON_LD), result)
    }

    @Test
    fun `extractFromJsonLd reads a Product nested in an @graph array`() {
        val html = """
            <html><body>
            <script type="application/ld+json">
            {"@context":"https://schema.org","@graph":[
              {"@type":"WebPage","name":"Product page"},
              {"@type":"Product","offers":{"availability":"https://schema.org/OutOfStock"}}
            ]}
            </script>
            </body></html>
        """.trimIndent()

        val result = AvailabilityExtractor.extractFromJsonLd(parse(html))

        assertEquals(ExtractedAvailability(Mode.OUT_OF_STOCK, PreCheckSource.JSON_LD), result)
    }

    @Test
    fun `extractFromJsonLd returns null for malformed JSON`() {
        val html = """
            <html><body>
            <script type="application/ld+json">{ not valid json at all </script>
            </body></html>
        """.trimIndent()

        assertNull(AvailabilityExtractor.extractFromJsonLd(parse(html)))
    }

    @Test
    fun `extractFromJsonLd returns null for an unrecognized availability token`() {
        val html = """
            <html><body>
            <script type="application/ld+json">
            {"@type":"Product","offers":{"availability":"https://schema.org/Reserved"}}
            </script>
            </body></html>
        """.trimIndent()

        assertNull(AvailabilityExtractor.extractFromJsonLd(parse(html)))
    }

    @Test
    fun `extractFromMicrodata reads an itemprop availability link`() {
        val html = """
            <html><body>
            <div itemscope itemtype="https://schema.org/Product">
              <link itemprop="availability" href="https://schema.org/PreOrder" />
            </div>
            </body></html>
        """.trimIndent()

        val result = AvailabilityExtractor.extractFromMicrodata(parse(html))

        assertEquals(ExtractedAvailability(Mode.PRE_ORDER, PreCheckSource.MICRODATA), result)
    }

    @Test
    fun `extractFromOpenGraph reads product availability meta tag`() {
        val html = """
            <html><head>
            <meta property="product:availability" content="out of stock" />
            </head><body></body></html>
        """.trimIndent()

        val result = AvailabilityExtractor.extractFromOpenGraph(parse(html))

        assertEquals(ExtractedAvailability(Mode.OUT_OF_STOCK, PreCheckSource.OPEN_GRAPH), result)
    }

    @Test
    fun `extractFromOpenGraph returns null when no matching meta tag exists`() {
        val html = "<html><head></head><body></body></html>"

        assertNull(AvailabilityExtractor.extractFromOpenGraph(parse(html)))
    }

    @Test
    fun `extractFromText matches an unambiguous out-of-stock phrase`() {
        val html = "<html><body><h1>Widget</h1><p>Sorry, this item is Sold Out right now.</p></body></html>"

        val result = AvailabilityExtractor.extractFromText(parse(html))

        assertEquals(ExtractedAvailability(Mode.OUT_OF_STOCK, PreCheckSource.TEXT_HEURISTIC), result)
    }

    @Test
    fun `extractFromText returns null when signals conflict`() {
        val html = """
            <html><body>
              <p>Add to cart</p>
              <p>Similar sold out items you may like</p>
            </body></html>
        """.trimIndent()

        assertNull(AvailabilityExtractor.extractFromText(parse(html)))
    }

    @Test
    fun `extractFromText returns null when nothing matches`() {
        val html = "<html><body><p>Welcome to our store.</p></body></html>"

        assertNull(AvailabilityExtractor.extractFromText(parse(html)))
    }

    @Test
    fun `looksLikeJsShell is true for a near-empty body`() {
        val html = """<html><body><div id="root"></div></body></html>"""

        assert(AvailabilityExtractor.looksLikeJsShell(parse(html)))
    }

    @Test
    fun `looksLikeJsShell is false for a page with real content`() {
        val html = "<html><body><p>" + "This is a real product description. ".repeat(20) + "</p></body></html>"

        assert(!AvailabilityExtractor.looksLikeJsShell(parse(html)))
    }

    @Test
    fun `looksLikeBlockedPage is true for a challenge status code`() {
        val html = "<html><body><p>Normal looking body text that is long enough to not look like a shell page at all.</p></body></html>"

        assert(AvailabilityExtractor.looksLikeBlockedPage(parse(html), statusCode = 403))
    }

    @Test
    fun `looksLikeBlockedPage is true for known challenge-page markers`() {
        val html = "<html><body><p>Just a moment... Please wait while we verify you are human.</p></body></html>"

        assert(AvailabilityExtractor.looksLikeBlockedPage(parse(html), statusCode = 200))
    }

    @Test
    fun `looksLikeBlockedPage is false for a normal 200 page`() {
        val html = "<html><body><p>Welcome to our store, browse our products below.</p></body></html>"

        assert(!AvailabilityExtractor.looksLikeBlockedPage(parse(html), statusCode = 200))
    }
}
