package com.ist.instocktracker

import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.PostGoogleIdVerificationBody
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
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

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
                            markAsRefreshTokenRequest()
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequest(refreshToken))
                            headers {
                                append(HttpHeaders.Authorization, "Bearer ${currentTokens.accessToken}")
                            }
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
        return nonAuthClient
            .post("$host/api/v1/id-verification") {
                contentType(ContentType.Application.Json)
                setBody(PostGoogleIdVerificationBody(idToken))
            }.body<TokenResponse>()
    }

    suspend fun getLinkItemsForUser(): List<LinkItem> {
        return authClient.get("$host/api/v1/link-items") {
        }.body<List<LinkItem>>()
    }

    suspend fun getLinkItem(id: String): LinkItem {
        return authClient.get("$host/api/v1/link-items/$id") {
        }.body<LinkItem>()
    }

    suspend fun createLinkItem(linkItem: LinkItem): LinkItem {
        return authClient.post("$host/api/v1/link-items") {
            contentType(ContentType.Application.Json)
            setBody(linkItem)
        }.body<LinkItem>()
    }

    suspend fun updateLinkItem(id: String, linkItem: LinkItem): LinkItem {
        return authClient.put("$host/api/v1/link-items/$id") {
            contentType(ContentType.Application.Json)
            setBody(linkItem)
        }.body<LinkItem>()
    }
}