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
    fun startAtFormatted(timeZone: TimeZone? = null): String {
        if (startAt == null) return "-"

        return try {
//            val localDateTime = LocalDateTime.parse(startAt)
            val instant = Instant.parse(startAt + "Z")
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
            println("Error formatting startAt date: ${e.message}")
            startAt
        }
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun lastCheckedDateFormatted(timeZone: TimeZone? = null): String {
        if (lastCheckDate == null) return "-"

        return try {
            val instant = Instant.parse(lastCheckDate + "Z")
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
