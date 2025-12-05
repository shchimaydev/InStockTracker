package com.ist.instocktracker.utils

import kotlinx.datetime.*
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

@OptIn(FormatStringsInDatetimeFormats::class)
fun formatIsoDateString(isoDateString: String?, timeZone: TimeZone? = null, fallback: String = "-"): String {
    if (isoDateString == null) return fallback

    return try {
        val instant = Instant.parse(isoDateString)
        val localDateTime = instant.toLocalDateTime(timeZone ?: TimeZone.currentSystemDefault())

        val formatter = LocalDateTime.Format {
            if ((localDateTime.hour != 0 && localDateTime.minute != 0)) {
                byUnicodePattern("MM/dd/yyyy 'at' HH:mm")
            } else {
                byUnicodePattern("MM/dd/yyyy")
            }
        }

        localDateTime.format(formatter)
    } catch (e: Exception) {
        isoDateString
    }
}