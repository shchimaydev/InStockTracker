package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String?,
    val email: String?,
    val googleIdToken: String,
    val deviceTokens: List<DeviceToken> = emptyList(),
    val trackableItemsLeft: Int = 1

) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "googleIdToken" to googleIdToken,
            "deviceTokens" to deviceTokens.map {
                mapOf(
                    "token" to it.token,
                    "platform" to it.platform.name,
                    "createdAt" to it.createdAt
                )
            },
            "trackableItemsLeft" to trackableItemsLeft
        )
    }
}

fun User.hasDeviceToken(token: String): Boolean {
    return deviceTokens.any { it.token == token }
}

fun User.addTokenIfPresent(deviceToken: DeviceToken): User {
    val (token, platform) = deviceToken
    if (token.isEmpty()) return this
    return copy(deviceTokens = this.deviceTokens + DeviceToken(token, platform, deviceToken.createdAt))
}

fun User.removeTokens(tokenList: List<String>): User {
    return copy(deviceTokens = this.deviceTokens.filter { !tokenList.contains(it.token) })
}

fun User.removeToken(token: String): User {
    val updated = this.deviceTokens.filter { it.token != token }
    return copy(deviceTokens = updated)
}