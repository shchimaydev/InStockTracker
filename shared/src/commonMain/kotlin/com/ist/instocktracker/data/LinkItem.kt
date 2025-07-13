package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialInfo

/**
 * Declares a multiplatform annotation that is expected to be implemented
 * on each target platform. On Android/iOS, this will map to the real
 * Firestore @DocumentId. On the JVM, it will be a simple annotation
 * with no special behavior.
 */
@SerialInfo
expect annotation class DocumentId()

@Serializable
enum class Mode {
    IN_STOCK, PRE_ORDER, OUT_OF_STOCK
}

@Serializable
enum class DurationUnit {
    MINUTES, HOURS, DAYS
}


@Serializable
data class Interval (
    val unit: Int = 1,
    val duration: DurationUnit = DurationUnit.HOURS
)

@Serializable
data class LinkItem(
    @DocumentId
    val id: String = "",
    val link: String = "",
    val mode: Mode = Mode.IN_STOCK,
    val additionalInstructions: String? = null,
    val isActive: Boolean = false,
    val interval: Interval = Interval()

)
