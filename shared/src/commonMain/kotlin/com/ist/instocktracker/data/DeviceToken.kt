package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

@Serializable
data class DeviceToken(
    val token: String,
    val platform: Platform,
    val createdAt: Long
)

@Serializable
enum class Platform { ANDROID, IOS }
