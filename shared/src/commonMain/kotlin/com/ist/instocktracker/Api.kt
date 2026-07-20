package com.ist.instocktracker

import com.ist.instocktracker.data.DeviceToken
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Platform
import com.ist.instocktracker.data.PostGoogleIdVerificationBody
import com.ist.instocktracker.data.SyncLimitsResult
import com.ist.instocktracker.data.auth.RefreshTokenException
import com.ist.instocktracker.data.auth.RefreshTokenRequest
import com.ist.instocktracker.data.auth.TokenResponse
import com.ist.instocktracker.data.interfaces.TokenStore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class Api(private val tokenStore: TokenStore, isDev: Boolean = true) {
    private val prodHost = "https://instocktracker-464721.ey.r.appspot.com"
    private val localHost = "http://10.0.2.2:8080"
    private val host = if (isDev) localHost else prodHost

    // A client for public endpoints that don't need authentication
    private val nonAuthClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    val authClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val tokens = tokenStore.getJwt().first()
                    println("authClient tokens: $tokens")
                    tokens?.let { BearerTokens(it.accessToken, it.refreshToken) }
                }
                refreshTokens {
                    println("authClient refreshTokens")
                    val currentTokens = tokenStore.getJwt().first()
                    val refreshToken = currentTokens?.refreshToken ?: return@refreshTokens null

                    try {
                        val tokenResponse: TokenResponse = nonAuthClient.post("$host/api/v1/token-refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequest(refreshToken))
                            markAsRefreshTokenRequest()
                        }.body()

                        tokenStore.saveJwt(tokenResponse)
                        BearerTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                    } catch (e: Exception) {
                        println("About to clearJwt")
                        tokenStore.clearJwt()
                        throw RefreshTokenException("Failed to refresh token. Session expired.", e)
                    }
                }
            }
        }
    }


    suspend fun verifyIdToken(idToken: String): TokenResponse {
        val res = nonAuthClient.post("$host/api/v1/id-verification") {
            contentType(ContentType.Application.Json)
            setBody(PostGoogleIdVerificationBody(idToken))
        }
        if (res.status.isSuccess()) {
            return res.body<TokenResponse>()
        } else {
            throw Exception("Failed to verify ID token: ${res.status}, error: ${res.bodyAsText()}")
        }
    }

    suspend fun getLinkItemsForUser(): List<LinkItem> {
        val res = authClient.get("$host/api/v1/link-items")
        if (res.status.isSuccess()) {
            return res.body<List<LinkItem>>()
        } else {
            throw Exception("Failed to get link items: ${res.status}, error: ${res.bodyAsText()}")
        }
    }

    suspend fun getCurrentUser(): com.ist.instocktracker.data.User {
        val res = authClient.get("$host/api/v1/user/me")
        if (res.status.isSuccess()) {
            return res.body<com.ist.instocktracker.data.User>()
        } else {
            throw Exception("Failed to get current user: ${res.status}, error: ${res.bodyAsText()}")
        }
    }

    suspend fun getLinkItem(id: String): LinkItem {
        val res = authClient.get("$host/api/v1/link-items/$id")
        if (res.status.isSuccess()) {
            return res.body<LinkItem>()
        } else {
            throw Exception("Failed to get link item: ${res.status}, error: ${res.bodyAsText()}")
        }
    }

    suspend fun createLinkItem(linkItem: LinkItem): LinkItem {
        val res = authClient.post("$host/api/v1/link-items") {
            contentType(ContentType.Application.Json)
            setBody(linkItem)
        }
        if (res.status.isSuccess()) {
            return res.body<LinkItem>()
        } else {
            throw Exception("Failed to create link item: ${res.status}, error: ${res.bodyAsText()}")
        }
    }

    suspend fun updateLinkItem(id: String, linkItem: LinkItem): LinkItem {
        val res = authClient.put("$host/api/v1/link-items/$id") {
            contentType(ContentType.Application.Json)
            setBody(linkItem)
        }
        if (res.status.isSuccess()) {
            return res.body<LinkItem>()
        } else {
            throw Exception("Failed to update link item: ${res.status}, error: ${res.bodyAsText()}")
        }
    }

    suspend fun deleteLinkItem(id: String) {
        val res = authClient.delete("$host/api/v1/link-items/$id")
        if (!res.status.isSuccess()) {
            throw Exception("Failed to delete link item: ${res.status}, error: ${res.bodyAsText()}")
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun sendDeviceToken(token: String, platform: Platform) {
        println("Sending device token: $token")
        val res = authClient.post("$host/api/v1/device-token") {
            contentType(ContentType.Application.Json)
            setBody(DeviceToken(token, platform, createdAt = Clock.System.now().toEpochMilliseconds()))
        }
        if (!res.status.isSuccess()) {
            throw Exception("Failed to send device token: ${res.status}")
        }
    }

    suspend fun syncLimits(limit: Int): SyncLimitsResult {
        val res = authClient.put("$host/api/v1/user/sync-limits") {
            parameter("limit", limit)
        }
        if (res.status.isSuccess()) {
            return res.body<SyncLimitsResult>()
        } else {
            throw Exception("Failed to sync limits: ${res.status}, error: ${res.bodyAsText()}")
        }
    }
}