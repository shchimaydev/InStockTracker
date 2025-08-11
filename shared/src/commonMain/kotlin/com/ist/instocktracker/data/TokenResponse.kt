package com.ist.instocktracker.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun fromJson(jsonString: String?): TokenResponse? {
            return if (jsonString != null) {
                try {
                    Json.decodeFromString<TokenResponse>(jsonString)
                } catch (e: Exception) {
                    // Handle potential parsing errors
                    null
                }
            } else {
                null
            }
        }
    }

}
