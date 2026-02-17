package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

sealed interface BrowserlessRequestConfigurationOptions {
    val waitUntil: String?
}

@Serializable
data class BrowserlessScreenshotOptions(
    val fullPage: Boolean = true,
    val type: String = "jpeg"
)

@Serializable
data class BrowserlessViewport(
    val width: Int,
    val height: Int
)

@Serializable
data class BrowserlessScreenshotBody(
    val url: String,
    val viewport: BrowserlessViewport? = null,
    val options: BrowserlessScreenshotOptions = BrowserlessScreenshotOptions()
)

@Serializable
data class BrowserlessUnblockBody(
    val url: String = "",
    val content: Boolean = false,
    val cookies: Boolean = false,
    val screenshot: Boolean = false,
    val browserWSEndpoint: Boolean = false,
    val ttl: Int = 0
)