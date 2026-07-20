package com.ist.instocktracker.data

import com.ist.instocktracker.DocumentId
import com.ist.instocktracker.utils.formatIsoDateString
import kotlinx.datetime.TimeZone
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
    val userId: String,
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
    val placeholderImage: String? = null,
    val updatedAt: Long = 0L,
    val isFrozen: Boolean = false

) {

    fun startAtFormatted(timeZone: TimeZone? = null): String {
        if (startAt == null) return "-"

        return try {
            return formatIsoDateString(startAt, timeZone)
        } catch (e: Exception) {
            println("Error formatting startAt date: ${e.message}")
            startAt

        }
    }

    fun lastCheckedDateFormatted(timeZone: TimeZone? = null): String {
        if (lastCheckDate == null) return "-"

        return try {
            return formatIsoDateString(lastCheckDate, timeZone)
        } catch (e: Exception) {
            println("Error formatting lastCheckDate date: ${e.message}")
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
            "lastCheckDate" to lastCheckDate,
            "updatedAt" to updatedAt,
            "isFrozen" to isFrozen
        )
    }

}
