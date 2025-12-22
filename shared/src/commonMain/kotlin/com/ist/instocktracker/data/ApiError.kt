package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val error: String
)
