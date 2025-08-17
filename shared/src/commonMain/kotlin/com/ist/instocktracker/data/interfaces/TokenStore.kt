package com.ist.instocktracker.data.interfaces

import com.ist.instocktracker.data.auth.TokenResponse
import kotlinx.coroutines.flow.Flow

interface TokenStore {

    // JWT tokens
    fun getJwt(): Flow<TokenResponse?>
    suspend fun saveJwt(token: TokenResponse)
    suspend fun clearJwt()

    // GoogleIdToken
    suspend fun clearGoogleIdToken()
    suspend fun saveGoogleIdToken(token: String)
    fun getGoogleIdToken(): Flow<String?>
    fun isAuthenticated(): Flow<Boolean>

}