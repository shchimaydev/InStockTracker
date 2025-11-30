package com.ist.instocktracker.data

import com.ist.instocktracker.DocumentId
import kotlinx.datetime.*
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.serialization.Serializable


@Serializable
enum class Mode(val displayName: String) {
    IN_STOCK(displayName = "In Stock"), PRE_ORDER(displayName = "Pre Order"), OUT_OF_STOCK(displayName = "Out of Stock")
}

@Serializable
enum class DurationUnit(val displayName: String) {
    MINUTES("Minutes"), HOURS("Hours"), DAYS("Days")
}


@Serializable
data class Interval(
    val unit: Int = 1,
    val duration: DurationUnit = DurationUnit.HOURS
)

fun Interval.getDisplayName(): String {
    val (unit, duration) = this
    //var str = "every $unit"
    val str = StringBuilder().append("every $unit")


    when (duration) {
        DurationUnit.MINUTES -> str.append(" minute")
        DurationUnit.HOURS -> str.append(" hour")
        DurationUnit.DAYS -> str.append(" day")
    }

    if (unit > 1) str.append("s")

    return str.toString()
}

@Serializable
data class LinkItem(
    @DocumentId
    val id: String = "",
    val userId: String? = null,
    val label: String? = null,
    val link: String = "",
    val mode: Mode = Mode.IN_STOCK,
    val startAt: String? = null,
    val additionalInstructions: String? = null,
    val isActive: Boolean = false,
    val interval: Interval = Interval(),
    val scheduleJobId: String? = null,
    val lastCheckResult: Boolean? = null,
    val lastCheckDate: String? = null,
    val placeholderImage: String? = null

) {
    @OptIn(FormatStringsInDatetimeFormats::class)
    fun lastCheckedDateFormatted(): String {
        if (lastCheckDate == null) return "-"

        return try {
            val instant = Instant.parse(lastCheckDate)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val formatter = LocalDateTime.Format {
                // "EEE" = Mon, "MMM" = Jan, "d" = 25, "h:mm" = 2:05, "a" = AM/PM
                byUnicodePattern("EEE, MMM d 'at' h:mm a")
            }

            localDateTime.format(formatter)
        } catch (e: Exception) {
            println("Error formatting last check date: ${e.message}")
            lastCheckDate
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "link" to link,
            "label" to label,
            "userId" to userId,
            "mode" to mode.name,
            "startAt" to startAt,
            "additionalInstructions" to additionalInstructions,
            "isActive" to isActive,
            "interval" to interval,
            "scheduleJobId" to scheduleJobId,
            "lastCheckResult" to lastCheckResult,
            "lastCheckDate" to lastCheckDate
        )
    }

}
