package com.ist.instocktracker.data.interfaces

import com.ist.instocktracker.data.auth.TokenResponse
import kotlinx.coroutines.flow.Flow

interface TokenStore {

    // JWT tokens
    fun getJwt(): Flow<TokenResponse?>
    suspend fun saveJwt(token: TokenResponse)
    suspend fun clearJwt()

    // GoogleIdToken
    fun getGoogleIdToken(): Flow<String?>
    suspend fun clearGoogleIdToken()
    suspend fun saveGoogleIdToken(token: String)

    // DeviceToken
    suspend fun getDeviceToken(): Flow<String?>
    suspend fun clearDeviceToken()
    suspend fun saveDeviceToken(token: String)

    fun isAuthenticated(): Flow<Boolean>

}