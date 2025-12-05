package com.ist.instocktracker.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

/**
 * Parses the startAt date string and returns a LocalDateTime.
 * If the time component is missing, returns the date at 00:00.
 *
 * @param startAt The date string to parse (e.g., "2024-12-04T10:30" or "2024-12-04")
 * @return LocalDateTime representing the parsed date and time
 */
fun parseStartAt(startAt: String): LocalDateTime {
    return try {
        // Try parsing as LocalDateTime (has time component)
        LocalDateTime.parse(startAt)
    } catch (e: Exception) {
        // If parsing as LocalDateTime fails, try as LocalDate and set time to 00:00
        try {
            LocalDate.parse(startAt).atTime(0, 0)
        } catch (e2: Exception) {
            // If all parsing fails, throw an exception
            throw IllegalArgumentException("Unable to parse startAt date: $startAt", e2)
        }
    }
}
