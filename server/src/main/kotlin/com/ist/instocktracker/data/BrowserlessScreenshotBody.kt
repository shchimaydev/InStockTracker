package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

@Serializable
data class BrowserlessScreenshotOptions(
    val fullPage: Boolean = true,
    val type: String = "jpeg"
)

@Serializable
data class BrowserlessScreenshotBody(
    val url: String,
    val options: BrowserlessScreenshotOptions = BrowserlessScreenshotOptions()
)
