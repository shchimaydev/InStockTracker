package com.ist.instocktracker.data.auth

import com.ist.instocktracker.Api
import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val isSignedIn: StateFlow<Boolean>
    suspend fun silentSignIn(): Boolean
    suspend fun signIn()
    suspend fun signOut()
    suspend fun <T> runWithAuth(block: suspend (api: Api) -> T): T
}