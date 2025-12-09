package com.ist.instocktracker.utils

fun extractUrlFromText(text: String): String? {
    val urlPattern = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
    return urlPattern.find(text)?.value
}