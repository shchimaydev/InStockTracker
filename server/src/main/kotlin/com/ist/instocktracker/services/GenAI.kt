package com.ist.instocktracker.services

import com.google.genai.Client
import io.ktor.server.engine.applicationEnvironment

object GenAI {
    private val geminiApiKey = applicationEnvironment().config.property("app.gemini.apiKey").getString()
    val geminiClient: Client by lazy { Client.builder().apiKey(geminiApiKey).build() }
}