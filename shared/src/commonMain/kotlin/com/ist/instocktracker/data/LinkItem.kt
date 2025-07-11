package com.ist.instocktracker.data

/**
 * Declares a multiplatform annotation that is expected to be implemented
 * on each target platform. On Android/iOS, this will map to the real
 * Firestore @DocumentId. On the JVM, it will be a simple annotation
 * with no special behavior.
 */
expect annotation class DocumentId()

enum class Mode {
    IN_STOCK, PRE_ORDER, OUT_OF_STOCK
}

data class LinkItem(
    @DocumentId
    val id: String = "",
    val link: String = "",
    val mode: Mode = Mode.IN_STOCK,
    val additionalInstructions: String? = null,
    val isActive: Boolean = false
)
