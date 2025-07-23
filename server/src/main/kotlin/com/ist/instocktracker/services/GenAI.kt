package com.ist.instocktracker.services

import com.google.genai.Client
import io.ktor.server.application.Application
import io.ktor.server.engine.applicationEnvironment

object GenAI {
    private lateinit var geminiApiKey: String
    val geminiClient: Client by lazy { Client.builder().apiKey(geminiApiKey).build() }

    fun init(application: Application) {
        geminiApiKey = application.environment.config.property("app.gemini.apiKey").getString()
    }
}