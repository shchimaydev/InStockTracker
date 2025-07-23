package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

@Serializable
data class CheckResponse(
    val status: String,
    val linkItemId: String,
    val result: Boolean,
    val timestamp: String
)

