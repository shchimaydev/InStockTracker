package com.ist.instocktracker

import com.ist.instocktracker.data.PostGoogleIdVerificationBody
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

object Api {
    private val host = "https://instocktracker-464721.ey.r.appspot.com"
    private val client = HttpClient()

    suspend fun verifyIdToken(idToken: String) {
        val response = client.post("$host/api/v1/id-verification") {
            contentType(ContentType.Application.Json)
            setBody(PostGoogleIdVerificationBody(idToken))
        }
    }
}