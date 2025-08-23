package com.ist.instocktracker.data

import com.ist.instocktracker.DocumentId
import kotlinx.serialization.Serializable


@Serializable
enum class Mode {
    IN_STOCK, PRE_ORDER, OUT_OF_STOCK
}

@Serializable
enum class DurationUnit {
    MINUTES, HOURS, DAYS
}


@Serializable
data class Interval(
    val unit: Int = 1,
    val duration: DurationUnit = DurationUnit.HOURS
)

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
    val lastCheckDate: String? = null

) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "link" to link,
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
