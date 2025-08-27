package com.ist.instocktracker.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refreshToken: String)