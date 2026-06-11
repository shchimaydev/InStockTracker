package com.ist.instocktracker.utils

import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Interval
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

object LinkItemFactory {
    fun createLinkItemFromSharedUrl(url: String, userId: String): LinkItem {
        return LinkItem(
            link = url,
            userId = userId,
            label = extractHostFromUrl(url),
            mode = Mode.IN_STOCK,
            startAt = (Clock.System.now() + 1.days).toString(),
            interval = Interval(unit = 1, duration = DurationUnit.DAYS),
            isActive = true
        )
    }

    private fun extractHostFromUrl(url: String): String {
        return try {
            url
                .removePrefix("https://")
                .removePrefix("http://")
                .substringBefore("/")
                .substringBefore("?")
        } catch (e: Exception) {
            "New"
        }
    }

    fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}