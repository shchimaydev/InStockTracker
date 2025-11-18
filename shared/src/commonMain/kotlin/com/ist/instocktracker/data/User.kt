package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String?,
    val email: String?,
    val googleIdToken: String,
    val deviceTokens: List<DeviceToken> = emptyList()
)

fun User.hasDeviceToken(token: String): Boolean {
    return deviceTokens.any { it.token == token }
}

fun User.addTokenIfPresent(deviceToken: DeviceToken): User {
    val (token, platform) = deviceToken
    if (token.isEmpty()) return this
    val filtered = this.deviceTokens.filterNot { it.token == token }
    val updated = filtered + DeviceToken(token, platform, deviceToken.createdAt)
    return copy(deviceTokens = updated)
}