package com.ist.instocktracker

import kotlinx.serialization.SerialInfo

/**
 * Declares a multiplatform annotation that is expected to be implemented
 * on each target platform. On Android/iOS, this will map to the real
 * Firestore @DocumentId. On the JVM, it will be a simple annotation
 * with no special behavior.
 */
@SerialInfo
expect annotation class DocumentId()